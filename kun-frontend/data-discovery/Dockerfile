# FROM metowolf/nginx:1.17.0
FROM nginx:1.17-alpine-perl

WORKDIR /app
COPY build/nginx.conf /etc/nginx/nginx.conf
COPY build/dist /app/dist
