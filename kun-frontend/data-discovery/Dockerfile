FROM node:12.13.0-alpine as builder
# FROM node:12.13.0

WORKDIR /app
ADD package.json yarn.lock .npmrc ./
RUN yarn install --ignore-optional

ADD src src
ADD public public
ADD tsconfig.json .eslint* .prettierrc.js .prettierignore .stylelintrc.js ./
RUN npm run build

# FROM metowolf/nginx:1.17.0
FROM nginx:1.17-alpine-perl
WORKDIR /app
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /app/dist
