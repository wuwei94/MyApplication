# ==============================================================================
# 基础公共库全局混淆规则 (basic_lib - 自动传递给所有上层业务模块)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Google Gson 序列化保护
# ------------------------------------------------------------------------------
# 保留泛型签名（Gson 反射泛型字段必须）
-keepattributes Signature
# 保留注解
-keepattributes *Annotation*

-dontwarn sun.misc.**

# 保持 TypeAdapter 与 Serializer 实现类
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保持带 @SerializedName 注解的字段名称与结构
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留 TypeToken 泛型签名
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ------------------------------------------------------------------------------
# 2. 阿里巴巴 ARouter 路由组件保护
# ------------------------------------------------------------------------------
-keep public class com.alibaba.android.arouter.routes.** { *; }
-keep public class com.alibaba.android.arouter.facade.** { *; }
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe { *; }

# 保护 IProvider 接口与实现类
-keep interface * implements com.alibaba.android.arouter.facade.template.IProvider
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider

# 保护 @Autowired 注入字段
-keepclasseswithmembers class * {
    @com.alibaba.android.arouter.facade.annotation.Autowired <fields>;
}

# ------------------------------------------------------------------------------
# 3. GreenRobot EventBus 事件总线保护
# ------------------------------------------------------------------------------
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-keep class org.greenrobot.eventbus.android.AndroidComponentsImpl

# ------------------------------------------------------------------------------
# 4. GreenDAO ORM 数据库保护
# ------------------------------------------------------------------------------
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }
-keep class org.greenrobot.greendao.database.SqlCipherEncryptedHelper { *; }
-dontwarn net.sqlcipher.database.**
-dontwarn rx.**