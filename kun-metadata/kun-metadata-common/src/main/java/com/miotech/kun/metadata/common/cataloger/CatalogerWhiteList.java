package com.miotech.kun.metadata.common.cataloger;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

public class CatalogerWhiteList {
    private final Set<String> whiteList;

    public CatalogerWhiteList(){
        whiteList = new HashSet<>();
    }

    public CatalogerWhiteList(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    public void addMember(String member){
        whiteList.add(member);
    }

    public void removeMember(String member){
        whiteList.remove(member);
    }

    public boolean contains(String database) {
        return whiteList.contains(database);
    }

    public Set<String> getWhiteList() {
        return ImmutableSet.copyOf(whiteList);
    }

    public Integer size(){
        return whiteList.size();
    }

    public CatalogerWhiteList merge(CatalogerWhiteList other){
        Set<String> newWhiteList = new HashSet<>(whiteList);
        newWhiteList.addAll(other.getWhiteList());
        return new CatalogerWhiteList(newWhiteList);
    }
}
