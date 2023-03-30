package io.kyoto.pillar.lcs.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础的转换类 * 通过调研发现 cglib.beans.BeanCopier 的性能是Bean拷贝性能最好的一个，基于动态代理的方式
 * * 不使用Apache和spring的 * *
 * @author: Leon
 */
@Slf4j
public abstract class EntityTranslate {
    /**
     * 对象拷贝 * *
     * @param source 源 *
     * @param target 目标 *
     * @param <K> 源泛型 *
     * @param <V> 目标泛型 *
     * @return target
     */
    public static <K, V> V translate(K source, V target) {
        if (source == null) {
            return null;
        }
        BeanCopier beanCopier = BeanCopier.create(source.getClass(), target.getClass(), false);
        beanCopier.copy(source, target, null);
        return target;
    }

    /**
     * 批量对象拷贝
     */
    public static <S, T> List<T> batchTranslate(List<S> sourceList, Class<T> targetClz) {
        List<T> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(sourceList)) {
            return resultList;
        }
        BeanCopier beanCopier = BeanCopier.create(sourceList.get(0).getClass(), targetClz, false);
        try {
            for (S source : sourceList) {
                T target = targetClz.newInstance();
                beanCopier.copy(source, target, null);
                resultList.add(target);
            }
        } catch (Exception e) {
            log.error("批量对象拷贝失败:", e);
            throw new RuntimeException("批量对象拷贝失败", e);
        }
        return resultList;
    }
}