import { defineConfig } from 'cypress';
import dotenv from 'dotenv';

dotenv.config({ path: '.env.test' });
dotenv.config();

const DEFAULT_AUTH_USERNAME = 'liyuzhang';
const DEFAULT_AUTH_PASSWORD = 'misaya.159';
const DEFAULT_HTTPS_HOST = 'https://dev.localhost.com';
const DEFAULT_HTTPS_PORT = 9801;

const { AUTH_PASSWORD, AUTH_USERNAME, HTTPS_HOST, HTTPS_PORT } = process.env;

export default defineConfig({
  env: {
    AUTH_USERNAME: AUTH_USERNAME || DEFAULT_AUTH_USERNAME,
    AUTH_PASSWORD: AUTH_PASSWORD || DEFAULT_AUTH_PASSWORD,
  },
  e2e: {
    baseUrl: `${HTTPS_HOST || DEFAULT_HTTPS_HOST}:${HTTPS_PORT || DEFAULT_HTTPS_PORT}`,
    setupNodeEvents() {
      // implement node event listeners here
    },
  },
});
