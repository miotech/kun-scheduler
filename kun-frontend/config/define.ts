import fs from 'fs';
import path from 'path';
import yaml from 'js-yaml';

const root = (fn: string) => path.resolve(__dirname, '../', fn);

const { USE_MOCK_PROFILE, NODE_ENV, NO_MOCK } = process.env;

let mockServiceCodeConfig: any = {};
const isProduction = (NODE_ENV || '').toLowerCase() === 'production';

try {
  if (process.env.USE_MOCK_PROFILE) {
    if (fs.existsSync(root(`mock/use-mock.config.${USE_MOCK_PROFILE}.yaml`))) {
      console.log(`Using mock config file: mock/use-mock.config.${USE_MOCK_PROFILE}.yaml`);
      mockServiceCodeConfig = yaml.safeLoad(
        // @ts-ignore
        fs.readFileSync(root(`mock/use-mock.config.${USE_MOCK_PROFILE}.yaml`), 'utf8'),
      );
    }
  } else if (fs.existsSync(root('mock/use-mock.config.yaml'))) {
    console.log('Using mock config file: mock/use-mock.config.yaml');
    mockServiceCodeConfig = yaml.safeLoad(
      // @ts-ignore
      fs.readFileSync(root('mock/use-mock.config.yaml'), 'utf8'),
    );
  } else if (fs.existsSync(root('mock/use-mock.config.default.yaml'))) {
    console.log('Using mock config file: mock/use-mock.config.default.yaml');
    mockServiceCodeConfig = yaml.safeLoad(
      // @ts-ignore
      fs.readFileSync(root('mock/use-mock.config.default.yaml', { encoding: 'utf8' })),
    );
  }
} catch (err) {
  console.log('Cannot find use-mock related config file, ignoring all');
}

export const define = {
  LOG_LEVEL: 'debug',
  USE_MOCK_CONFIG: JSON.stringify(mockServiceCodeConfig),
  DISABLE_MOCK: !!NO_MOCK,
  PRODUCTION_MODE: isProduction,
};
