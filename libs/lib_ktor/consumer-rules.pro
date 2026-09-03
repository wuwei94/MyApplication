# ==============================================================================
# Ktor Client 混淆保护规则 (lib_ktor)
# ==============================================================================
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# 忽略协程内部调度与原子操作告警
-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.**
