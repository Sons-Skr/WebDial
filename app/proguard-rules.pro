# WebView and WebSettings preservation
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Data Models
-keep class com.example.data.** { *; }

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

