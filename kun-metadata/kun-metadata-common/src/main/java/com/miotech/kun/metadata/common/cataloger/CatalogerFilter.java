package com.miotech.kun.metadata.common.cataloger;

public class CatalogerFilter {

    private final CatalogerWhiteList whiteList;

    private final CatalogerBlackList blackList;

    public CatalogerFilter(CatalogerWhiteList whiteList, CatalogerBlackList blackList) {
        if(whiteList == null){
            whiteList = new CatalogerWhiteList();
        }
        if(blackList == null){
            blackList = new CatalogerBlackList();
        }
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    /**
     * return ture if database should retain
     *
     * @param database
     * @return
     */
    public boolean filterDatabase(String database) {
        if (whiteList.contains(database)) {
            return true;
        }

        if (blackList.contains(database)) {
            return false;
        }
        return true;
    }

    /**
     * return ture if table should retain
     *
     * @param database
     * @return
     */
    public boolean filterTable(String database, String table) {
        if (whiteList.contains(database)) {
            return true;
        }
        if (blackList.contains(database)) {
            return false;
        }
        StringBuilder fullPathBuilder = new StringBuilder();
        fullPathBuilder.append(database);
        String fullPath = fullPathBuilder.append(":").append(table).toString();
        if (whiteList.contains(fullPath)) {
            return true;
        }
        if (blackList.contains(fullPath)) {
            return false;
        }
        return true;
    }
}
