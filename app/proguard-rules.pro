# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Security Crypto for EncryptedSharedPreferences
-keep class androidx.security.** { *; }
-keep class android.security.keystore.** { *; }

# Keep all Tink crypto classes
-keep class com.google.crypto.tink.** { *; }
-keep class com.google.crypto.tink.proto.** { *; }

# Keep SQLCipher classes
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-keep class com.xplore.paymobile.data.remote.model.data** { *; }

# Keep all classes that are serialized/deserialized using Gson
-keep class com.google.gson.** { *; }

# Ignore AWT classes
-dontwarn java.awt.**
-dontwarn java.awt.image.**

-keep class com.clearent.idtech.** { *; }