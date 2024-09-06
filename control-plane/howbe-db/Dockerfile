FROM node:20

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . /app

RUN npm run build

CMD [ "node", "dist/main.js" ]