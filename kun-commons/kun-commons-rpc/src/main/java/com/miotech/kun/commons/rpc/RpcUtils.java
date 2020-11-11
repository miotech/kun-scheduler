package com.miotech.kun.commons.rpc;

import org.apache.dubbo.common.utils.NetUtils;

public class RpcUtils {


    public static Integer getRandomPort(){
        return NetUtils.getAvailablePort(-1);
    }

}
