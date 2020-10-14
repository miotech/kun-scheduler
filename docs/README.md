# Kun Documentation Repository

This website is built using [Docusaurus 2](https://v2.docusaurus.io/), a modern static website generator.

### Installation

```
$ yarn
```

### Local Development

```
$ yarn start
```

This command starts a local development server and open up a browser window. Most changes are reflected live without having to restart the server.

### Build

```
$ yarn build
```

This command generates static content into the `build` directory and can be served using any static contents hosting service.

### Deployment

```
$ GIT_USER=<Your GitHub username> USE_SSH=true yarn deploy
```

If you are using GitHub pages for hosting, this command is a convenient way to build the website and push to the `gh-pages` branch.

## API Documentation Development Guide

Kun Documentation adopts [Redoc](https://redocly.github.io/redoc/) as the rendering engine for its OpenAPI specification (also compatible with Swagger).
The spec file to be rendered is [`swagger/swagger.json`](./swagger/swagger.json),
and all you need to do is to modify this file which will be finally rendered by Docusaurus.

To start development, follows these steps:

1. Setup local development environment.
2. Bootstrap local dev-server
3. Modify [`swagger/swagger.json`](./swagger/swagger.json) to your satisfaction.

Reference steps:

```bash
# Install dependencies
$ yarn install

# Boot up local development environment
$ yarn start

# Then you will see the project running at http://localhost:3000/.
# Doing modifications on swagger/swagger.json will trigger hot-reload by webpack
# so you will see the changes immediately.
```
**(Optional)** We also recommend using [Swagger Editor](https://github.com/swagger-api/swagger-editor) inside your browser
to do the modification by running a docker container:

```
$ docker pull swaggerapi/swagger-editor
$ docker run --rm -p 8080:8080 -v $(pwd)/swagger/:/tmp -e SWAGGER_FILE=/tmp/swagger.json swaggerapi/swagger-editor
```

Then open your browser and visit [http://localhost:8080](http://localhost:8080) to start editing.

