# ─── Stage 1: Build ───────────────────────────────────────────────
FROM node:20-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files first (layer caching — only reinstalls if changed)
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci

# Copy all source files
COPY . .

# Build the production bundle
RUN npm run build

# ─── Stage 2: Serve ───────────────────────────────────────────────
FROM nginx:alpine

# Copy built files from builder stage to nginx html folder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy our custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]