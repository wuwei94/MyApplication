// import 'package:flutter_demo/core/utils/ui/toast.dart';
// import 'package:extended_text_field/extended_text_field.dart';
// import 'package:flutter/gestures.dart';
// import 'package:flutter/material.dart';
// import 'package:flutter/services.dart';
//
// /// extended_text_field
// /// https://pub.dev/packages/extended_text_field
// class ExtendedTextFieldDemoPage extends StatelessWidget {
//   const ExtendedTextFieldDemoPage({super.key, required this.title});
//
//   final String title;
//
//   @override
//   Widget build(BuildContext context) {
//     return ExtendedTextFieldDemoView(title: title);
//   }
// }
//
// class ExtendedTextFieldDemoView extends StatefulWidget {
//   const ExtendedTextFieldDemoView({super.key, required this.title});
//
//   final String title;
//
//   @override
//   State<ExtendedTextFieldDemoView> createState() =>
//       _ExtendedTextFieldDemoViewState();
// }
//
// class _ExtendedTextFieldDemoViewState extends State<ExtendedTextFieldDemoView> {
//   static const String _initialValue =
//       'extended_text_field 可以把输入中的特殊 token 渲染成富文本。\n\n'
//       '试试输入 @Alice 然后继续打字，或者插入 #Flutter# 这样的主题标签。\n\n'
//       '如果要把图片也嵌进文本里，可以使用 [img:pic7] 这样的占位 token。';
//
//   static const String _selectablePreviewValue =
//       '只读场景也可以复用同一套 builder，比如高亮 @Alice 和 #Flutter#。';
//
//   static const Map<String, String> _imageAssets = <String, String>{
//     'pic7': 'assets/images/pic7.jpg',
//     'pic14': 'assets/images/pic14.jpg',
//     'pic18': 'assets/images/pic18.jpg',
//   };
//
//   late final TextEditingController _controller;
//   late final FocusNode _focusNode;
//   late final _DemoSpecialTextSpanBuilder _spanBuilder;
//
//   String _statusMessage = '点击快捷 token 插入内容，观察输入态富文本和实际保存字符串之间的关系。';
//
//   @override
//   void initState() {
//     super.initState();
//     _controller = TextEditingController(text: _initialValue);
//     _focusNode = FocusNode();
//     _spanBuilder = _DemoSpecialTextSpanBuilder(
//       imageAssets: _imageAssets,
//       onTokenTap: _handleSpecialTokenTap,
//     );
//     _controller.addListener(_handleTextChanged);
//   }
//
//   @override
//   void dispose() {
//     _controller.removeListener(_handleTextChanged);
//     _controller.dispose();
//     _focusNode.dispose();
//     super.dispose();
//   }
//
//   int get _mentionCount =>
//       RegExp(r'@[^\s]+\s').allMatches(_controller.text).length;
//
//   int get _topicCount =>
//       RegExp(r'#[^#\n]+#').allMatches(_controller.text).length;
//
//   int get _imageCount =>
//       RegExp(r'\[img:[^\]]+\]').allMatches(_controller.text).length;
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(title: Text(widget.title)),
//       body: ListView(
//         padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
//         children: <Widget>[
//           _buildOverviewCard(context),
//           const SizedBox(height: 16),
//           _buildEditorSection(context),
//           const SizedBox(height: 16),
//           _buildPreviewSection(context),
//           const SizedBox(height: 16),
//           _buildSelectableSection(context),
//           const SizedBox(height: 16),
//           _buildRawTextSection(context),
//         ],
//       ),
//     );
//   }
//
//   Widget _buildOverviewCard(BuildContext context) {
//     final ThemeData theme = Theme.of(context);
//
//     return DecoratedBox(
//       decoration: BoxDecoration(
//         gradient: const LinearGradient(
//           colors: <Color>[Color(0xFFF7FAFC), Color(0xFFE8F1FF)],
//           begin: Alignment.topLeft,
//           end: Alignment.bottomRight,
//         ),
//         borderRadius: BorderRadius.circular(24),
//         border: Border.all(color: const Color(0xFFD6E4FF)),
//         boxShadow: const <BoxShadow>[
//           BoxShadow(
//             color: Color(0x110F172A),
//             blurRadius: 20,
//             offset: Offset(0, 10),
//           ),
//         ],
//       ),
//       child: Padding(
//         padding: const EdgeInsets.all(20),
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: <Widget>[
//             Row(
//               crossAxisAlignment: CrossAxisAlignment.start,
//               children: <Widget>[
//                 Container(
//                   width: 52,
//                   height: 52,
//                   decoration: BoxDecoration(
//                     color: const Color(0xFF2563EB),
//                     borderRadius: BorderRadius.circular(16),
//                   ),
//                   alignment: Alignment.center,
//                   child: const Icon(
//                     Icons.text_fields_rounded,
//                     color: Colors.white,
//                   ),
//                 ),
//                 const SizedBox(width: 12),
//                 Expanded(
//                   child: Column(
//                     crossAxisAlignment: CrossAxisAlignment.start,
//                     children: <Widget>[
//                       Text(
//                         'extended_text_field 16.0.2',
//                         style: theme.textTheme.titleMedium?.copyWith(
//                           fontWeight: FontWeight.w700,
//                         ),
//                       ),
//                       const SizedBox(height: 6),
//                       Text(
//                         '它的核心价值不是“再包一层 TextField”，而是让输入框支持可解析的特殊文本、可点击高亮和 inline widget span。',
//                         style: theme.textTheme.bodyMedium?.copyWith(
//                           color: Colors.black87,
//                           height: 1.5,
//                         ),
//                       ),
//                     ],
//                   ),
//                 ),
//               ],
//             ),
//             const SizedBox(height: 16),
//             Wrap(
//               spacing: 10,
//               runSpacing: 10,
//               children: <Widget>[
//                 _MetricChip(label: '@Mention', value: '$_mentionCount'),
//                 _MetricChip(label: '#Topic#', value: '$_topicCount'),
//                 _MetricChip(label: 'Inline Image', value: '$_imageCount'),
//               ],
//             ),
//             const SizedBox(height: 16),
//             Text(
//               _statusMessage,
//               style: theme.textTheme.bodyMedium?.copyWith(
//                 color: const Color(0xFF1D4ED8),
//                 height: 1.4,
//               ),
//             ),
//             const SizedBox(height: 16),
//             const Wrap(
//               spacing: 8,
//               runSpacing: 8,
//               children: <Widget>[
//                 _SyntaxBadge(label: '@mention', example: '@Alice '),
//                 _SyntaxBadge(label: 'topic', example: '#Flutter#'),
//                 _SyntaxBadge(label: 'image', example: '[img:pic7]'),
//               ],
//             ),
//           ],
//         ),
//       ),
//     );
//   }
//
//   Widget _buildEditorSection(BuildContext context) {
//     return _SectionCard(
//       title: '输入态 Demo',
//       subtitle:
//           '这里直接使用 ExtendedTextField，并通过 SpecialTextSpanBuilder 把 token 映射成高亮文本和 inline image。',
//       child: Column(
//         crossAxisAlignment: CrossAxisAlignment.start,
//         children: <Widget>[
//           Wrap(
//             spacing: 8,
//             runSpacing: 8,
//             children: <Widget>[
//               ActionChip(
//                 avatar: const Icon(Icons.alternate_email, size: 18),
//                 label: const Text('插入 @Alice'),
//                 onPressed: () => _insertText('@Alice '),
//               ),
//               ActionChip(
//                 avatar: const Icon(Icons.tag, size: 18),
//                 label: const Text('插入 #Flutter#'),
//                 onPressed: () => _insertText('#Flutter#'),
//               ),
//               ActionChip(
//                 avatar: const Icon(Icons.image_outlined, size: 18),
//                 label: const Text('插入 [img:pic7]'),
//                 onPressed: () => _insertText('[img:pic7]'),
//               ),
//               ActionChip(
//                 avatar: const Icon(Icons.text_snippet_outlined, size: 18),
//                 label: const Text('插入普通文本'),
//                 onPressed: () => _insertText(' 这是一段普通文本 '),
//               ),
//             ],
//           ),
//           const SizedBox(height: 12),
//           Row(
//             children: <Widget>[
//               Expanded(
//                 child: Text(
//                   '@mention 以空格结尾，#topic# 以 `#` 包裹，图片用 [img:alias] 表示。',
//                   style: Theme.of(context).textTheme.bodySmall?.copyWith(
//                     color: Colors.black54,
//                     height: 1.4,
//                   ),
//                 ),
//               ),
//               const SizedBox(width: 12),
//               OutlinedButton.icon(
//                 onPressed: _resetText,
//                 icon: const Icon(Icons.refresh),
//                 label: const Text('重置'),
//               ),
//             ],
//           ),
//           const SizedBox(height: 16),
//           DecoratedBox(
//             decoration: BoxDecoration(
//               color: const Color(0xFFF8FAFC),
//               borderRadius: BorderRadius.circular(20),
//               border: Border.all(color: const Color(0xFFDCE3F0)),
//             ),
//             child: Padding(
//               padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
//               child: ExtendedTextField(
//                 controller: _controller,
//                 focusNode: _focusNode,
//                 minLines: 6,
//                 maxLines: 10,
//                 strutStyle: const StrutStyle(),
//                 specialTextSpanBuilder: _spanBuilder,
//                 keyboardType: TextInputType.multiline,
//                 decoration: const InputDecoration(
//                   border: InputBorder.none,
//                   hintText: '输入 @mention、#topic# 或 [img:pic7] 试试看',
//                 ),
//                 style: const TextStyle(fontSize: 16, height: 1.5),
//               ),
//             ),
//           ),
//         ],
//       ),
//     );
//   }
//
//   Widget _buildPreviewSection(BuildContext context) {
//     return _SectionCard(
//       title: '渲染预览',
//       subtitle: '同一段底层字符串经过 builder 处理后，会变成真正的富文本和 widget span。',
//       child: DecoratedBox(
//         decoration: BoxDecoration(
//           color: const Color(0xFFF8FAFC),
//           borderRadius: BorderRadius.circular(20),
//           border: Border.all(color: const Color(0xFFDCE3F0)),
//         ),
//         child: Padding(
//           padding: const EdgeInsets.all(16),
//           child: Text.rich(
//             _spanBuilder.build(
//               _controller.text,
//               textStyle: const TextStyle(
//                 fontSize: 16,
//                 height: 1.55,
//                 color: Color(0xFF111827),
//               ),
//             ),
//           ),
//         ),
//       ),
//     );
//   }
//
//   Widget _buildSelectableSection(BuildContext context) {
//     return _SectionCard(
//       title: '只读态 Demo',
//       subtitle:
//           '同样的 builder 也可以给 ExtendedSelectableText 复用，这样详情页、评论区、聊天记录就能和输入态保持一致。',
//       child: DecoratedBox(
//         decoration: BoxDecoration(
//           color: const Color(0xFFF8FAFC),
//           borderRadius: BorderRadius.circular(20),
//           border: Border.all(color: const Color(0xFFDCE3F0)),
//         ),
//         child: Padding(
//           padding: const EdgeInsets.all(16),
//           child: _SelectablePreview(onTokenTap: _handleSpecialTokenTap),
//         ),
//       ),
//     );
//   }
//
//   Widget _buildRawTextSection(BuildContext context) {
//     return _SectionCard(
//       title: '实际保存值',
//       subtitle: '图片并不会直接写进文本本身，真正保存的仍然是 token，这也是它很适合做富文本输入协议层的原因。',
//       trailing: OutlinedButton.icon(
//         onPressed: _copyRawText,
//         icon: const Icon(Icons.copy_all_outlined),
//         label: const Text('复制字符串'),
//       ),
//       child: DecoratedBox(
//         decoration: BoxDecoration(
//           color: const Color(0xFF0F172A),
//           borderRadius: BorderRadius.circular(20),
//         ),
//         child: Padding(
//           padding: const EdgeInsets.all(16),
//           child: SelectionArea(
//             child: Text(
//               _controller.text,
//               style: const TextStyle(
//                 color: Colors.white,
//                 height: 1.55,
//                 fontFamily: 'monospace',
//               ),
//             ),
//           ),
//         ),
//       ),
//     );
//   }
//
//   void _handleTextChanged() {
//     if (!mounted) {
//       return;
//     }
//
//     setState(() {});
//   }
//
//   void _updateStatusMessage(String message) {
//     if (!mounted) {
//       return;
//     }
//
//     setState(() {
//       _statusMessage = message;
//     });
//   }
//
//   void _handleSpecialTokenTap(String token) {
//     _updateStatusMessage('点击了特殊 token：$token');
//     showToast('点击了 $token');
//   }
//
//   void _insertText(String text) {
//     final TextEditingValue value = _controller.value;
//     final TextSelection selection = value.selection;
//     final int start = selection.isValid ? selection.start : value.text.length;
//     final int end = selection.isValid ? selection.end : value.text.length;
//     final String newText = value.text.replaceRange(start, end, text);
//     final int offset = start + text.length;
//
//     _controller.value = value.copyWith(
//       text: newText,
//       selection: TextSelection.collapsed(offset: offset),
//       composing: TextRange.empty,
//     );
//
//     _focusNode.requestFocus();
//     _updateStatusMessage('已插入 $text');
//   }
//
//   void _resetText() {
//     _controller.value = const TextEditingValue(
//       text: _initialValue,
//       selection: TextSelection.collapsed(offset: _initialValue.length),
//     );
//     _updateStatusMessage('已恢复示例文本');
//   }
//
//   Future<void> _copyRawText() async {
//     await Clipboard.setData(ClipboardData(text: _controller.text));
//     if (!mounted) {
//       return;
//     }
//
//     _updateStatusMessage('原始字符串已复制，可以看到图片仍以 [img:alias] token 形式保存。');
//     ScaffoldMessenger.of(
//       context,
//     ).showSnackBar(const SnackBar(content: Text('已复制当前实际保存字符串')));
//   }
// }
//
// class _SelectablePreview extends StatelessWidget {
//   const _SelectablePreview({required this.onTokenTap});
//
//   static const String _value =
//       _ExtendedTextFieldDemoViewState._selectablePreviewValue;
//
//   final ValueChanged<String> onTokenTap;
//
//   @override
//   Widget build(BuildContext context) {
//     return ExtendedSelectableText(
//       _value,
//       specialTextSpanBuilder: _DemoSpecialTextSpanBuilder(
//         imageAssets: _ExtendedTextFieldDemoViewState._imageAssets,
//         onTokenTap: onTokenTap,
//       ),
//       style: const TextStyle(
//         fontSize: 16,
//         height: 1.55,
//         color: Color(0xFF111827),
//       ),
//     );
//   }
// }
//
// class _MetricChip extends StatelessWidget {
//   const _MetricChip({required this.label, required this.value});
//
//   final String label;
//   final String value;
//
//   @override
//   Widget build(BuildContext context) {
//     return DecoratedBox(
//       decoration: BoxDecoration(
//         color: Colors.white,
//         borderRadius: BorderRadius.circular(999),
//         border: Border.all(color: const Color(0xFFDCE3F0)),
//       ),
//       child: Padding(
//         padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
//         child: Row(
//           mainAxisSize: MainAxisSize.min,
//           children: <Widget>[
//             Text(
//               label,
//               style: Theme.of(context).textTheme.bodySmall?.copyWith(
//                 color: Colors.black54,
//                 fontWeight: FontWeight.w600,
//               ),
//             ),
//             const SizedBox(width: 8),
//             Text(
//               value,
//               style: Theme.of(
//                 context,
//               ).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w700),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }
//
// class _SyntaxBadge extends StatelessWidget {
//   const _SyntaxBadge({required this.label, required this.example});
//
//   final String label;
//   final String example;
//
//   @override
//   Widget build(BuildContext context) {
//     return DecoratedBox(
//       decoration: BoxDecoration(
//         color: Colors.white,
//         borderRadius: BorderRadius.circular(16),
//         border: Border.all(color: const Color(0xFFDCE3F0)),
//       ),
//       child: Padding(
//         padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           mainAxisSize: MainAxisSize.min,
//           children: <Widget>[
//             Text(
//               label,
//               style: Theme.of(
//                 context,
//               ).textTheme.labelMedium?.copyWith(color: Colors.black54),
//             ),
//             const SizedBox(height: 4),
//             Text(
//               example,
//               style: Theme.of(context).textTheme.bodyMedium?.copyWith(
//                 fontWeight: FontWeight.w700,
//                 color: const Color(0xFF1D4ED8),
//               ),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }
//
// class _SectionCard extends StatelessWidget {
//   const _SectionCard({
//     required this.title,
//     required this.subtitle,
//     required this.child,
//     this.trailing,
//   });
//
//   final String title;
//   final String subtitle;
//   final Widget child;
//   final Widget? trailing;
//
//   @override
//   Widget build(BuildContext context) {
//     return DecoratedBox(
//       decoration: BoxDecoration(
//         color: Colors.white,
//         borderRadius: BorderRadius.circular(24),
//         border: Border.all(color: const Color(0xFFE2E8F0)),
//         boxShadow: const <BoxShadow>[
//           BoxShadow(
//             color: Color(0x0D0F172A),
//             blurRadius: 18,
//             offset: Offset(0, 8),
//           ),
//         ],
//       ),
//       child: Padding(
//         padding: const EdgeInsets.all(20),
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: <Widget>[
//             Row(
//               crossAxisAlignment: CrossAxisAlignment.start,
//               children: <Widget>[
//                 Expanded(
//                   child: Column(
//                     crossAxisAlignment: CrossAxisAlignment.start,
//                     children: <Widget>[
//                       Text(
//                         title,
//                         style: Theme.of(context).textTheme.titleMedium
//                             ?.copyWith(fontWeight: FontWeight.w700),
//                       ),
//                       const SizedBox(height: 6),
//                       Text(
//                         subtitle,
//                         style: Theme.of(context).textTheme.bodyMedium?.copyWith(
//                           color: Colors.black54,
//                           height: 1.45,
//                         ),
//                       ),
//                     ],
//                   ),
//                 ),
//                 if (trailing != null) ...<Widget>[
//                   const SizedBox(width: 12),
//                   trailing!,
//                 ],
//               ],
//             ),
//             const SizedBox(height: 16),
//             child,
//           ],
//         ),
//       ),
//     );
//   }
// }
//
// class _DemoSpecialTextSpanBuilder extends SpecialTextSpanBuilder {
//   _DemoSpecialTextSpanBuilder({
//     required this.imageAssets,
//     required this.onTokenTap,
//   });
//
//   final Map<String, String> imageAssets;
//   final ValueChanged<String> onTokenTap;
//
//   @override
//   SpecialText? createSpecialText(
//     String flag, {
//     TextStyle? textStyle,
//     SpecialTextGestureTapCallback? onTap,
//     required int index,
//   }) {
//     if (flag.isEmpty) {
//       return null;
//     }
//
//     if (isStart(flag, _AssetImageText.flag)) {
//       return _AssetImageText(
//         textStyle,
//         imageAssets: imageAssets,
//         onTokenTap: onTokenTap,
//         start: index - (_AssetImageText.flag.length - 1),
//       );
//     }
//
//     if (isStart(flag, _MentionText.flag)) {
//       return _MentionText(
//         textStyle,
//         onTokenTap: onTokenTap,
//         start: index - (_MentionText.flag.length - 1),
//       );
//     }
//
//     if (isStart(flag, _TopicText.flag)) {
//       return _TopicText(
//         textStyle,
//         onTokenTap: onTokenTap,
//         start: index - (_TopicText.flag.length - 1),
//       );
//     }
//
//     return null;
//   }
// }
//
// class _MentionText extends SpecialText {
//   _MentionText(
//     TextStyle? textStyle, {
//     required this.onTokenTap,
//     required this.start,
//   }) : super(flag, ' ', textStyle);
//
//   static const String flag = '@';
//
//   final ValueChanged<String> onTokenTap;
//   final int start;
//
//   @override
//   InlineSpan finishText() {
//     final String token = toString();
//     final TextStyle style = (textStyle ?? const TextStyle()).copyWith(
//       color: const Color(0xFF0369A1),
//       fontWeight: FontWeight.w700,
//     );
//
//     return BackgroundTextSpan(
//       background: Paint()..color = const Color(0xFFE0F2FE),
//       clipBorderRadius: BorderRadius.circular(999),
//       text: token,
//       actualText: token,
//       start: start,
//       deleteAll: true,
//       style: style,
//       recognizer: TapGestureRecognizer()
//         ..onTap = () {
//           onTokenTap(token.trim());
//         },
//     );
//   }
// }
//
// class _TopicText extends SpecialText {
//   _TopicText(
//     TextStyle? textStyle, {
//     required this.onTokenTap,
//     required this.start,
//   }) : super(flag, flag, textStyle);
//
//   static const String flag = '#';
//
//   final ValueChanged<String> onTokenTap;
//   final int start;
//
//   @override
//   InlineSpan finishText() {
//     final String token = toString();
//     final TextStyle style = (textStyle ?? const TextStyle()).copyWith(
//       color: const Color(0xFF7C3AED),
//       fontWeight: FontWeight.w700,
//     );
//
//     return SpecialTextSpan(
//       text: token,
//       actualText: token,
//       start: start,
//       deleteAll: true,
//       style: style,
//       recognizer: TapGestureRecognizer()
//         ..onTap = () {
//           onTokenTap(token);
//         },
//     );
//   }
// }
//
// class _AssetImageText extends SpecialText {
//   _AssetImageText(
//     TextStyle? textStyle, {
//     required this.imageAssets,
//     required this.onTokenTap,
//     required this.start,
//   }) : super(flag, ']', textStyle);
//
//   static const String flag = '[img:';
//
//   final Map<String, String> imageAssets;
//   final ValueChanged<String> onTokenTap;
//   final int start;
//
//   @override
//   InlineSpan finishText() {
//     final String token = toString();
//     final String alias = getContent();
//     final String? assetPath = imageAssets[alias];
//
//     return ExtendedWidgetSpan(
//       start: start,
//       actualText: token,
//       alignment: PlaceholderAlignment.middle,
//       child: Padding(
//         padding: const EdgeInsets.symmetric(horizontal: 2),
//         child: GestureDetector(
//           onTap: () {
//             onTokenTap(token);
//           },
//           child: assetPath == null
//               ? DecoratedBox(
//                   decoration: BoxDecoration(
//                     color: const Color(0xFFFEF2F2),
//                     borderRadius: BorderRadius.circular(12),
//                     border: Border.all(color: const Color(0xFFFECACA)),
//                   ),
//                   child: Padding(
//                     padding: const EdgeInsets.symmetric(
//                       horizontal: 12,
//                       vertical: 8,
//                     ),
//                     child: Text(
//                       'unknown:$alias',
//                       style: const TextStyle(
//                         color: Color(0xFFB91C1C),
//                         fontWeight: FontWeight.w700,
//                       ),
//                     ),
//                   ),
//                 )
//               : ClipRRect(
//                   borderRadius: BorderRadius.circular(14),
//                   child: Stack(
//                     alignment: Alignment.bottomLeft,
//                     children: <Widget>[
//                       Image.asset(
//                         assetPath,
//                         width: 72,
//                         height: 52,
//                         fit: BoxFit.cover,
//                       ),
//                       Container(
//                         margin: const EdgeInsets.all(6),
//                         padding: const EdgeInsets.symmetric(
//                           horizontal: 6,
//                           vertical: 2,
//                         ),
//                         decoration: BoxDecoration(
//                           color: Colors.black54,
//                           borderRadius: BorderRadius.circular(999),
//                         ),
//                         child: Text(
//                           alias,
//                           style: const TextStyle(
//                             color: Colors.white,
//                             fontSize: 11,
//                             fontWeight: FontWeight.w600,
//                           ),
//                         ),
//                       ),
//                     ],
//                   ),
//                 ),
//         ),
//       ),
//     );
//   }
// }
