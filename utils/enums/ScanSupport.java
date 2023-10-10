/**
 * Xinfei.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package io.kyoto.pillar.lcs.utils.enums;

import com.sun.xml.internal.ws.api.databinding.MetadataReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class ScanSupport implements ResourceLoaderAware {{
    private ResourceLoader resourceLoader;
    private final ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 扫描公共接口包,获取所有的公共接口并加入白名单
     *
     */
    @SneakyThrows
    public Set<Class<?>> doScan(String classPath) {
        Set<Class<?>> classes = new HashSet<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                .concat(ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(classPath))
                        .concat("/**/*.class"));
        Resource[] resources = resolver.getResources(packageSearchPath);
        MetadataReader metadataReader = null;
        for (Resource resource : resources) {
            if (!resource.isReadable()) continue;
            metadataReader = metadataReaderFactory.getMetadataReader(resource);
            try {
                classes.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
            } catch (Exception e) {
                log.info("公共接口信息解析错误:{}", e.getMessage());
            }
        }
        return classes;
    }
}