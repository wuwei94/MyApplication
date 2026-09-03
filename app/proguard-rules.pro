# ==============================================================================
# MyApplication - 主应用 R8 / ProGuard 混淆规则配置
# ==============================================================================
# 编译时配合 proguard-android-optimize.txt (见 app/build.gradle.kts)
# 子模块混淆规则通过各模块中的 consumer-rules.pro 自动合并。
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. 基础混淆控制与调试追溯配置
# ------------------------------------------------------------------------------
# 不使用大小写混合类名（防止 Windows/macOS 大小写不敏感文件系统解压冲突）
-dontusemixedcaseclassnames
# 不跳过非公共的库类
-dontskipnonpubliclibraryclasses
# 打印详细混淆日志
-verbose
# 忽略警告（保证存在可选依赖时编译通过）
-ignorewarnings

# 保持属性：保留行号和源文件名（线上崩溃堆栈符号化还原定位必备）
-keepattributes SourceFile,LineNumberTable
# 保持属性：保留泛型签名、注解、内部类与封闭方法
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# ------------------------------------------------------------------------------
# 2. Android 核心组件与系统回调保护
# ------------------------------------------------------------------------------
# 保持四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保持所有自定义 View 的 XML 布局反射构造函数与 setter 方法
-keepclassmembers public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public void set*(...);
}

# 保持 XML 布局中 android:onClick 反射绑定的点击事件方法
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ------------------------------------------------------------------------------
# 3. 本地 Native JNI 方法保护
# ------------------------------------------------------------------------------
-keepclasseswithmembernames class * {
    native <methods>;
}

# ------------------------------------------------------------------------------
# 4. 数据实体与序列化保护 (Serialization, Parcelable, Enum)
# ------------------------------------------------------------------------------
# 枚举类保护（保持 values 和 valueOf 方法）
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable 序列化保护（保持 CREATOR 静态成员）
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Serializable 序列化保护
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ------------------------------------------------------------------------------
# 5. 本项目业务数据模型 (Bean / Model / Entity / DTO) 统一保护
# ------------------------------------------------------------------------------
# 保护所有数据实体类字段不被混淆或剔除（供 JSON 解析与 ORM 映射使用）
-keep class com.example.william.my.**.model.** { *; }
-keep class com.example.william.my.**.bean.** { *; }
-keep class com.example.william.my.**.entity.** { *; }
-keep class com.example.william.my.**.dto.** { *; }

# ------------------------------------------------------------------------------
# 6. WebView 与 JavaScript 接口安全规则
# ------------------------------------------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ------------------------------------------------------------------------------
# 7. Kotlin 协程与元数据保护
# ------------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}