# -- Frontend Dockerfile (React + Vite -> nginx) ---------------------------------
# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files for layer caching (so we don't reinstall node_modules unless packages change)
COPY package*.json ./
# Clean install dependencies in headless mode for consistent builds
RUN npm ci --silent

# Copy all source files and trigger the build process (outputs static files to /app/dist)
COPY . .
RUN npm run build

# -- Stage 2: Minimal nginx runtime to serve the built static assets ------------
FROM nginx:1.27-alpine

# Remove default nginx welcome config to prevent conflicts with our custom configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy build output (static HTML/CSS/JS) to nginx directory and put custom routing configuration
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose standard HTTP port
EXPOSE 80

# Check nginx status periodically to ensure it is running and responding
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://127.0.0.1:80 | grep -q "SMS\|html" || exit 1

# Start nginx in the foreground (daemon off) so the Docker container remains active
CMD ["nginx", "-g", "daemon off;"]
