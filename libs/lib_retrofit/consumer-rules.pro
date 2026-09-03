# ==============================================================================
# Retrofit 2 混淆保护规则 (lib_retrofit)
# ==============================================================================

# 保持方法泛型签名与内部类信息（Retrofit 动态代理反射必须）
-keepattributes Signature, InnerClasses, EnclosingMethod
# 保持运行时可见注解与参数注解（@GET, @POST, @Query, @Body 等）
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# 保留 Retrofit API 接口中的方法签名
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 解决 R8 Full Mode 下动态代理接口被剔除导致返回 null 的问题
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# 忽略构建工具与可选注解告警
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
