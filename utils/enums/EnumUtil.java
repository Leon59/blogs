/**
 * Xinfei.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package io.kyoto.pillar.lcs.utils.enums;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @see https://www.cnblogs.com/mindzone/p/16890632.html
 * @see https://www.cnblogs.com/mindzone/p/16922717.html
 */
@Slf4j
@SuppressWarnings("all")
public class EnumUtil {
    private static final List<Class> enumClass = new ArrayList<>();
    private static final String CLASS_SCAN_PACKAGE_PATH = "cn.cloud9.server.struct.enums.state";

    @SneakyThrows
    public static void findEnumClassByPath() {
        if (CollectionUtils.isNotEmpty(enumClass)) return;
        try {
            final ScanSupport scanSupport = SpringContextHolder.getBean(ScanSupport.class);
            final Set<Class<?>> classes = scanSupport.doScan(CLASS_SCAN_PACKAGE_PATH);
            enumClass.addAll(classes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initialize() {
        findEnumClassByPath();

        RedisTemplate<String, Map<String, String>> mapTemplate = SpringContextHolder.getBean("redisTemplate", RedisTemplate.class);
        final HashOperations<String, Object, Object> hashOps = mapTemplate.opsForHash();

        String sqlKey = "default";
        /* 准备缓存结构容器, 并装载数据 */
        Map<String, String> mapTank = new ConcurrentHashMap<>();
        Map<String, List<DictDTO>> listTank = new ConcurrentHashMap<>();

        final List<DictDTO> dicts = transEnumsToDicts();
        for (DictDTO dict : dicts) {
            final Long dictCode = dict.getDictCode();
            final String dictName = dict.getDictName();
            final String dictType = dict.getDictType();

            /* 装载 key -> h-key -> h-value */
            final String mapKey = sqlKey + SEPARATOR + dictType + SEPARATOR + dictCode;
            mapTank.put(mapKey, dictName);

            /* 装载 key -> h-key -> h-list */
            final String listKey = sqlKey + SEPARATOR + dictType;
            List<DictDTO> cateList = listTank.get(listKey);
            if (CollectionUtils.isEmpty(cateList)) {
                cateList = new ArrayList<>();
                listTank.put(listKey, cateList);
            }
            cateList.add(dict);
        }

        /* 装填到Redis中 */
        hashOps.putAll(KEY_MAP, mapTank);
        hashOps.putAll(KEY_LISTS, listTank);

        log.info("Redis 枚举缓存装载完毕 ...... ");
    }

    /**
     * 将声明的枚举集合转换为字典集合
     * @return
     */
    @SneakyThrows
    private static List<DictDTO> transEnumsToDicts() {
        final List<DictDTO> dicts = new ArrayList<>();
        for (Class enumClass : enumClass) {
            if (!enumClass.isEnum()) continue;
            final String cateName = enumClass.getSimpleName();
            final Object[] enumInstances = enumClass.getEnumConstants();
            for (Object inst : enumInstances) {

                /* 反射，寻找注解的编号字段 */
                final Field codeField = findFieldByAnnotaion(enumClass, DictCode.class);
                final Object codeVal = codeField.get(inst);

                /* 反射，寻找注解的名字字段 */
                final Field nameField = findFieldByAnnotaion(enumClass, DictName.class);
                final Object nameVal = nameField.get(inst);

                /* 反射，寻找注解的类别字段 */
                final Field cateField = findFieldByAnnotaion(enumClass, DictCate.class);
                final Object cateVal = cateField.get(inst);

                DictDTO dto = ObjectBuilder
                        .<DictDTO>builder(DictDTO::new)
                        .with(DictDTO::setDictType, String.valueOf(cateVal))
                        .with(DictDTO::setDictCode, Long.valueOf(String.valueOf(codeVal)))
                        .with(DictDTO::setDictName, String.valueOf(nameVal))
                        .build();
                dicts.add(dto);
            }
        }
        return dicts;
    }

    /**
     * 获取枚举类中标记了指定注解的字段
     * @param targetClass 枚举目标类对象
     * @param annotationClass 声明的注解类对象
     * @param <T> 注解泛型
     * @param <E> 枚举泛型
     * @return 被注解的字段
     */
    @SneakyThrows
    private static <T extends Annotation, E extends Enum> Field findFieldByAnnotaion(Class<E> targetClass, Class<T> annotationClass) {
        final Field targetField = Arrays
                .stream(targetClass.getDeclaredFields())
                .filter(field -> Objects.nonNull(field.getAnnotation(annotationClass)))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(targetField)) throw new RuntimeException(targetClass.getName() + " 没有声明@" + annotationClass.getName() + "注解！！加载失败！！！");
        targetField.setAccessible(true);
        return targetField;
    }
}