# ─────────────────────────────────────────────────────────────────────────
# logger.py — Centralized logger for CLOUDSHADOW Agent
# ─────────────────────────────────────────────────────────────────────────

import logging
import sys
from datetime import datetime


def setup_logger(log_level: str = "INFO") -> logging.Logger:
    """
    Sets up and returns the CLOUDSHADOW agent logger.
    Logs to both console and file.
    """

    logger = logging.getLogger("CLOUDSHADOW")
    logger.setLevel(getattr(logging, log_level, logging.INFO))

    # ── Formatter ──────────────────────────────────────────────────────────
    formatter = logging.Formatter(
        fmt="[%(asctime)s] [%(name)s] [%(levelname)s] %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S"
    )

    # ── Console Handler ────────────────────────────────────────────────────
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)

    # ── File Handler ───────────────────────────────────────────────────────
    try:
        file_handler = logging.FileHandler("cloudshadow-agent.log")
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)
    except PermissionError:
        logger.warning("Cannot write to log file — console logging only")

    return logger


# ─── Singleton logger ─────────────────────────────────────────────────────
logger = setup_logger()