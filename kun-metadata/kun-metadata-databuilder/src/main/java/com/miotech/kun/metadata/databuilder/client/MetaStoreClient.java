package com.miotech.kun.metadata.databuilder.client;

import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;

public class MetaStoreClient {

    public static HiveMetaStoreClient getClient(String metaStoreUris) {
        HiveConf conf = new HiveConf();
        conf.set("hive.metastore.uris", metaStoreUris);
        try {
            return new HiveMetaStoreClient(conf);
        } catch (MetaException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public static void close(HiveMetaStoreClient client) {
        if (client != null) {
            client.close();
        }
    }

}
