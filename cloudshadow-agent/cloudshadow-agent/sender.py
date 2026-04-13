# ─────────────────────────────────────────────────────────────────────────
# sender.py — Secure HTTP sender for CLOUDSHADOW Agent
# Sends metrics to backend with retry logic + token auth
# ─────────────────────────────────────────────────────────────────────────

import requests
import time
from config import config
from logger import logger


class MetricsSender:
    """
    Sends collected metrics to the CLOUDSHADOW backend.

    Security:
    - Token is never logged (only masked version)
    - HTTPS supported (set backend URL to https://)
    - Retry logic with exponential backoff
    """

    def __init__(self):
        self.endpoint  = config.metrics_endpoint
        self.token     = config.token
        self.timeout   = config.request_timeout
        self.retries   = config.max_retries

        # ── Mask token for safe logging ────────────────────────────────────
        self._masked_token = (
            self.token[:6] + "****" + self.token[-4:]
            if len(self.token) > 10
            else "****"
        )

        # ── Session for connection reuse (more efficient) ──────────────────
        self.session = requests.Session()
        self.session.headers.update({
            "Content-Type": "application/json",
            "User-Agent":   "CloudShadow-Agent/1.0"
        })

        logger.info(f"Sender initialized → {self.endpoint}")
        logger.info(f"Token: {self._masked_token}")

    def send(self, metrics: dict) -> bool:
        """
        Send metrics to backend with retry logic.
        Token is injected here — never stored in metrics dict before this.

        Returns True if successful, False if all retries failed.
        """

        # ── Build payload with token ───────────────────────────────────────
        payload = {
            "token":       self.token,   # Server token for auth
            "cpu":         metrics["cpu"],
            "memory":      metrics["memory"],
            "disk":        metrics["disk"],
            "network_in":  metrics.get("network_in",  0.0),
            "network_out": metrics.get("network_out", 0.0),
        }

        # ── Retry loop with exponential backoff ───────────────────────────
        for attempt in range(1, self.retries + 1):
            try:
                logger.debug(
                    f"Sending metrics (attempt {attempt}/{self.retries})..."
                )

                response = self.session.post(
                    self.endpoint,
                    json=payload,
                    timeout=self.timeout
                )

                # ── Success ────────────────────────────────────────────────
                if response.status_code == 200:
                    logger.info(
                        f"✅ Metrics sent successfully "
                        f"[{response.status_code}]"
                    )
                    return True

                # ── Invalid token ──────────────────────────────────────────
                elif response.status_code == 401:
                    logger.error(
                        f"❌ Invalid token [{self._masked_token}] — "
                        f"check your CLOUDSHADOW_TOKEN"
                    )
                    return False  # No point retrying — token is wrong

                # ── Server error → retry ───────────────────────────────────
                else:
                    logger.warning(
                        f"⚠️  Backend returned {response.status_code} "
                        f"— retrying ({attempt}/{self.retries})..."
                    )

            except requests.exceptions.ConnectionError:
                logger.warning(
                    f"⚠️  Cannot connect to backend at {self.endpoint} "
                    f"— retrying ({attempt}/{self.retries})..."
                )

            except requests.exceptions.Timeout:
                logger.warning(
                    f"⚠️  Request timed out after {self.timeout}s "
                    f"— retrying ({attempt}/{self.retries})..."
                )

            except requests.exceptions.RequestException as e:
                logger.error(f"❌ Unexpected request error: {e}")

            # ── Exponential backoff before retry ──────────────────────────
            if attempt < self.retries:
                backoff = 2 ** attempt   # 2s, 4s, 8s
                logger.info(f"Waiting {backoff}s before retry...")
                time.sleep(backoff)

        logger.error(
            f"❌ Failed to send metrics after {self.retries} attempts"
        )
        return False

    def close(self):
        """Close the HTTP session cleanly"""
        self.session.close()
        logger.info("HTTP session closed")