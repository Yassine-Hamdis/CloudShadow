# ─────────────────────────────────────────────────────────────────────────
# config.py — Secure configuration loader for CLOUDSHADOW Agent
# Token is loaded from environment variable or .env file
# NEVER hardcoded in source code
# ─────────────────────────────────────────────────────────────────────────

import os
import sys
from dotenv import load_dotenv

# Load .env file if it exists
load_dotenv()


class AgentConfig:
    """
    Loads and validates all agent configuration.
    Token is read from environment variable CLOUDSHADOW_TOKEN.
    This ensures the token is never hardcoded in source code.
    """

    def __init__(self):
        # ── Token (REQUIRED — secured via env variable) ────────────────────
        self.token = os.environ.get("CLOUDSHADOW_TOKEN")
        if not self.token:
            print("[CLOUDSHADOW] ❌ ERROR: CLOUDSHADOW_TOKEN is not set!")
            print("[CLOUDSHADOW] Set it using:")
            print("              export CLOUDSHADOW_TOKEN=your_token_here")
            print("              or add it to your .env file")
            sys.exit(1)

        # ── Backend URL ────────────────────────────────────────────────────
        self.backend_url = os.environ.get(
            "CLOUDSHADOW_BACKEND_URL",
            "http://localhost:8080"
        ).rstrip("/")

        # ── Metrics endpoint ───────────────────────────────────────────────
        self.metrics_endpoint = f"{self.backend_url}/api/metrics"

        # ── Sending interval (default 20 seconds) ─────────────────────────
        try:
            self.interval = int(
                os.environ.get("CLOUDSHADOW_INTERVAL", "20")
            )
        except ValueError:
            self.interval = 20

        # ── Log level ──────────────────────────────────────────────────────
        self.log_level = os.environ.get(
            "CLOUDSHADOW_LOG_LEVEL", "INFO"
        ).upper()

        # ── Request timeout (seconds) ──────────────────────────────────────
        self.request_timeout = int(
            os.environ.get("CLOUDSHADOW_TIMEOUT", "10")
        )

        # ── Max retries on failed send ─────────────────────────────────────
        self.max_retries = int(
            os.environ.get("CLOUDSHADOW_MAX_RETRIES", "3")
        )

    def display(self):
        """Display config (token is masked for security)"""
        masked_token = self.token[:6] + "****" + self.token[-4:]
        print(f"[CLOUDSHADOW] ✅ Config loaded:")
        print(f"              Backend URL : {self.backend_url}")
        print(f"              Token       : {masked_token}")
        print(f"              Interval    : {self.interval}s")
        print(f"              Timeout     : {self.request_timeout}s")
        print(f"              Max Retries : {self.max_retries}")
        print(f"              Log Level   : {self.log_level}")


# ─── Singleton config instance ────────────────────────────────────────────
config = AgentConfig()