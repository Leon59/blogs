package io.kyoto.pillar.lcs.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Leon
 */
public class MapUtils {
    /**
     * *
     * @param list *
     * @param keyMapper *
     * @param <K> *
     * @param <V> *
     * @return
     */
    public static <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> keyMapper) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().filter(Objects::nonNull).filter(obj -> Objects.nonNull(keyMapper.apply(obj))).collect(Collectors.toMap(keyMapper, Function.identity(), oldMerger()));
    }

    /**
     * list通过key group by 生成 map *
     * @param list list *
     * @param keyMapper key *
     * @param <K> k *
     * @param <V> v *
     * @return map
     */
    public static <K, V> Map<K, List<V>> listGroup(List<V> list, Function<V, K> keyMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * list通过key group by 生成 map, 对value进行转换 *
     * @param list *
     * @param keyMapper *
     * @param valueMapper *
     * @param <K> *
     * @param <V> *
     * @param <T> *
     * @return
     */
    public static <K, V, T> Map<K, List<T>> listGroup(List<V> list, Function<V, K> keyMapper, Function<V, T> valueMapper) {
        return listGroup(list, keyMapper).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().stream().map(valueMapper).collect(Collectors.toList())));
    }

    /**
     * list通过key group by 生成 map, 对value进行转换, value为Set *
     * @param list *
     * @param keyMapper *
     * @param valueMapper *
     * @param <K> *
     * @param <V> *
     * @param <T> *
     * @return
     */
    public static <K, V, T> Map<K, Set<T>> listGroup2Set(List<V> list, Function<V, K> keyMapper, Function<V, T> valueMapper) {
        return listGroup(list, keyMapper).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().stream().map(valueMapper).collect(Collectors.toSet())));
    }

    public static <K, V, O> List<O> mapToObjList(Map<K, V> map, KeyValueConsumer<K, V, O> consumer) {
        List<O> list = new ArrayList<>();
        map.forEach((k, v) -> list.add(consumer.apply(k, v)));
        return list;
    }

    /**
     * 对象list转为指定key、value的map *
     * @param list 入参list *
     * @param keyMapper map key函数 *
     * @param valMapper map value函数 *
     * @param <K> map key类型 *
     * @param <V> map value类型 *
     * @param <T> 入参对象类型 *
     * @return map
     */
    public static <K, V, T> Map<K, V> listToAttributeMap(Collection<T> list, Function<T, K> keyMapper, Function<T, V> valMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(0);
        }
        return list.stream().filter(Objects::nonNull).filter(obj -> Objects.nonNull(keyMapper.apply(obj))).collect(Collectors.toMap(keyMapper, valMapper, oldMerger()));
    }

    public static <K, V> List<V> mapToValueList(Map<K, V> map) {
        return mapToObjList(map, (k, v) -> v);
    }

    private static <T> BinaryOperator<T> oldMerger() {
        return (t, u) -> u;
    }

    @FunctionalInterface
    public interface KeyValueConsumer<K, V, O> {
        O apply(K k, V v);
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !MapUtils.isEmpty(map);
    }
}