package io.kyoto.pillar.lcs.utils.enums;

import java.lang.annotation.*;

/**
 * -- 字典类别标记
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictCate {
}


/**
 * - 字典编号标记
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface DictCode {
}


/**
 * -- 字典名称标记
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface DictName {
}