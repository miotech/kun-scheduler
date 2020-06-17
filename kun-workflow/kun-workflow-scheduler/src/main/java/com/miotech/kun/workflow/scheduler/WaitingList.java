package com.miotech.kun.workflow.scheduler;

import java.util.*;

public class WaitingList<W, T> {
    private final Map<W, Collection<T>> waiterToTarget;
    private final Map<T, Collection<W>> targetToWaiter;
    private final List<W> nextToPop;

    public WaitingList() {
        this.waiterToTarget = new HashMap<>();
        this.targetToWaiter = new HashMap<>();
        this.nextToPop = new ArrayList<>();
    }

    /**
     * 增加等待关系
     * @param waiter
     * @param target
     */
    public void addWait(W waiter, Collection<T> target) {
        if (target.isEmpty()) {
            nextToPop.add(waiter);
            return;
        }

        if (!waiterToTarget.containsKey(waiter)) {
            waiterToTarget.put(waiter, target);
            for (T t : target) {
                if (!targetToWaiter.containsKey(t)) {
                    targetToWaiter.put(t, new ArrayList<>());
                }
                targetToWaiter.get(t).add(waiter);
            }
        }
    }

    /**
     * 删除等待关系
     * @param target
     */
    public void removeWait(T target) {
        if (targetToWaiter.containsKey(target)) {
            for (W w : targetToWaiter.get(target)) {
                Collection<T> waitTo = waiterToTarget.get(w);
                waitTo.remove(target);
                if (waitTo.isEmpty()) {
                    nextToPop.add(w);
                    waiterToTarget.remove(w);
                }
            }
            targetToWaiter.remove(target);
        }
    }

    /**
     * 弹出当前不再等待的对象
     * @return
     */
    public List<W> pop() {
        if (!nextToPop.isEmpty()) {
            List<W> ret = new ArrayList<>(nextToPop);
            nextToPop.clear();
            return ret;
        } else {
            return Collections.emptyList();
        }
    }
}
