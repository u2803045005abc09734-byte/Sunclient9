FROM node:20-alpine
WORKDIR /app
COPY package.json server.mjs ./
ENV PORT=8787
ENV DATA_DIR=/data/capes
VOLUME ["/data"]
EXPOSE 8787
CMD ["npm", "start"]
