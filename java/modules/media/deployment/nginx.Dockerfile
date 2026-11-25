# The minimal CDN edge: nginx with the njs module that verifies the download tokens
# (download_token.js) and proxies /cdn/ reads to the media API.
#
# The official image runs envsubst over /etc/nginx/templates on start, so nginx.conf stays a template
# whose `${MEDIA_*}` variables come from the environment. The output directory is /etc/nginx because
# the edge needs the main config (load_module and js_import live there), and the filter keeps
# envsubst away from the nginx runtime variables of the config.

FROM nginx:1.31-alpine

RUN apk add --no-cache nginx-module-njs

COPY nginx.conf /etc/nginx/templates/nginx.conf.template
COPY download_token.js /etc/nginx/njs/download_token.js

ENV NGINX_ENVSUBST_OUTPUT_DIR=/etc/nginx
ENV NGINX_ENVSUBST_FILTER=MEDIA_
