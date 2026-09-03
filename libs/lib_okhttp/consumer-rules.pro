# ==============================================================================
# OkHttp 3 & Okio 混淆保护规则 (lib_okhttp)
# ==============================================================================

# ------------------------------------------------------------------------------
# Okio
# ------------------------------------------------------------------------------
# Animal Sniffer compileOnly 兼容性告警忽略
-dontwarn org.codehaus.mojo.animal_sniffer.*

# ------------------------------------------------------------------------------
# OkHttp 3
# ------------------------------------------------------------------------------
# JSR 305 空安全注解告警忽略
-dontwarn javax.annotation.**

# 公共后缀资源相对路径保留
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# 可选加密平台适配实现（Conscrypt, BouncyCastle, OpenJSSE 等平台非必需）
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
