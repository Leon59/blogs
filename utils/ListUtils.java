package io.kyoto.pillar.lcs.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Leon
 */
public class ListUtils {
    /**
     * 从list中摘出字段生成新的list并去重和去空 *
     * @param rawList *
     * @param apply *
     * @param <T> *
     * @param <R> *
     * @return
     */
    public static <T, R> List<R> distinctMap(List<T> rawList, Function<T, R> apply) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawList.stream().map(apply).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 从list中摘出字段生成新的list并过滤、去重和去空 *
     * @param rawList *
     * @param filter *
     * @param map *
     * @param <T> *
     * @param <R> *
     * @return
     */
    public static <T, R> List<R> filterDistinctMap(List<T> rawList, Predicate<T> filter, Function<T, R> map) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawList.stream().filter(Objects::nonNull).filter(filter).map(map).distinct().collect(Collectors.toList());
    }

    /**
     * 求和 *
     * @param rawList *
     * @param apply *
     * @param <T> *
     * @return
     */
    public static <T> int sumToInt(List<T> rawList, ToIntFunction<T> apply) {
        return rawList.stream().mapToInt(apply).sum();
    }

    /**
     * 求和 * *
     * @param rawList *
     * @param apply *
     * @param <T> *
     * @return
     */
    public static <T> Long sumToLong(Collection<T> rawList, ToLongFunction<T> apply) {
        return rawList.stream().mapToLong(apply).sum();
    }

    /**
     * list 去空去从 * *
     * @param rawList list *
     * @param <T> 泛型 *
     * @return list
     */
    public static <T> List<T> distinct(List<T> rawList) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawList.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 从list中摘出字段生成新的set并去重和去空 *
     * @param rawList *
     * @param apply *
     * @param <T> *
     * @param <R> *
     * @return
     */
    public static <T, R> Set<R> toSet(List<T> rawList, Function<T, R> apply) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Sets.newHashSetWithExpectedSize(0);
        }
        return rawList.stream().map(apply).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 按list中的字段去重并返回原先类型的stream * *
     * @param rawList *
     * @param <T> *
     * @param <R> *
     * @return  @paramparam
     */
    public static <T, R> Stream<T> distinctListByParam(List<T> rawList, Function<T, R> param, BinaryOperator<T> reduceFunc) {
        return rawList.stream().collect(Collectors.groupingBy(param)).values()
                //取value的stream获得合并后的列表
                .stream()
                //用reduceFunc取出列表中被去重后的结果
                .map(item -> item.stream()
                        //取时间最小值
                        .reduce(reduceFunc).get());
    }

    public static <T> List<T> fromArray(T[] arr) {
        if (arr == null || arr.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(arr));
    }

    /**
     * 获取list中第一个元素 * *
     * @param list list *
     * @param <T> T *
     * @return list中第一个元素
     */
    public static <T> T findFirst(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 获取list中最后一个元素 * *
     * @param list list *
     * @param <T> T *
     * @return list中最后一个元素
     */
    public static <T> T findLast(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(list.size() - 1);
    }

    /**
     * 从list中摘出字段生成新的list并去重和去blank * *
     * @param rawList *
     * @param apply *
     * @param <T> *
     * @return
     */
    public static <T> List<String> distinctNotBlankMap(List<T> rawList, Function<T, String> apply) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawList.stream().map(apply).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
    }

    public static <T, V> List<V> distinctSet2List(Set<T> rawSet, Function<T, V> apply) {
        if (CollectionUtils.isEmpty(rawSet)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawSet.stream().map(apply).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 字符拼接 * *
     * @param rawList list *
     * @param apply Function *
     * @param delimiter 拼接字符 *
     * @param <T> 类型 *
     * @return 字符
     */
    public static <T> String join(List<T> rawList, Function<T, String> apply, String delimiter) {
        if (CollectionUtils.isEmpty(rawList)) {
            return StringUtils.EMPTY;
        }
        return rawList.stream().map(apply).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }

    /**
     * 获取最大的Long值，不存在返回null * *
     * @param rawList list *
     * @param apply function *
     * @param <T> 类型 *
     * @return 最大的Long值
     */
    public static <T> Long maxLong(List<T> rawList, Function<T, Long> apply) {
        if (CollectionUtils.isEmpty(rawList)) {
            return null;
        }
        return rawList.stream().map(apply).max(Long::compare).orElse(null);
    }

    /**
     * 类型转换(去空但是不会去重复) * *
     * @param rawList 需要处理的集合 *
     * @param apply function *
     * @param <T> 类型 *
     * @param <R> 返回结果 *
     * @return List<R>
     */
    public static <T, R> List<R> convertNoDistinct(List<T> rawList, Function<T, R> apply) {
        if (CollectionUtils.isEmpty(rawList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return rawList.stream().map(apply).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static <T> List<List<T>> splitList(List<T> tList, int rows) {
        if (CollectionUtils.isEmpty(tList)) {
            return new ArrayList<>();
        }
        if (rows <= 0) {
            throw new IllegalArgumentException("rows 不能小于等于 0");
        }
        int temp = tList.size() / rows;
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i <= temp; i++) {
            List<T> subList = tList.subList((i * rows), Math.min((i + 1) * rows, tList.size()));
            if (subList.size() != 0) {
                result.add(subList);
            }
        }
        return result;
    }
}