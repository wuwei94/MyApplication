# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Java-WebSocket
-keep class org.java_websocket.** { *; }
-keepclassmembers class * extends org.java_websocket.WebSocketServer {
    *;
}
-keepclassmembers class * extends org.java_websocket.client.WebSocketClient {
    *;
}
