package io.kyoto.pillar.lcs.utils.enums;/**
 * Xinfei.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */

import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Leon
 * @since 2023/9/25
 */
@Getter
@AllArgsConstructor
public enum MyType {


        GROUP1_TYPE1("a类型1", 1001, MyType.KEY_GP1),
        GROUP1_TYPE2("a类型2", 1002, MyType.KEY_GP1),
        GROUP1_TYPE3("a类型3", 1003, MyType.KEY_GP1),

        GROUP2_TYPE1("b类型1", 1001,  MyType.KEY_GP2),
        GROUP2_TYPE2("b类型2", 1002,  MyType.KEY_GP2),
        GROUP2_TYPE3("b类型3", 1003,  MyType.KEY_GP2),

        GROUP3_TYPE1("c类型1", 1001, MyType.KEY_GP3),
        GROUP3_TYPE2("c类型2", 1002, MyType.KEY_GP3),
        GROUP3_TYPE3("c类型3", 1003, MyType.KEY_GP3),
        ;

        private final String name;
        private final Integer value;
        private final String cate;

        private static final Map<String, List<MyType>> typeList = new ConcurrentHashMap<>();
        private static final Map<String, String> typeMap = new ConcurrentHashMap<>();

        public static final String SEPARATOR = "@";
        public static final String KEY_GP1 = "GT-1001";
        public static final String KEY_GP2 = "GT-1002";
        public static final String KEY_GP3 = "GT-1003";

        static {
            for (MyType myType : MyType.values()) {
                final String myTypeCate = myType.getCate();
                final String myTypeName = myType.getName();
                final Integer myTypeValue = myType.getValue();

                final String key = myTypeCate + SEPARATOR + myTypeValue;
                typeMap.put(key, myTypeName);

                List<MyType> myTypes = typeList.get(myTypeCate);
                if (CollectionUtils.isEmpty(myTypes)) {
                    myTypes = new ArrayList<>();
                    typeList.put(myTypeCate, myTypes);
                }
                myTypes.add(myType);
            }
        }

        /**
         * 按枚举类别获取枚举集合
         * @param cate
         * @return
         */
        public static List<MyType> getTypeListByCate(String cate) {
            return typeList.get(cate);
        }

        /**
         * 按枚举类别获取枚举集合（响应用）
         * @param cate
         * @return
         */
        public static List<Map<String, String>> getItemListByCate(String cate) {
            final List<MyType> myTypes = typeList.get(cate);
            if (CollectionUtils.isEmpty(myTypes)) return Collections.EMPTY_LIST;
            final ArrayList<Map<String, String>> items = new ArrayList<>(myTypes.size());
            for (MyType myType : myTypes) {
                items.add(myType.getItem());
            }
            return items;
        }

        /**
         * 按枚举类别和code获取名称
         * @param cate
         * @param code
         * @return
         */
        public static String getNameBy(String cate, Integer code) {
            String key =  cate + SEPARATOR + code;
            return typeMap.get(key);
        }

        public Map<String, String> getItem() {
            Map<String, String> item = new ConcurrentHashMap<>();
            item.put("instName", name());
            item.put("instIndex", String.valueOf(this.ordinal()));
            item.put("name", this.name);
            item.put("value", String.valueOf(this.value));
            return item;
        }

}
