# ─────────────────────────────────────────────────────────────────────────
# collector.py — Metrics collector for CLOUDSHADOW Agent
# Collects: CPU, Memory, Disk, Network using psutil
# ─────────────────────────────────────────────────────────────────────────

import psutil
import platform
from logger import logger


class MetricsCollector:
    """
    Collects system metrics using psutil.
    All values are percentages (0.0 - 100.0) except network (bytes).
    """

    def __init__(self):
        # ── Initialize network baseline (first call returns 0) ─────────────
        self._last_net_io = psutil.net_io_counters()
        self.hostname = platform.node()
        self.os_info = f"{platform.system()} {platform.release()}"
        logger.info(f"Collector initialized on {self.hostname} ({self.os_info})")

    def collect_cpu(self) -> float:
        """
        Collect CPU usage percentage.
        interval=1 → measures over 1 second for accuracy.
        """
        try:
            cpu = psutil.cpu_percent(interval=1)
            logger.debug(f"CPU collected: {cpu}%")
            return round(cpu, 2)
        except Exception as e:
            logger.error(f"Failed to collect CPU: {e}")
            return 0.0

    def collect_memory(self) -> float:
        """
        Collect RAM usage percentage.
        Uses virtual memory (RAM) not swap.
        """
        try:
            memory = psutil.virtual_memory()
            usage = memory.percent
            logger.debug(
                f"Memory collected: {usage}% "
                f"({self._bytes_to_gb(memory.used)}GB / "
                f"{self._bytes_to_gb(memory.total)}GB)"
            )
            return round(usage, 2)
        except Exception as e:
            logger.error(f"Failed to collect memory: {e}")
            return 0.0

    def collect_disk(self) -> float:
        """
        Collect disk usage percentage for root partition.
        """
        try:
            # Use root partition (works on Linux/Mac/Windows)
            path = "C:\\" if platform.system() == "Windows" else "/"
            disk = psutil.disk_usage(path)
            usage = disk.percent
            logger.debug(
                f"Disk collected: {usage}% "
                f"({self._bytes_to_gb(disk.used)}GB / "
                f"{self._bytes_to_gb(disk.total)}GB)"
            )
            return round(usage, 2)
        except Exception as e:
            logger.error(f"Failed to collect disk: {e}")
            return 0.0

    def collect_network(self) -> dict:
        """
        Collect network I/O in MB/s since last collection.
        Returns bytes_sent and bytes_recv per second.
        """
        try:
            current_net_io = psutil.net_io_counters()

            # ── Calculate delta since last collection ──────────────────────
            bytes_sent = max(
                0,
                current_net_io.bytes_sent - self._last_net_io.bytes_sent
            )
            bytes_recv = max(
                0,
                current_net_io.bytes_recv - self._last_net_io.bytes_recv
            )

            # ── Update baseline ────────────────────────────────────────────
            self._last_net_io = current_net_io

            # ── Convert to KB/s ────────────────────────────────────────────
            network_out = round(bytes_sent / 1024, 2)
            network_in  = round(bytes_recv / 1024, 2)

            logger.debug(
                f"Network collected: "
                f"IN={network_in}KB/s OUT={network_out}KB/s"
            )

            return {
                "network_in":  network_in,
                "network_out": network_out
            }
        except Exception as e:
            logger.error(f"Failed to collect network: {e}")
            return {"network_in": 0.0, "network_out": 0.0}

    def collect_all(self) -> dict:
        """
        Collect all metrics at once.
        Returns a complete metrics payload ready to send to backend.
        """
        logger.info("─── Collecting metrics ───────────────────────────")

        cpu     = self.collect_cpu()
        memory  = self.collect_memory()
        disk    = self.collect_disk()
        network = self.collect_network()

        metrics = {
            "cpu":         cpu,
            "memory":      memory,
            "disk":        disk,
            "network_in":  network["network_in"],
            "network_out": network["network_out"],
        }

        logger.info(
            f"Metrics → CPU: {cpu}% | "
            f"MEM: {memory}% | "
            f"DISK: {disk}% | "
            f"NET_IN: {network['network_in']}KB/s | "
            f"NET_OUT: {network['network_out']}KB/s"
        )

        return metrics

    # ─── Helpers ──────────────────────────────────────────────────────────
    @staticmethod
    def _bytes_to_gb(b: int) -> float:
        return round(b / (1024 ** 3), 2)