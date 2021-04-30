# 解决了什么问题

1. 避免重复写HTTP调用工具类
2. 支持配置中心配置，动态更改配置信息，无需重启应用
3. 支持动态配置域名(domain)、读取超时时间(readTimeout)、连接超时时间(connectTimeout)、HTTP代理地址(proxy)、需要认证的用户名(userName)、需要认证的密码(password)
4. 支持注解式配置回调及重试策略

# 使用方式

## 引入Maven坐标

```properties
<dependency>
   <groupId>com.github.mx-go</groupId>
   <artifactId>retrofit-rest-proxy</artifactId>
   <version>1.0.0</version>
</dependency>
```

## 定义调用接口

```java
/**
 * Create by max on 2020/09/22
 **/
@RetrofitConfig(value = "message", desc = "发送消息")
@Flexible(maxAttempts = 2, retrySleepTime = 500, retryUnit = TimeUnit.MILLISECONDS)
public interface Message {

    @POST("/send")
    @Flexible(maxAttempts = 3, retrySleepTime = 1, retryUnit = TimeUnit.SECONDS)
    String send(@QueryMap Map<String, String> map);
}
```

> RetrofitConfig注解为自定义注解，方法上注解可参考retrofit注解。

### 重试

```java
@Flexible(maxAttempts = 2, retrySleepTime = 500, retryUnit = TimeUnit.MILLISECONDS)
```

重试注解为 **@Flexible**，如果注解到方法上，则会覆盖类上的配置，优先取方法上的策略。

| 参数           | 备注                                               | 默认值 |
| -------------- | -------------------------------------------------- | ------ |
| maxAttempts    | 最大重试次数(包含首次调用)，最小为1。1表示不重试。 | 1      |
| retrySleepTime | 每次重试间隔时间                                   | 1      |
| retryUnit      | 重试间隔时间单位                                   | 秒     |
| callBackClazz  | 调用后回调。需实现RetrofitCallable接口             | 无     |

## 配置代理接口

```java
@Configuration
public class RestProxy {

    @Resource
    private ConfigRetrofitSpringFactory configRetrofitSpringFactory;

    /**
     * 配置factoryBean.setType为需要代理的接口
     */
    @Bean
    public RetrofitSpringFactoryBean<Message> message() {
        RetrofitSpringFactoryBean<Message> factoryBean = new RetrofitSpringFactoryBean<>();
        factoryBean.setFactory(configRetrofitSpringFactory);
        factoryBean.setType(Message.class);
        return factoryBean;
    }
}
```

> 需要生成多个代理对象时配置多个bean即可

## 配置中心配置

```properties
# 所在的nacos配置中心的dataId
retrofit.rest.proxy.data-id=nacos.properties
# 配置信息的key名称
retrofit.rest.proxy.config-key=rest.config.content
# 具体配置
rest.config.content={"message":{"domain":"localhost:8080","desc":"发送消息","readTimeout":"5000","connectTimeout":"5000"}}
```

> 其中@RetrofitConfig注解中的value要与配置中心中的其中的key名称对应。
>
> retrofit.rest.proxy.config-key 和retrofit.rest.proxy.data-id相同时读取整个dataId数据作为配置。
>
> retrofit.rest.proxy.config-key 默认值为retrofit.config.content
>
> readTimeout和connectTimeout默认为5s

## 使用

```java
@Service
public class DemoServiceImpl {

    @Resource
    private Message message;

    public void sendMessage() {
        String result = message.send(Maps.newHashMap());
    }
}
```

> 直接注入接口调用方法即可