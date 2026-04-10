# ─────────────────────────────────────────────────────────────────────────
# agent.py — CLOUDSHADOW Agent Main Entry Point
#
# This agent:
# 1. Loads secure config (token from env variable)
# 2. Collects system metrics every 20 seconds
# 3. Sends metrics to CLOUDSHADOW backend
# 4. Handles errors gracefully with retry logic
#
# Usage:
#   export CLOUDSHADOW_TOKEN=your_token_here
#   export CLOUDSHADOW_BACKEND_URL=http://your-backend:8080
#   python3 agent.py
# ─────────────────────────────────────────────────────────────────────────

import time
import signal
import sys

from config import config
from collector import MetricsCollector
from sender import MetricsSender
from logger import logger


# ─── Graceful shutdown handler ────────────────────────────────────────────
def handle_shutdown(signum, frame):
    """Handle CTRL+C and kill signals gracefully"""
    logger.info("─────────────────────────────────────────────────")
    logger.info("🛑 Shutdown signal received — stopping agent...")
    logger.info("─────────────────────────────────────────────────")
    sender.close()
    sys.exit(0)


# ─── Register shutdown signals ────────────────────────────────────────────
signal.signal(signal.SIGINT,  handle_shutdown)   # CTRL+C
signal.signal(signal.SIGTERM, handle_shutdown)   # Docker stop / kill


# ─────────────────────────────────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":

    # ── Banner ─────────────────────────────────────────────────────────────
    print("═══════════════════════════════════════════════════")
    print("          🌩️  CLOUDSHADOW AGENT v1.0               ")
    print("═══════════════════════════════════════════════════")

    # ── Display config (token masked) ──────────────────────────────────────
    config.display()
    print("═══════════════════════════════════════════════════")

    # ── Initialize collector and sender ────────────────────────────────────
    collector = MetricsCollector()
    sender    = MetricsSender()

    logger.info("🚀 Agent started — sending metrics every "
                f"{config.interval}s")
    logger.info("─────────────────────────────────────────────────")

    # ── Stats tracking ─────────────────────────────────────────────────────
    total_sent   = 0
    total_failed = 0
    start_time   = time.time()

    # ─────────────────────────────────────────────────────────────────────
    # MAIN LOOP
    # ─────────────────────────────────────────────────────────────────────
    while True:
        try:
            # ── 1. Collect all metrics ─────────────────────────────────────
            metrics = collector.collect_all()

            # ── 2. Send metrics to backend ─────────────────────────────────
            success = sender.send(metrics)

            # ── 3. Update stats ────────────────────────────────────────────
            if success:
                total_sent += 1
            else:
                total_failed += 1

            # ── 4. Log session stats every 10 cycles ──────────────────────
            if (total_sent + total_failed) % 10 == 0:
                uptime = int(time.time() - start_time)
                logger.info(
                    f"📊 Session stats → "
                    f"Sent: {total_sent} | "
                    f"Failed: {total_failed} | "
                    f"Uptime: {uptime}s"
                )

            # ── 5. Wait for next cycle ─────────────────────────────────────
            logger.debug(f"Sleeping {config.interval}s until next collection...")
            time.sleep(config.interval)

        # ── Handle unexpected errors without crashing ──────────────────────
        except Exception as e:
            logger.error(f"❌ Unexpected error in main loop: {e}")
            logger.info("Recovering — waiting 30s before retry...")
            time.sleep(30)