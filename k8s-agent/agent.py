import os
import time
import logging
import requests
import psutil
from kubernetes import client, config
from kubernetes.client.rest import ApiException

# ─── Logging ──────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - CloudShadow K8s Agent - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ─── Config from Environment ──────────────────────────────────────────────
BACKEND_URL  = os.getenv('BACKEND_URL', 'http://backend.cloudshadow:8080')
SERVER_TOKEN = os.getenv('SERVER_TOKEN', '')
INTERVAL     = int(os.getenv('INTERVAL', '20'))
NODE_NAME    = os.getenv('NODE_NAME', 'unknown')  # injected by K8s downward API
METRICS_ENDPOINT = f"{BACKEND_URL}/api/metrics"


# ─── Kubernetes Client Setup ──────────────────────────────────────────────

def setup_k8s_client():
    """
    Sets up K8s client.
    Inside cluster → uses service account token automatically.
    Outside cluster → uses kubeconfig.
    """
    try:
        # Running inside K8s pod
        config.load_incluster_config()
        logger.info("✅ Using in-cluster K8s config")
    except config.ConfigException:
        # Running locally for testing
        config.load_kube_config()
        logger.info("✅ Using local kubeconfig")

    return client.CustomObjectsApi()


# ─── Collect Node Metrics ─────────────────────────────────────────────────

def collect_node_metrics(custom_api) -> dict:
    """
    Collects node-level metrics from K8s metrics-server.

    Returns CPU%, Memory%, Disk%, network_in, network_out
    """
    try:
        # ── Get node metrics from metrics-server ──────────────────────
        node_metrics = custom_api.get_cluster_custom_object(
            group="metrics.k8s.io",
            version="v1beta1",
            plural="nodes",
            name=NODE_NAME
        )

        usage = node_metrics['usage']

        # ── Parse CPU ────────────────────────────────────────────────
        # K8s gives CPU in nanocores (n) or millicores (m)
        cpu_usage = parse_cpu(usage.get('cpu', '0n'))

        # ── Get node capacity for percentage calculation ──────────────
        core_api = client.CoreV1Api()
        node = core_api.read_node(NODE_NAME)
        cpu_capacity = parse_cpu(
            node.status.capacity.get('cpu', '1')
        )
        memory_capacity = parse_memory(
            node.status.capacity.get('memory', '1Ki')
        )

        # ── Parse Memory ─────────────────────────────────────────────
        memory_usage = parse_memory(usage.get('memory', '0Ki'))

        # ── Calculate percentages ─────────────────────────────────────
        cpu_percent = round(
            (cpu_usage / cpu_capacity) * 100, 2
        ) if cpu_capacity > 0 else 0.0

        memory_percent = round(
            (memory_usage / memory_capacity) * 100, 2
        ) if memory_capacity > 0 else 0.0

        # ── Disk from host (psutil reads node disk) ───────────────────
        disk = psutil.disk_usage('/')
        disk_percent = round(disk.percent, 2)

        # ── Network from host (psutil reads node network) ─────────────
        net_in, net_out = collect_network()

        logger.info(
            f"📊 Node {NODE_NAME} → "
            f"CPU: {cpu_percent}% | "
            f"MEM: {memory_percent}% | "
            f"DISK: {disk_percent}% | "
            f"NET↓: {net_in} KB/s | "
            f"NET↑: {net_out} KB/s"
        )

        return {
            "token":      SERVER_TOKEN,
            "cpu":        min(cpu_percent, 100.0),
            "memory":     min(memory_percent, 100.0),
            "disk":       min(disk_percent, 100.0),
            "network_in":  net_in,
            "network_out": net_out
        }

    except ApiException as e:
        logger.error(f"❌ K8s API error: {e.status} - {e.reason}")
        # Fallback to psutil only
        return collect_metrics_psutil_fallback()

    except Exception as e:
        logger.error(f"❌ Error collecting node metrics: {e}")
        return collect_metrics_psutil_fallback()


# ─── Network Collection ───────────────────────────────────────────────────

def collect_network() -> tuple:
    """
    Collects network I/O in KB/s using psutil.
    Takes two readings 1 second apart to calculate rate.
    """
    try:
        before = psutil.net_io_counters()
        time.sleep(1)
        after = psutil.net_io_counters()

        net_in  = round((after.bytes_recv - before.bytes_recv) / 1024, 2)
        net_out = round((after.bytes_sent - before.bytes_sent) / 1024, 2)

        return net_in, net_out

    except Exception:
        return 0.0, 0.0


# ─── Psutil Fallback ──────────────────────────────────────────────────────

def collect_metrics_psutil_fallback() -> dict:
    """
    Fallback when K8s metrics-server is unavailable.
    Uses psutil to read host metrics directly.
    """
    logger.warning("⚠️ Using psutil fallback (metrics-server unavailable)")

    cpu     = psutil.cpu_percent(interval=1)
    memory  = psutil.virtual_memory().percent
    disk    = psutil.disk_usage('/').percent
    net_in, net_out = collect_network()

    return {
        "token":      SERVER_TOKEN,
        "cpu":        round(cpu, 2),
        "memory":     round(memory, 2),
        "disk":       round(disk, 2),
        "network_in":  net_in,
        "network_out": net_out
    }


# ─── CPU Parser ───────────────────────────────────────────────────────────

def parse_cpu(cpu_string: str) -> float:
    """
    Parses K8s CPU values to cores.

    Examples:
      "250m"  → 0.25 cores
      "1"     → 1.0 cores
      "500n"  → 0.0000005 cores (nanocores)
      "2000m" → 2.0 cores
    """
    if cpu_string.endswith('n'):
        # Nanocores
        return float(cpu_string[:-1]) / 1_000_000_000
    elif cpu_string.endswith('m'):
        # Millicores
        return float(cpu_string[:-1]) / 1000
    else:
        # Full cores
        return float(cpu_string)


# ─── Memory Parser ────────────────────────────────────────────────────────

def parse_memory(memory_string: str) -> float:
    """
    Parses K8s memory values to bytes.

    Examples:
      "512Mi" → 536870912 bytes
      "1Gi"   → 1073741824 bytes
      "1024Ki"→ 1048576 bytes
      "1000"  → 1000 bytes
    """
    if memory_string.endswith('Ki'):
        return float(memory_string[:-2]) * 1024
    elif memory_string.endswith('Mi'):
        return float(memory_string[:-2]) * 1024 * 1024
    elif memory_string.endswith('Gi'):
        return float(memory_string[:-2]) * 1024 * 1024 * 1024
    elif memory_string.endswith('Ti'):
        return float(memory_string[:-2]) * 1024 * 1024 * 1024 * 1024
    elif memory_string.endswith('k'):
        return float(memory_string[:-1]) * 1000
    elif memory_string.endswith('M'):
        return float(memory_string[:-1]) * 1000 * 1000
    elif memory_string.endswith('G'):
        return float(memory_string[:-1]) * 1000 * 1000 * 1000
    else:
        return float(memory_string)


# ─── Send Metrics ─────────────────────────────────────────────────────────

def send_metrics(metrics: dict) -> bool:
    """
    Sends collected metrics to CloudShadow backend.
    """
    try:
        response = requests.post(
            METRICS_ENDPOINT,
            json=metrics,
            timeout=10,
            headers={"Content-Type": "application/json"}
        )

        if response.status_code == 200:
            logger.info("✅ Metrics sent successfully")
            return True
        else:
            logger.error(
                f"❌ Backend rejected: "
                f"{response.status_code} - {response.text}"
            )
            return False

    except requests.exceptions.ConnectionError:
        logger.error(
            f"❌ Cannot reach backend at {BACKEND_URL} "
            f"- retrying in {INTERVAL}s"
        )
        return False

    except requests.exceptions.Timeout:
        logger.error("❌ Request timed out")
        return False

    except Exception as e:
        logger.error(f"❌ Unexpected error: {e}")
        return False


# ─── Validate Config ──────────────────────────────────────────────────────

def validate_config():
    """Validates required env vars."""

    errors = []

    if not SERVER_TOKEN:
        errors.append("SERVER_TOKEN is not set")

    if not BACKEND_URL:
        errors.append("BACKEND_URL is not set")

    if errors:
        for e in errors:
            logger.error(f"❌ Config error: {e}")
        raise ValueError("Agent misconfigured. Check environment variables.")

    logger.info("🚀 CloudShadow K8s Agent starting...")
    logger.info(f"   Backend URL: {BACKEND_URL}")
    logger.info(f"   Node Name:   {NODE_NAME}")
    logger.info(f"   Token:       {SERVER_TOKEN[:8]}****")
    logger.info(f"   Interval:    {INTERVAL}s")


# ─── Wait For Backend ─────────────────────────────────────────────────────

def wait_for_backend():
    """Waits until backend is reachable."""

    health_url = f"{BACKEND_URL}/actuator/health"
    logger.info(f"⏳ Waiting for backend at {health_url}...")

    while True:
        try:
            response = requests.get(health_url, timeout=5)
            if response.status_code == 200:
                logger.info("✅ Backend is ready!")
                return
        except Exception:
            pass

        logger.info("⏳ Backend not ready - retrying in 5s...")
        time.sleep(5)


# ─── Main Loop ────────────────────────────────────────────────────────────

def run():
    """Main agent loop."""

    # Validate config
    validate_config()

    # Wait for backend
    wait_for_backend()

    # Setup K8s client
    custom_api = setup_k8s_client()

    logger.info(f"📡 Starting metrics collection every {INTERVAL}s")

    while True:
        try:
            metrics = collect_node_metrics(custom_api)
            send_metrics(metrics)

        except Exception as e:
            logger.error(f"❌ Error in main loop: {e}")

        # Subtract ~1s used by network sampling
        time.sleep(max(1, INTERVAL - 1))


if __name__ == "__main__":
    run()