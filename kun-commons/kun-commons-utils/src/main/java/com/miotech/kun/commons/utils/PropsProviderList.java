package com.miotech.kun.commons.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PropsProviderList extends ArrayList<PropsProvider> {

    private final MapProps runTimeProps;

    public PropsProviderList(){
        super();
        runTimeProps = new MapProps(new HashMap<>());
        super.add(runTimeProps);
    }

    public void put(String key,Object value){
        runTimeProps.put(key,value);
    }

    @Override
    public boolean add(PropsProvider propsProvider){
        add(1,propsProvider);
        return true;
    }

}
