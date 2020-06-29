import Mock from 'mockjs';
import FlakeId from "flakeid";
import { MockDbUtil } from '../mock-commons/utils/mock-db-util';

import dataDevelopmentMockAPIs from './data-development';
import credentialMockAPIs from './credential';

const flakeId = new FlakeId({
  mid: 42,
  timeOffset: 1594003540433,
});

if (process.env?.USE_TEST_DB === 'true') {
  MockDbUtil.setDBType('test');
}

// @ts-ignore
Mock.Random.extend({
  flakeId(): string {
    return flakeId.gen();
  },
});

export default {
  ...dataDevelopmentMockAPIs,
  ...credentialMockAPIs,
};
