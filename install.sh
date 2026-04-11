#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────
# CLOUDSHADOW Agent — Linux Install Script
# Usage: bash install.sh --token YOUR_TOKEN --url http://your-backend:8080
# ─────────────────────────────────────────────────────────────────────────

set -e

# ─── Colors ───────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ─── Banner ───────────────────────────────────────────────────────────────
echo -e "${BLUE}"
echo "═══════════════════════════════════════════════════"
echo "          🌩️  CLOUDSHADOW AGENT INSTALLER           "
echo "═══════════════════════════════════════════════════"
echo -e "${NC}"

# ─── Parse arguments ──────────────────────────────────────────────────────
TOKEN=""
BACKEND_URL=""
INTERVAL=20

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --token)   TOKEN="$2";       shift ;;
        --url)     BACKEND_URL="$2"; shift ;;
        --interval) INTERVAL="$2";  shift ;;
        *) echo -e "${RED}Unknown parameter: $1${NC}"; exit 1 ;;
    esac
    shift
done

# ─── Validate required args ───────────────────────────────────────────────
if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ ERROR: --token is required${NC}"
    echo "Usage: bash install.sh --token YOUR_TOKEN --url http://backend:8080"
    exit 1
fi

if [ -z "$BACKEND_URL" ]; then
    echo -e "${RED}❌ ERROR: --url is required${NC}"
    echo "Usage: bash install.sh --token YOUR_TOKEN --url http://backend:8080"
    exit 1
fi

# ─── Check Python3 installed ──────────────────────────────────────────────
echo -e "${YELLOW}[1/5] Checking Python3...${NC}"
if ! command -v python3 &>/dev/null; then
    echo -e "${YELLOW}Python3 not found — installing...${NC}"
    sudo apt-get update -qq
    sudo apt-get install -y python3 python3-pip
fi
echo -e "${GREEN}✅ Python3 found: $(python3 --version)${NC}"

# ─── Install pip dependencies ─────────────────────────────────────────────
echo -e "${YELLOW}[2/5] Installing Python dependencies...${NC}"
pip3 install --quiet psutil requests python-dotenv cryptography
echo -e "${GREEN}✅ Dependencies installed${NC}"

# ─── Create agent directory ───────────────────────────────────────────────
echo -e "${YELLOW}[3/5] Setting up agent directory...${NC}"
AGENT_DIR="/opt/cloudshadow-agent"
sudo mkdir -p $AGENT_DIR
sudo chown $USER:$USER $AGENT_DIR
echo -e "${GREEN}✅ Agent directory: $AGENT_DIR${NC}"

# ─── Create .env file with token ──────────────────────────────────────────
echo -e "${YELLOW}[4/5] Writing secure config...${NC}"
cat > $AGENT_DIR/.env <<EOF
# CLOUDSHADOW Agent Config — DO NOT SHARE THIS FILE
CLOUDSHADOW_TOKEN=${TOKEN}
CLOUDSHADOW_BACKEND_URL=${BACKEND_URL}
CLOUDSHADOW_INTERVAL=${INTERVAL}
CLOUDSHADOW_LOG_LEVEL=INFO
CLOUDSHADOW_TIMEOUT=10
CLOUDSHADOW_MAX_RETRIES=3
EOF

# Secure the .env file — only owner can read it
chmod 600 $AGENT_DIR/.env
echo -e "${GREEN}✅ Config written and secured (chmod 600)${NC}"

# ─── Create systemd service ───────────────────────────────────────────────
echo -e "${YELLOW}[5/5] Creating systemd service...${NC}"
sudo tee /etc/systemd/system/cloudshadow-agent.service > /dev/null <<EOF
[Unit]
Description=CLOUDSHADOW Monitoring Agent
After=network.target
Wants=network-online.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$AGENT_DIR
EnvironmentFile=$AGENT_DIR/.env
ExecStart=python3 $AGENT_DIR/agent.py
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# ─── Enable and start service ─────────────────────────────────────────────
sudo systemctl daemon-reload
sudo systemctl enable cloudshadow-agent
sudo systemctl start cloudshadow-agent

echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}   ✅ CLOUDSHADOW Agent installed and running!      ${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""
echo -e "  Backend URL : ${BLUE}${BACKEND_URL}${NC}"
echo -e "  Token       : ${YELLOW}${TOKEN:0:6}****${TOKEN: -4}${NC}"
echo -e "  Interval    : ${BLUE}${INTERVAL}s${NC}"
echo -e "  Config file : ${BLUE}${AGENT_DIR}/.env${NC}"
echo -e "  Log file    : ${BLUE}${AGENT_DIR}/cloudshadow-agent.log${NC}"
echo ""
echo -e "  Check status : ${YELLOW}sudo systemctl status cloudshadow-agent${NC}"
echo -e "  View logs    : ${YELLOW}sudo journalctl -u cloudshadow-agent -f${NC}"
echo -e "  Stop agent   : ${YELLOW}sudo systemctl stop cloudshadow-agent${NC}"
echo ""