FROM node:22-alpine AS build
WORKDIR /app

COPY package.json ./
RUN npm install && chmod -R +x node_modules/.bin/

COPY . .
RUN node node_modules/vite/bin/vite.js build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
