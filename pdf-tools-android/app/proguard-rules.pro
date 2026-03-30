# Add project specific ProGuard rules here.

# iText7 — keep all public API
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Kotlin metadata
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class ** {
    @kotlin.Metadata *;
}
