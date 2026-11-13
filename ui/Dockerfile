FROM node:22-alpine

WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3000
ENV API_BASE_URL=http://host.docker.internal:8080

COPY package.json server.js ./
COPY public ./public

EXPOSE 3000
CMD ["npm", "start"]
