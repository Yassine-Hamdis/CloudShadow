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
                    image: cloudshadow/docker-agent:latest
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
                # ══════════════════════════════════════════════════
                # ☸️  KUBERNETES — CloudShadow Sidecar Agent
                # ══════════════════════════════════════════════════
                \s
                # ── Step 1: Create a Secret for the token ──
                kubectl create secret generic cloudshadow-secret \\
                  --from-literal=server-token="%s"
                \s
                # ── Step 2: Add agent as sidecar in your Pod spec ──
                # Add this block under "containers:" in your
                # existing deployment yaml:
                \s
                  - name: cloudshadow-agent
                    image: cloudshadow/docker-agent:latest
                    env:
                      - name: SERVER_TOKEN
                        valueFrom:
                          secretKeyRef:
                            name: cloudshadow-secret
                            key: server-token
                      - name: BACKEND_URL
                        value: "%s"
                      - name: MONITOR_CONTAINER
                        value: "your-container-name"
                      - name: INTERVAL
                        value: "20"
                    volumeMounts:
                      - name: docker-sock
                        mountPath: /var/run/docker.sock
                \s
                # ── Step 3: Add volume to your Pod spec ──
                # Add this block under "volumes:" section:
                \s
                  volumes:
                    - name: docker-sock
                      hostPath:
                        path: /var/run/docker.sock
                        type: Socket
                \s
                # ── Step 4: Apply your updated manifest ──
                kubectl apply -f your-deployment.yaml
                \s
                # ── Step 5: Verify agent is running ──
                kubectl get pods
                kubectl logs <your-pod-name> -c cloudshadow-agent
                \s
                # ── Step 6: View live logs ──
                kubectl logs -f <your-pod-name> -c cloudshadow-agent
                \s
                # ══════════════════════════════════════════════════
                # ⚙️  Useful commands:
                # Logs   → kubectl logs -f <pod> -c cloudshadow-agent
                # Status → kubectl describe pod <pod-name>
                # Delete → kubectl delete secret cloudshadow-secret
                # ══════════════════════════════════════════════════
                """,
                token,
                backendUrl
        );
    }
}