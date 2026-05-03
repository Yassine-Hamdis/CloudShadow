package com.yassine.cloudshadow.service;


import com.yassine.cloudshadow.dto.response.InstallInstructions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstallInstructionsGenerator {

    @Value("${cloudshadow.agent.download-url:https://github.com/Yassine-Hamdis/cloudshadow-agent}")
    private String agentDownloadUrl;

    /**
     * Generates complete installation instructions for all platforms.
     * Called when a new server is registered.
     *
     * @param token       → The server token (used for agent auth)
     * @param backendUrl  → The backend URL (where agent sends metrics)
     * @param serverName  → The server name (for display only)
     */
    public InstallInstructions generate(
            String token,
            String backendUrl,
            String serverName) {

        return InstallInstructions.builder()
                .tokenReminder(buildTokenReminder(token, serverName))
                .envVariables(buildEnvVariables(token, backendUrl))
                .normalServer(buildNormalServerInstructions(token, backendUrl))
                .docker(buildDockerInstructions(token, backendUrl))
                .kubernetes(buildKubernetesInstructions(token, backendUrl))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TOKEN REMINDER
    // ─────────────────────────────────────────────────────────────────────
    private String buildTokenReminder(String token, String serverName) {
        return String.format("""
                ⚠️  IMPORTANT — Save your server token now!
                ════════════════════════════════════════════
                Server Name : %s
                Token       : %s
                ════════════════════════════════════════════
                This token identifies your server to CLOUDSHADOW.
                It will NOT be shown again in full.
                Store it securely before closing this window.
                """,
                serverName,
                token
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENVIRONMENT VARIABLES
    // ─────────────────────────────────────────────────────────────────────
    private String buildEnvVariables(String token, String backendUrl) {
        return String.format("""
                # ── Normal Server Agent Variables ──────────────────
                export CLOUDSHADOW_TOKEN="%s"
                export CLOUDSHADOW_BACKEND_URL="%s"
                export CLOUDSHADOW_INTERVAL="20"
                export CLOUDSHADOW_LOG_LEVEL="INFO"
                \s
                # ── To make them permanent (Linux) ──
                echo 'export CLOUDSHADOW_TOKEN="%s"' >> ~/.bashrc
                echo 'export CLOUDSHADOW_BACKEND_URL="%s"' >> ~/.bashrc
                source ~/.bashrc
                \s
                # ── Docker / Kubernetes Agent Variables ─────────────
                SERVER_TOKEN="%s"
                # Your unique server token (do not share this)
                \s
                BACKEND_URL="%s"
                # URL of your CloudShadow backend
                \s
                MONITOR_CONTAINER="your-container-name"
                # Name of the Docker container to monitor
                # Must match container_name in docker-compose
                # or the container name in Kubernetes
                \s
                INTERVAL="20"
                # How often to send metrics in seconds
                # Minimum recommended: 10 — Default: 20
                \s
                # ══════════════════════════════════════════════════
                # ⚠️  Security Notes:
                # → Never commit SERVER_TOKEN to version control
                # → Use Docker secrets or K8s secrets in production
                # → Regenerate token from dashboard if compromised
                # ══════════════════════════════════════════════════
                """,
                token,
                backendUrl,
                token,
                backendUrl,
                token,
                backendUrl
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // NORMAL SERVER — Python Agent
    // ─────────────────────────────────────────────────────────────────────
    private String buildNormalServerInstructions(
            String token, String backendUrl) {

        return String.format("""
                # ══════════════════════════════════════════════════
                # 🖥️  NORMAL SERVER — Python Agent Installation
                # ══════════════════════════════════════════════════
                \s
                # ── Step 1: Download the agent ──
                curl -LO %s/releases/latest/download/cloudshadow-agent.tar.gz
                tar -xzf cloudshadow-agent.tar.gz
                cd cloudshadow-agent
                \s
                # ── Step 2: One-command install ( RECOMMENDED ) ──
                bash install.sh \\
                  --token "%s" \\
                  --url "%s" \\
                  --interval 20
                \s
                # ── OR: Run manually without installing ──
                export CLOUDSHADOW_TOKEN="%s"
                export CLOUDSHADOW_BACKEND_URL="%s"
                python3 agent.py
                \s
                # ── Step 3: Verify agent is running ──
                sudo systemctl status cloudshadow-agent
                \s
                # ── Step 4: View live logs ──
                sudo journalctl -u cloudshadow-agent -f
                \s
                # ══════════════════════════════════════════════════
                # ⚙️  Useful commands:
                # Stop  → sudo systemctl stop cloudshadow-agent
                # Start → sudo systemctl start cloudshadow-agent
                # Logs  → sudo journalctl -u cloudshadow-agent -f
                # ══════════════════════════════════════════════════
                """,
                agentDownloadUrl,
                token,
                backendUrl,
                token,
                backendUrl
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // DOCKER — Sidecar Agent
    // ─────────────────────────────────────────────────────────────────────
    private String buildDockerInstructions(
            String token, String backendUrl) {

        return String.format("""
                # ══════════════════════════════════════════════════
                # 🐳  DOCKER — CloudShadow Sidecar Agent
                # ══════════════════════════════════════════════════
                \s
                # ── Step 1: Add agent to your docker-compose.yml ──
                # Copy and paste this block into your existing
                # docker-compose.yml under "services:":
                \s
                  cloudshadow-agent:
                    image: yacinham10/cloudshadow-docker-agent:latest
                    container_name: cloudshadow-agent
                    restart: unless-stopped
                    environment:
                      SERVER_TOKEN: "%s"
                      BACKEND_URL: "%s"
                      MONITOR_CONTAINER: "your-container-name"
                      INTERVAL: "20"
                    volumes:
                      - /var/run/docker.sock:/var/run/docker.sock
                    depends_on:
                      - your-container-name
                \s
                # ── Step 2: Replace placeholders ──
                # → Replace "your-container-name" with the name
                #   of the container you want to monitor
                #   Example: my-app, nginx, api-server
                \s
                # ── Step 3: Start the agent ──
                docker-compose up -d cloudshadow-agent
                \s
                # ── Step 4: Verify agent is running ──
                docker-compose ps cloudshadow-agent
                \s
                # ── Step 5: View live logs ──
                docker-compose logs -f cloudshadow-agent
                \s
                # ══════════════════════════════════════════════════
                # ⚙️  Useful commands:
                # Stop  → docker-compose stop cloudshadow-agent
                # Start → docker-compose start cloudshadow-agent
                # Logs  → docker-compose logs -f cloudshadow-agent
                # ══════════════════════════════════════════════════
                """,
                token,
                backendUrl
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // KUBERNETES — Sidecar Agent
    // ─────────────────────────────────────────────────────────────────────
    private String buildKubernetesInstructions(
            String token, String backendUrl) {

        return String.format("""
            # ══════════════════════════════════════════════════════
            # ☸️  KUBERNETES — CloudShadow Agent (DaemonSet)
            # ══════════════════════════════════════════════════════
            \s
            # ── How it works ─────────────────────────────────────
            # The DaemonSet runs ONE agent pod on EVERY node
            # in your cluster automatically.
            # All nodes report metrics under this one server entry.
            # New nodes joining the cluster get the agent automatically.
            \s
            # ── Prerequisites ─────────────────────────────────────
            # Make sure metrics-server is installed:
            kubectl top nodes
            # If command fails, install metrics-server first:
            kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
            \s
            # ── Step 1: Create monitoring namespace ───────────────
            kubectl create namespace monitoring
            \s
            # ── Step 2: Create secret with your server token ──────
            # This token is shared across all nodes
            kubectl create secret generic agent-secret \\
              --namespace monitoring \\
              --from-literal=server-token="%s"
            \s
            # ── Step 3: Apply RBAC permissions ────────────────────
            # Grants agent permission to read node/pod metrics
            curl -LO https://raw.githubusercontent.com/Yassine-Hamdis/CloudShadow/main/k8s/monitoring/rbac.yaml
            kubectl apply -f rbac.yaml
            \s
            # ── Step 4: Apply the DaemonSet ───────────────────────
            curl -LO https://raw.githubusercontent.com/Yassine-Hamdis/CloudShadow/main/k8s/monitoring/daemonset.yaml
            \s
            # ── Edit daemonset.yaml ───────────────────────────
            # Find the BACKEND_URL value and replace it:
            # BACKEND_URL: "%s"
            # (This is your CloudShadow backend public URL)
            \s
            kubectl apply -f daemonset.yaml
            \s
            # ── Step 5: Verify running on ALL nodes ───────────────
            # You should see ONE pod per node
            kubectl get daemonset -n monitoring
            kubectl get pods -n monitoring -l app=cloudshadow-agent -o wide
            # -o wide shows which NODE each pod is running on
            \s
            # ── Step 6: View live logs from all nodes ─────────────
            kubectl logs -l app=cloudshadow-agent -n monitoring -f
            \s
            # ── Step 7: Verify metrics are flowing ────────────────
            # Check logs for: ✅ Metrics sent successfully
            kubectl logs -l app=cloudshadow-agent -n monitoring --tail=20
            \s
            # ══════════════════════════════════════════════════════
            # ⚙️  Useful commands:
            # How many nodes?  → kubectl get nodes
            # Agent status     → kubectl get pods -n monitoring -o wide
            # Single node logs → kubectl logs <pod-name> -n monitoring -f
            # Restart agent    → kubectl rollout restart daemonset cloudshadow-agent -n monitoring
            # Delete agent     → kubectl delete daemonset cloudshadow-agent -n monitoring
            # Delete all       → kubectl delete namespace monitoring
            # ══════════════════════════════════════════════════════
            \s
            # ══════════════════════════════════════════════════════
            # ℹ️  Note:
            # All nodes in your cluster report metrics under
            # this single server entry in your dashboard.
            # The agent automatically runs on new nodes
            # when they join your cluster.
            # ══════════════════════════════════════════════════════
            """,
                token,
                backendUrl
        );
    }
}