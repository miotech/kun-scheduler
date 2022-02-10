package com.miotech.kun.metadata.common.cataloger;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * member contains in CataLoger blacklist will be filtered out
 * from extract metadata
 *
 */
public class CatalogerBlackList {

    private final Set<String> blackList;

    public CatalogerBlackList(){
        blackList = new HashSet<>();
    }

    public CatalogerBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public void addMember(String member){
        blackList.add(member);
    }

    public void removeMember(String member){
        blackList.remove(member);
    }

    public boolean contains(String key) {
        return blackList.contains(key);
    }

    public Set<String> getBlackList(){
        return ImmutableSet.copyOf(blackList);
    }

    public Integer size(){
        return blackList.size();
    }

    public CatalogerBlackList merge(CatalogerBlackList other){
        HashSet<String> newBackList = new HashSet<>(blackList);
        newBackList.addAll(other.getBlackList());
        return new CatalogerBlackList(newBackList);
    }
}
