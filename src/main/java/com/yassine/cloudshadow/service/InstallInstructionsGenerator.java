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
                .normalServer(buildNormalServerInstructions(
                        token, backendUrl))
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
                # ── Set these environment variables on your server ──
                export CLOUDSHADOW_TOKEN="%s"
                export CLOUDSHADOW_BACKEND_URL="%s"
                export CLOUDSHADOW_INTERVAL="20"
                export CLOUDSHADOW_LOG_LEVEL="INFO"
                \s
                # ── To make them permanent (Linux) ──
                echo 'export CLOUDSHADOW_TOKEN="%s"' >> ~/.bashrc
                echo 'export CLOUDSHADOW_BACKEND_URL="%s"' >> ~/.bashrc
                source ~/.bashrc
                """,
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
                # ── Step 2: One-command install ( RECOMMENDED )──
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
                # ⚙️ Useful commands:
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
    // DOCKER — Phase 2 (preview instructions)
    // ─────────────────────────────────────────────────────────────────────
    private String buildDockerInstructions(
            String token, String backendUrl) {

        return String.format("""
                # ══════════════════════════════════════════════════
                # 🐳 DOCKER — Container Agent (Phase 2)
                # ══════════════════════════════════════════════════
                \s
                # ── Run agent as Docker container ──
                docker run -d \\
                  --name cloudshadow-agent \\
                  --restart unless-stopped \\
                  -e CLOUDSHADOW_TOKEN="%s" \\
                  -e CLOUDSHADOW_BACKEND_URL="%s" \\
                  -e CLOUDSHADOW_INTERVAL="20" \\
                  -v /var/run/docker.sock:/var/run/docker.sock \\
                  cloudshadow/agent:latest
                \s
                # ── View agent logs ──
                docker logs cloudshadow-agent -f
                \s
                # ── Stop agent ──
                docker stop cloudshadow-agent
                \s
                # ══════════════════════════════════════════════════
                # ⚠️  Docker agent coming in Phase 2
                # Use Normal Server agent for now
                # ══════════════════════════════════════════════════
                """,
                token,
                backendUrl
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // KUBERNETES — Phase 3 (preview instructions)
    // ─────────────────────────────────────────────────────────────────────
    private String buildKubernetesInstructions(
            String token, String backendUrl) {

        return String.format("""
                # ══════════════════════════════════════════════════
                # ☸️  KUBERNETES — DaemonSet Agent (Phase 3)
                # ══════════════════════════════════════════════════
                \s
                # ── Step 1: Create secret for token ──
                kubectl create secret generic cloudshadow-secret \\
                  --from-literal=token="%s"
                \s
                # ── Step 2: Apply DaemonSet ──
                curl -O %s/releases/latest/download/k8s-daemonset.yaml
                \s
                # ── Edit k8s-daemonset.yaml ──
                # Replace BACKEND_URL with: %s
                \s
                kubectl apply -f k8s-daemonset.yaml
                \s
                # ── Step 3: Verify DaemonSet running on all nodes ──
                kubectl get daemonset cloudshadow-agent -n monitoring
                kubectl get pods -n monitoring
                \s
                # ── Step 4: View logs ──
                kubectl logs -l app=cloudshadow-agent -n monitoring -f
                \s
                # ══════════════════════════════════════════════════
                # ⚠️  Kubernetes agent coming in Phase 3
                # Use Normal Server agent for now
                # ══════════════════════════════════════════════════
                """,
                token,
                agentDownloadUrl,
                backendUrl
        );
    }
}