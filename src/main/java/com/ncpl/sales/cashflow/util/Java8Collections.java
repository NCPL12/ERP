package com.ncpl.sales.cashflow.util;
import java.util.*;
/** Java 8 equivalents of immutable collection factories used by the analyzer. */
public final class Java8Collections {
    private Java8Collections() {}
    @SuppressWarnings("unchecked") public static <K,V> Map<K,V> map(Object... pairs) {
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Expected key/value pairs");
        Map<K,V> result = new LinkedHashMap<>();
        for (int i=0; i<pairs.length; i+=2) {
            K key=(K)Objects.requireNonNull(pairs[i]); V value=(V)Objects.requireNonNull(pairs[i+1]);
            if (result.containsKey(key)) throw new IllegalArgumentException("Duplicate key");
            result.put(key,value);
        }
        return Collections.unmodifiableMap(result);
    }
    @SafeVarargs public static <T> List<T> list(T... items) { return copyList(Arrays.asList(items)); }
    public static <T> List<T> copyList(Collection<? extends T> items) {
        List<T> result=new ArrayList<>(); for(T item:items) result.add(Objects.requireNonNull(item));
        return Collections.unmodifiableList(result);
    }
    @SafeVarargs public static <T> Set<T> set(T... items) {
        Set<T> result=new LinkedHashSet<>(); for(T item:items) if(!result.add(Objects.requireNonNull(item))) throw new IllegalArgumentException("Duplicate item");
        return Collections.unmodifiableSet(result);
    }
}
