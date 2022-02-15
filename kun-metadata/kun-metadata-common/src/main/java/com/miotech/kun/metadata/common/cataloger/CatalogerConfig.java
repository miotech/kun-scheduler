package com.miotech.kun.metadata.common.cataloger;

public class CatalogerConfig {

    private final CatalogerBlackList blackList;

    private final CatalogerWhiteList whiteList;

    public CatalogerConfig(CatalogerWhiteList whiteList,CatalogerBlackList blackList) {
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    public CatalogerWhiteList getWhiteList() {
        return whiteList;
    }

    public CatalogerBlackList getBlackList() {
        return blackList;
    }

    public static CatalogerConfigBuilder newBuilder(){
        return new CatalogerConfigBuilder();
    }

    public static final class CatalogerConfigBuilder {
        private CatalogerBlackList blackList;
        private CatalogerWhiteList whiteList;

        private CatalogerConfigBuilder() {
        }

        public CatalogerConfigBuilder withBlackList(CatalogerBlackList blackList) {
            this.blackList = blackList;
            return this;
        }

        public CatalogerConfigBuilder withWhiteList(CatalogerWhiteList whiteList) {
            this.whiteList = whiteList;
            return this;
        }

        public CatalogerConfig build() {
            return new CatalogerConfig(whiteList, blackList);
        }
    }
}
