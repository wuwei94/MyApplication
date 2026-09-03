# ==============================================================================
# Java-WebSocket 混淆保护规则 (lib_websocket_java)
# ==============================================================================
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**
-keepclassmembers class * extends org.java_websocket.WebSocketServer {
    *;
}
-keepclassmembers class * extends org.java_websocket.client.WebSocketClient {
    *;
}
