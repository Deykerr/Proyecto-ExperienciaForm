# syntax=docker/dockerfile:1.7

FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund

COPY angular.json tsconfig*.json ./
COPY public ./public
COPY src ./src
RUN npm run build -- --configuration=production

FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build --chown=nginx:nginx /app/dist/botica_front/browser/ /usr/share/nginx/html/

USER nginx
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q --spider http://127.0.0.1:8080/health || exit 1

ENTRYPOINT ["nginx", "-g", "daemon off;"]
