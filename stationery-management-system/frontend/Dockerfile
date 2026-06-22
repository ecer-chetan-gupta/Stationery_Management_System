# -- Frontend Dockerfile (React + Vite -> nginx) ---------------------------------
# Stage 1: Build React app
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files for layer caching
COPY package*.json ./
RUN npm ci --silent

# Copy source and build
COPY . .
RUN npm run build

# -- Stage 2: Serve with nginx --------------------------------------------------
FROM nginx:1.27-alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy built assets and our custom nginx config
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://127.0.0.1:80 | grep -q "SMS\|html" || exit 1

CMD ["nginx", "-g", "daemon off;"]
