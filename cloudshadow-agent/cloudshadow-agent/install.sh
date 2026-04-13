#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────
# CLOUDSHADOW Agent — Linux Install Script (FIXED)
# ─────────────────────────────────────────────────────────────────────────

set -e

# ─── Colors ───────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
echo "═══════════════════════════════════════════════════"
echo "        🌩️ CLOUDSHADOW AGENT INSTALLER (FIXED)      "
echo "═══════════════════════════════════════════════════"
echo -e "${NC}"

# ─── Args ────────────────────────────────────────────────────────────────
TOKEN=""
BACKEND_URL=""
INTERVAL=20

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --token) TOKEN="$2"; shift ;;
        --url) BACKEND_URL="$2"; shift ;;
        --interval) INTERVAL="$2"; shift ;;
        *) echo -e "${RED}Unknown parameter: $1${NC}"; exit 1 ;;
    esac
    shift
done

if [ -z "$TOKEN" ] || [ -z "$BACKEND_URL" ]; then
    echo -e "${RED}❌ --token and --url are required${NC}"
    exit 1
fi

# ─── Step 1: System dependencies ─────────────────────────────────────────
echo -e "${YELLOW}[1/5] Installing system dependencies...${NC}"
apt update -qq
apt install -y python3 python3-pip python3-venv curl

# ─── Step 2: Setup directory ─────────────────────────────────────────────
AGENT_DIR="/opt/cloudshadow-agent"

echo -e "${YELLOW}[2/5] Setting up directory...${NC}"
mkdir -p $AGENT_DIR
cp -r . $AGENT_DIR 2>/dev/null || true
cd $AGENT_DIR

# fix permissions
chown -R $USER:$USER $AGENT_DIR

# ─── Step 3: Create virtual environment ──────────────────────────────────
echo -e "${YELLOW}[3/5] Creating virtual environment...${NC}"
python3 -m venv venv

# upgrade pip
$AGENT_DIR/venv/bin/pip install --upgrade pip

# install dependencies
$AGENT_DIR/venv/bin/pip install -r requirements.txt

echo -e "${GREEN}✅ Dependencies installed in venv${NC}"

# ─── Step 4: Create .env ─────────────────────────────────────────────────
echo -e "${YELLOW}[4/5] Writing configuration...${NC}"

cat > $AGENT_DIR/.env <<EOF
CLOUDSHADOW_TOKEN=${TOKEN}
CLOUDSHADOW_BACKEND_URL=${BACKEND_URL}
CLOUDSHADOW_INTERVAL=${INTERVAL}
CLOUDSHADOW_LOG_LEVEL=INFO
CLOUDSHADOW_TIMEOUT=10
CLOUDSHADOW_MAX_RETRIES=3
EOF

chmod 600 $AGENT_DIR/.env

# ─── Step 5: systemd service ─────────────────────────────────────────────
echo -e "${YELLOW}[5/5] Creating systemd service...${NC}"

cat > /etc/systemd/system/cloudshadow-agent.service <<EOF
[Unit]
Description=CLOUDSHADOW Monitoring Agent
After=network.target
Wants=network-online.target

[Service]
Type=simple
User=root
WorkingDirectory=$AGENT_DIR
EnvironmentFile=$AGENT_DIR/.env
ExecStart=$AGENT_DIR/venv/bin/python $AGENT_DIR/agent.py
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# ─── Start service ───────────────────────────────────────────────────────
systemctl daemon-reload
systemctl enable cloudshadow-agent
systemctl restart cloudshadow-agent

echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}   ✅ CLOUDSHADOW Agent installed successfully!     ${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""
echo -e "Backend URL : ${BLUE}${BACKEND_URL}${NC}"
echo -e "Interval    : ${BLUE}${INTERVAL}s${NC}"
echo -e "Config      : ${BLUE}${AGENT_DIR}/.env${NC}"
echo ""
echo -e "Status: ${YELLOW}systemctl status cloudshadow-agent${NC}"
echo -e "Logs  : ${YELLOW}journalctl -u cloudshadow-agent -f${NC}"