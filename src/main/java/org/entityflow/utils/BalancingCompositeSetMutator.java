package org.entityflow.utils;

import org.apache.commons.collections4.set.CompositeSet;
import org.entityflow.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A mutator for composite sets that balances added objects by placing them in the smallest composite set.
 *
 * Will work reasonably well if objects are added as well as removed at a similar rate.
 * Will result in skewed distributions if objects are mainly first added and then removed,
 * as no re-balancing is done when an object is removed.
 */
public final class BalancingCompositeSetMutator<T> implements CompositeSet.SetMutator<T> {

    @Override public boolean add(CompositeSet<T> composite, List<Set<T>> sets, T obj) {
        if (!composite.contains(obj) && !sets.isEmpty()) {
            // Add to the smallest set
            return findSmallestComponentSet(sets).add(obj);
        }
        else {
            return false;
        }
    }

    @Override public boolean addAll(CompositeSet<T> composite,
                                    List<Set<T>> sets,
                                    Collection<? extends T> coll) {
        boolean changed = false;

        // Add objects one by one
        for (T obj: coll) {
            changed = changed || add(composite, sets, obj);
        }

        return changed;
    }

    @Override public void resolveCollision(CompositeSet<T> comp,
                                           Set<T> existing,
                                           Set<T> added,
                                           Collection<T> intersects) {
        // Remove intersecting objects from the larger collection
        Set<T> largest = existing;
        if (added.size() > largest.size()) largest = added;
        largest.removeAll(intersects);
    }

    private Set<T> findSmallestComponentSet(List<Set<T>> sets) {
        Set<T> smallestSet = null;
        int smallestSetSize = 0;

        for (Set<T> set : sets) {
            final int setSize = set.size();
            if (smallestSet == null || setSize < smallestSetSize) {
                // New smallest set found
                smallestSet = set;
                smallestSetSize = setSize;
            }
        }
        return smallestSet;
    }
}
