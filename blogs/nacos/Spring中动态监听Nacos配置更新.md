# Spring中动态监听Nacos配置更新

## 需求

有时候我们不只是要获取更新的配置属性，而是要监听配置文件修改事件，然后重新初始化某些类，或者做一些别的事的时候。

## 实现

Nacos提供了一个回调接口Listener，来帮我们实现此类需求。

## 使用示例

```java

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig implements InitializingBean {

    private final NacosConfigManager nacosConfigManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        AbstractConfigChangeListener listener =
                new AbstractConfigChangeListener() {
                    @Override
                    public void receiveConfigChange(ConfigChangeEvent event) {
                        Collection<ConfigChangeItem> changeItems = event.getChangeItems();
                        log.info(changeItems.toString());
                    }
                };
        this.nacosConfigManager
                .getConfigService()
                .addListener("config.yml", "DEFAULT_GROUP", listener);
    }
}

```

当我们在后台修改config.yml后，控制台输出：

```
[ConfigChangeItem{key='app.appName', oldValue='ab', newValue='ab2', type=MODIFIED}]
```

可以很清楚地拿到哪个key被修改了，oldValue和newValue都是什么，我们就可以根据这些信息来实现我们的需求了。
> **注意:**
> 在receiveConfigChange()方法里，需要手动解析配置，不能通过在外部类中使用@Value和注入Properties类来获取最新的配置，否则获取到的还是旧值，原因稍后会说明。

## 原理

在Nacos客户端创建时，会创建NacosConfigService对象，在NacosConfigService内部会委托ClientWorker来进行一些config client相关的操作。
其内部就执行了监听配置变更的逻辑：

```
@Override
public void startInternal() {
    executor.schedule(() -> {
        while (!executor.isShutdown() && !executor.isTerminated()) {
            try {
                listenExecutebell.poll(5L, TimeUnit.SECONDS);
                if (executor.isShutdown() || executor.isTerminated()) {
                    continue;
                }
                executeConfigListen();
            } catch (Throwable e) {
                LOGGER.error("[ rpc listen execute ] [rpc listen] exception", e);
            }
        }
    }, 0L, TimeUnit.MILLISECONDS);
}
```

* executor开启了一个任务，任务内部用while来循环执行。

* listenExecutebell是一个容量为1的阻塞队列，每poll一次就等待5秒，达到每5秒监听一次配置的目的。

* executeConfigListen()方法内部会校验配置的md5值来判断文件是否修改过，如果修改过的话，就调用
  safeNotifyListener()来「异步」通知我们注册的Listener：

```
listener.receiveConfigInfo(contentTmp);
// compare lastContent and content
if (listener instanceof AbstractConfigChangeListener) {
    Map<String, ConfigChangeItem> data = ConfigChangeHandler.getInstance()
            .parseChangeData(listenerWrap.lastContent, contentTmp, type);
    ConfigChangeEvent event = new ConfigChangeEvent(data);
    ((AbstractConfigChangeListener) listener).receiveConfigChange(event);
    listenerWrap.lastContent = contentTmp;
}

```

可以看到我们前面配置的AbstractConfigChangeListener在这里就被调用了。

其实前面的@Value和@ConfigurationProperties的方式也是通过Listener来实现的。

这个监听器是在NacosContextRefresher类中添加的：

```
private void registerNacosListener(final String groupKey, final String dataKey) {
    String key = NacosPropertySourceRepository.getMapKey(dataKey, groupKey);
    Listener listener = listenerMap.computeIfAbsent(key,
        lst -> new AbstractSharedListener() {
                    @Override
                    public void innerReceive(String dataId, String group,
                                    String configInfo) {
                        refreshCountIncrement();
                        nacosRefreshHistory.addRefreshRecord(dataId, group, configInfo);
                        applicationContext.publishEvent(
                                        new RefreshEvent(this, null, "Refresh Nacos config"));
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Refresh Nacos config group=%s,dataId=%s,configInfo=%s", group, dataId, configInfo));
                        }
            }
        });
    try {
        configService.addListener(dataKey, groupKey, listener);
        log.info("[Nacos Config] Listening config: dataId={}, group={}", dataKey,
                        groupKey);
    }
    catch (NacosException e) {
        log.warn(String.format("register fail for nacos listener ,dataId=[%s],group=[%s]", dataKey,
                        groupKey), e);
    }
}
```

其监听了Spring的ApplicationReadyEvent事件，当应用程序准备就绪之后，就会触发这个事件，完成Listener的注册。

## 拓展

虽然我们通过Listener的回调方法拿到的ConfigChangeEvent信息已经足够详细，但是还要手动去解析配置的内容，处理起来比较麻烦。

如果在Listener收到信息之后，能够使用@Value或者注入Properties类来直接获取最新的属性就好了，就像这样：

```
@Override
public void afterPropertiesSet() throws Exception {
    AbstractConfigChangeListener listener = new AbstractConfigChangeListener() {
        @Override
        public void receiveConfigChange(ConfigChangeEvent event) {
            log.info("app Name: {}", appProperties.getAppName());
        }
    };
    this.nacosConfigManager
            .getConfigService()
            .addListener("config.yml", "DEFAULT_GROUP", listener);
}

```

但是测试之后发现log打印的还是旧的配置值。

这是为什么呢？

通过前面的原理我们知道了@Value和@ConfigurationProperties实现自动刷新的方式也是通过Listener来实现的，它们的监听器是在收到ApplicationReadyEvent事件之后添加的，而我们配置类的InitializingBean在这个事件之前就已经被触发了，我们添加的监听器就排在它之前，当触发配置文件修改时，我们的监听器也会先执行，这时候properties的属性都还没刷新，当然就获取不到了。

## 解决方案

### 监听ApplicationReadyEvent事件

既然你这个事件的触发比较晚，那我也监听它，并且做到比你更晚执行。
示例代码

```
@EventListener(ApplicationReadyEvent.class)
public void refresh() throws Exception {
    AbstractConfigChangeListener listener = new AbstractConfigChangeListener() {
        @Override
        public void receiveConfigChange(ConfigChangeEvent event) {
            log.info("app Name: {}", appProperties.getAppName());
        }
    };
    this.nacosConfigManager
            .getConfigService()
            .addListener("config.yml", "DEFAULT_GROUP", listener);
}
```

> 这种方式必须要使用@EventListener注解来监听事件。
>
> 通过注解监听事件，我们的事件监听就会排在Nacos的之后执行，这样我们注册的监听器也就在它之后了。

我们可以在Listener的回调中判断是否是我们需要的配置更新了，然后用properties获取最新的属性来执行逻辑。

### 监听RefreshScopeRefreshedEvent事件

配置属性在刷新完成之后，会发送一个RefreshScopeRefreshedEvent事件，通过监听这个事件，我们就能确保获取的是最新的属性了。
示例代码

```
@EventListener(RefreshScopeRefreshedEvent.class)
public void refresh() throws Exception {
    log.info("app Name: {}", appProperties.getAppName());
}
 
```

> 这种方式无需向Nacos注册Listener，但是也无法判断是不是我们需要的配置更新了。适合一些配置简单的场景。

### 使用BeanPostProcessor(推荐)

通过BeanPostProcessor来在配置类初始化完成之后获取最新的值，这种方式最简单也最准确。

示例代码：

```
@Slf4j
@Component
public class AppPropertiesBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AppProperties properties) {
            log.info("post: " + properties.getAppName());
        }
        return bean;
    }
}

```

> 引用：https://juejin.cn/post/7198073902882177082