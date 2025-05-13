FROM node:latest as build-stage
WORKDIR /app
COPY package*.json ./
COPY patches/* ./patches/

RUN npm install
RUN npx patch-package
COPY ./ .
RUN npm run build

FROM nginx as production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
COPY nginx.conf /etc/nginx/nginx.conf
