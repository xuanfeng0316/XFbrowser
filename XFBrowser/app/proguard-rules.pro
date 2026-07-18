# 终极混淆 - 强制类名也变
-dontwarn
-ignorewarnings

# 重打包到根目录（类名全变）
-repackageclasses ''
-flattenpackagehierarchy ''
-useuniqueclassmembernames

# 混淆字典（强制用abc）
-obfuscationdictionary {system}
-classobfuscationdictionary {system}
-packageobfuscationdictionary {system}

# 保留必要的（防崩溃）
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Application

# 保留WebView JS接口
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 保留View的构造方法（防反射崩溃）
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 移除所有日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}