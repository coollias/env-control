package com.bank.config.client.hotupdate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置值注解
 * 用于标记需要热更新的字段
 * 
 * @author bank
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {
    
    /**
     * 配置键
     * 如果不指定，则使用字段名作为配置键
     */
    String value() default "";
    
    /**
     * 配置键前缀
     * 例如：app.config.，最终配置键为 app.config.字段名
     */
    String prefix() default "";
    
    /**
     * 默认值
     * 当配置不存在时使用此值
     */
    String defaultValue() default "";
    
    /**
     * 是否必填
     * 如果为true，配置不存在时会抛出异常
     */
    boolean required() default false;
    
    /**
     * 配置描述
     */
    String description() default "";
}
