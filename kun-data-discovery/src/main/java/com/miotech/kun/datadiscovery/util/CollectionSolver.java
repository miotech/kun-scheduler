package com.miotech.kun.datadiscovery.util;

import com.clearspring.analytics.util.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-22 10:16
 **/
public class CollectionSolver<T> {
    private Collection<T> old;
    private Collection<T> fresh;

    public CollectionSolver(Collection<T> old, Collection<T> fresh) {
        this.old = CollectionUtils.isEmpty(old) ? Lists.newArrayList() : old;
        this.fresh = CollectionUtils.isEmpty(fresh) ? Lists.newArrayList() : fresh;
    }

    public Collection<T> subtractRemove() {
        return CollectionUtils.subtract(old, fresh);
    }

    public Collection<T> intersection() {
        return CollectionUtils.intersection(old, fresh);
    }

    public Collection<T> subtractAdd() {
        return CollectionUtils.subtract(fresh, intersection());
    }


}
