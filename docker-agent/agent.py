import docker
import requests
import time
import os
import logging

# ─── Logging Setup ────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - CloudShadow Docker Agent - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ─── Config from Environment Variables ───────────────────────────────────
BACKEND_URL    = os.getenv('BACKEND_URL', 'http://localhost:8080')
SERVER_TOKEN   = os.getenv('SERVER_TOKEN', '')
INTERVAL       = int(os.getenv('INTERVAL', '20'))
MONITOR_CONTAINER = os.getenv('MONITOR_CONTAINER', '')  # container name to monitor
METRICS_ENDPOINT  = f"{BACKEND_URL}/api/metrics"


# ─── Docker Stats Parser ──────────────────────────────────────────────────

def calculate_cpu_percent(stats: dict) -> float:
    """
    Calculates CPU usage % from Docker stats.

    Docker gives us raw CPU ticks, we calculate
    the percentage ourselves.

    Formula used by 'docker stats' command itself.
    """
    try:
        cpu_delta = (
            stats['cpu_stats']['cpu_usage']['total_usage'] -
            stats['precpu_stats']['cpu_usage']['total_usage']
        )

        system_delta = (
            stats['cpu_stats']['system_cpu_usage'] -
            stats['precpu_stats']['system_cpu_usage']
        )

        num_cpus = stats['cpu_stats'].get('online_cpus') or \
                   len(stats['cpu_stats']['cpu_usage'].get('percpu_usage', [1]))

        if system_delta > 0 and cpu_delta > 0:
            cpu_percent = (cpu_delta / system_delta) * num_cpus * 100.0
            return round(min(cpu_percent, 100.0), 2)

        return 0.0

    except (KeyError, ZeroDivisionError):
        return 0.0


def calculate_memory_percent(stats: dict) -> float:
    """
    Calculates memory usage % from Docker stats.

    Uses: (used_memory / available_memory) * 100
    Excludes cache from used memory (same as docker stats).
    """
    try:
        mem_stats = stats['memory_stats']

        used_memory = mem_stats['usage']
        available   = mem_stats['limit']

        # Exclude file cache (same formula as docker stats CLI)
        cache = mem_stats.get('stats', {}).get('cache', 0)
        actual_used = used_memory - cache

        if available > 0:
            percent = (actual_used / available) * 100.0
            return round(min(percent, 100.0), 2)

        return 0.0

    except (KeyError, ZeroDivisionError):
        return 0.0


def calculate_disk_percent(stats: dict) -> float:
    """
    Calculates disk I/O usage as a relative % from Docker stats.

    Note: Docker doesn't give disk % directly.
    We use block I/O bytes and normalize to a
    relative scale based on a reasonable max
    (1GB of block I/O = 100%).

    This gives a meaningful trending metric.
    """
    try:
        blkio = stats.get('blkio_stats', {})
        io_service_bytes = blkio.get('io_service_bytes_recursive', [])

        total_bytes = 0
        for entry in io_service_bytes:
            if entry.get('op', '').lower() in ('read', 'write'):
                total_bytes += entry.get('value', 0)

        # Normalize: 1GB = 100%
        max_bytes = 1 * 1024 * 1024 * 1024  # 1 GB
        percent = (total_bytes / max_bytes) * 100.0
        return round(min(percent, 100.0), 2)

    except (KeyError, ZeroDivisionError):
        return 0.0


def calculate_network(stats: dict):
    """
    Calculates network I/O in KB/s from Docker stats.

    Returns (network_in_kb, network_out_kb)
    """
    try:
        networks = stats.get('networks', {})

        total_rx = 0  # bytes received
        total_tx = 0  # bytes sent

        for interface, data in networks.items():
            total_rx += data.get('rx_bytes', 0)
            total_tx += data.get('tx_bytes', 0)

        # Convert bytes to KB
        network_in  = round(total_rx / 1024, 2)
        network_out = round(total_tx / 1024, 2)

        return network_in, network_out

    except (KeyError, AttributeError):
        return 0.0, 0.0


# ─── Collect Metrics from Container ──────────────────────────────────────

def collect_container_metrics(container) -> dict:
    """
    Collects all metrics from a Docker container.

    Uses Docker SDK's stats() with stream=False
    for a single snapshot read.
    """

    # Get single stats snapshot (stream=False = one reading)
    stats = container.stats(stream=False)

    cpu     = calculate_cpu_percent(stats)
    memory  = calculate_memory_percent(stats)
    disk    = calculate_disk_percent(stats)
    net_in, net_out = calculate_network(stats)

    return {
        "token":      SERVER_TOKEN,
        "cpu":        cpu,
        "memory":     memory,
        "disk":       disk,
        "network_in":  net_in,
        "network_out": net_out
    }


# ─── Send Metrics to Backend ─────────────────────────────────────────────

def send_metrics(metrics: dict) -> bool:
    """
    POSTs metrics to CloudShadow backend.
    Returns True if successful.
    """
    try:
        response = requests.post(
            METRICS_ENDPOINT,
            json=metrics,
            timeout=10,
            headers={"Content-Type": "application/json"}
        )

        if response.status_code == 200:
            logger.info(
                f"✅ Sent → "
                f"CPU: {metrics['cpu']}% | "
                f"MEM: {metrics['memory']}% | "
                f"DISK: {metrics['disk']}% | "
                f"NET↓: {metrics['network_in']} KB | "
                f"NET↑: {metrics['network_out']} KB"
            )
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
        logger.error(f"❌ Unexpected error sending metrics: {e}")
        return False


# ─── Validate Config ──────────────────────────────────────────────────────

def validate_config():
    """Validates required env vars before starting."""

    errors = []

    if not SERVER_TOKEN:
        errors.append("SERVER_TOKEN is not set")

    if not BACKEND_URL:
        errors.append("BACKEND_URL is not set")

    if not MONITOR_CONTAINER:
        errors.append("MONITOR_CONTAINER is not set")

    if errors:
        for e in errors:
            logger.error(f"❌ Config error: {e}")
        raise ValueError(
            "Agent misconfigured. "
            "Check SERVER_TOKEN, BACKEND_URL, MONITOR_CONTAINER."
        )

    logger.info("🐳 CloudShadow Docker Agent starting...")
    logger.info(f"   Backend URL:       {BACKEND_URL}")
    logger.info(f"   Token:             {SERVER_TOKEN[:8]}****")
    logger.info(f"   Monitor container: {MONITOR_CONTAINER}")
    logger.info(f"   Interval:          {INTERVAL}s")


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


# ─── Get Target Container ─────────────────────────────────────────────────

def get_container(client):
    """
    Gets the target container by name.
    Retries until found (container may not be up yet).
    """
    while True:
        try:
            container = client.containers.get(MONITOR_CONTAINER)
            logger.info(
                f"✅ Found container: {MONITOR_CONTAINER} "
                f"[{container.status}]"
            )
            return container

        except docker.errors.NotFound:
            logger.warning(
                f"⏳ Container '{MONITOR_CONTAINER}' not found "
                f"- retrying in 5s..."
            )
            time.sleep(5)

        except Exception as e:
            logger.error(f"❌ Docker error: {e}")
            time.sleep(5)


# ─── Main Loop ────────────────────────────────────────────────────────────

def run():
    """Main agent loop."""

    # Validate config
    validate_config()

    # Wait for backend
    wait_for_backend()

    # Connect to Docker socket
    try:
        client = docker.from_env()
        logger.info("✅ Connected to Docker socket")
    except Exception as e:
        logger.error(f"❌ Cannot connect to Docker socket: {e}")
        raise

    # Get target container
    container = get_container(client)

    logger.info(f"📡 Monitoring '{MONITOR_CONTAINER}' every {INTERVAL}s")

    while True:
        try:
            # Refresh container object (catches restarts)
            container = client.containers.get(MONITOR_CONTAINER)

            # Check container is running
            if container.status != 'running':
                logger.warning(
                    f"⚠️ Container '{MONITOR_CONTAINER}' "
                    f"is {container.status} - waiting..."
                )
                time.sleep(INTERVAL)
                continue

            # Collect and send metrics
            metrics = collect_container_metrics(container)
            send_metrics(metrics)

        except docker.errors.NotFound:
            logger.error(
                f"❌ Container '{MONITOR_CONTAINER}' disappeared "
                f"- waiting for restart..."
            )
            time.sleep(5)
            container = get_container(client)

        except Exception as e:
            logger.error(f"❌ Error in main loop: {e}")

        time.sleep(INTERVAL)


if __name__ == "__main__":
    run()