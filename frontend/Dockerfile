FROM nginx:1.27-alpine

WORKDIR /usr/share/nginx/html

RUN apk add --no-cache curl

COPY . /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
