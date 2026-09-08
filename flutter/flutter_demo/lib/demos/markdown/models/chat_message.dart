/// 聊天消息数据实体（对标 Android module_markdown 的 `ChatMessage`）
///
/// [id] 消息唯一标识（UUID）
/// [role] 角色（user / assistant / system）
/// [content] 消息文本内容（支持 Markdown）
/// [status] 发送与生成状态（sending / streaming / completed / failed）
/// [createdAt] 发送时间戳
class ChatMessage {
  ChatMessage({
    required this.id,
    required this.role,
    required this.content,
    this.status = ChatStatus.completed,
  }) : createdAt = DateTime.now();

  final String id;
  final ChatRole role;
  String content;
  ChatStatus status;
  final DateTime createdAt;
}

/// 消息角色：区分用户与助手消息
enum ChatRole { user, assistant, system }

/// 消息状态：消息的加载与生成状态
enum ChatStatus { sending, streaming, completed, failed }

extension ChatRoleLabel on ChatRole {
  String get label => switch (this) {
    ChatRole.user => '用户',
    ChatRole.assistant => '助手',
    ChatRole.system => '系统',
  };
}

extension ChatStatusLabel on ChatStatus {
  String get label => switch (this) {
    ChatStatus.sending => '发送中',
    ChatStatus.streaming => '生成中',
    ChatStatus.completed => '已完成',
    ChatStatus.failed => '已中断',
  };
}
