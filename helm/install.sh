# Development (minikube)
helm upgrade --install cloudshadow ./helm/cloudshadow \
  -f helm/cloudshadow/values-dev.yaml

# Production (AWS EKS)
helm upgrade --install cloudshadow ./helm/cloudshadow \
  -f helm/cloudshadow/values-prod.yaml \
  --set publicUrl=http://YOUR-EXTERNAL-IP \
  --set mysql.rootPassword=YOUR-SECURE-PASSWORD \
  --set backend.jwtSecret=YOUR-SECURE-SECRET

# Uninstall
helm uninstall cloudshadow -n cloudshadow

# Check status
helm ls -n cloudshadow
helm status cloudshadow -n cloudshadow