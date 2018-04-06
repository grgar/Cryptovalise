# Debugging assistance
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Graph animations use reflection which ProGuard doesn't detect as class usage
-keep class com.github.mikephil.charting.** { *; }
