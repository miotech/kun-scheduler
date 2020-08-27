import kndb, { KnDBObject } from 'kndb';
import path from 'path';

const root = (fn: string) => path.resolve(__dirname, '../../', fn);

export class MockDbUtil {
  static dbType = 'local';

  static setDBType(type: string) {
    this.dbType = type;
  }

  static getDatabaseInstance(dbName: string): KnDBObject {
    return kndb.getDB(`${this.dbType}-${dbName}`, {
      position: root(`kndb/${this.dbType}`),
    });
  }
}
