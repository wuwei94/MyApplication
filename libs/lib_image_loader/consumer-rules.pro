# ==============================================================================
# 图片加载框架混淆保护规则 (lib_image_loader - Glide & Coil)
# ==============================================================================

# ------------------------------------------------------------------------------
# Glide 混淆保护
# ------------------------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# ------------------------------------------------------------------------------
# Coil 3 混淆保护
# ------------------------------------------------------------------------------
-dontwarn coil3.**
-keep class coil3.** { *; }