# 1. Build and start all containers
docker-compose up --build

# 2. Run in background (detached mode)
docker-compose up --build -d

# 3. Check running containers
docker ps

# 4. Check backend logs
docker logs cloudshadow-backend -f

# 5. Check mysql logs
docker logs cloudshadow-mysql -f