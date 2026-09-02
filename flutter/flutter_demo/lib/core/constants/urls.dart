class Urls {
  Urls._();

  /// 登录接口
  static const String login = 'https://www.wanandroid.com/user/login';

  /// 帖子列表接口
  static const String posts = 'https://jsonplaceholder.typicode.com/posts';

  /// 用户名 key
  static const String keyUsername = 'username';

  /// 密码 key
  static const String keyPassword = 'password';

  /// 用户名（示例值）
  static const String valueUsername = '17778060027';

  /// 密码（示例值）
  static const String valuePassword = '123456';

  /// WebSocket Echo 测试服务
  static const String websocketEcho = 'wss://echo.websocket.org';

  /// MQTT 公共 Broker 主机（EMQX，无需账号）
  static const String mqttHost = 'broker.emqx.io';

  /// MQTT 公共 Broker 端口
  static const int mqttPort = 1883;

  /// MQTT 默认主题
  static const String mqttTopic = 'mqtt/example';

  /// DeepSeek AI 流式对话接口
  static const String deepSeek = 'https://api.deepseek.com/chat/completions';
}
