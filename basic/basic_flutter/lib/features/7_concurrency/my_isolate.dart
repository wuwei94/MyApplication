import 'dart:async';
import 'dart:convert';
import 'dart:isolate';

import 'package:basic_flutter/core/constants/urls.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart';

/// Isolate 消息数据类
class IsolateMessage {
  final SendPort replyPort;
  final String url;

  const IsolateMessage({required this.replyPort, required this.url});
}

/// 列表项数据类
class PostItem {
  final String title;

  const PostItem({required this.title});

  factory PostItem.fromJson(Map<String, Object?> json) {
    return PostItem(title: json['title']?.toString() ?? '');
  }
}

///Isolate
class MyIsolate extends StatelessWidget {
  const MyIsolate({super.key});

  @override
  Widget build(BuildContext context) {
    return const IsolatePage(title: 'Flutter Isolate demo');
  }
}

class IsolatePage extends StatefulWidget {
  const IsolatePage({super.key, required this.title});

  final String title;

  @override
  State<IsolatePage> createState() => _IsolatePageState();
}

class _IsolatePageState extends State<IsolatePage> {
  List<PostItem> widgets = [];

  @override
  void initState() {
    super.initState();
    loadData();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final showLoadingDialog = widgets.isEmpty;
    if (showLoadingDialog) {
      return getProgressDialog();
    } else {
      return getListView();
    }
  }

  Widget getProgressDialog() {
    return const Center(child: CircularProgressIndicator());
  }

  ListView getListView() {
    return ListView.builder(
      itemCount: widgets.length,
      itemBuilder: (context, position) {
        return Padding(
          padding: const EdgeInsets.all(10),
          child: Text("Row ${widgets[position].title}"),
        );
      },
    );
  }

  Future<void> loadData() async {
    final ReceivePort receivePort = ReceivePort();
    await Isolate.spawn(dataLoader, receivePort.sendPort);

    final SendPort sendPort = await receivePort.cast<SendPort>().first;

    final ReceivePort responsePort = ReceivePort();
    sendPort.send(
      IsolateMessage(replyPort: responsePort.sendPort, url: Urls().posts),
    );

    final List<PostItem> posts = await responsePort
        .cast<List<PostItem>>()
        .first;

    setState(() {
      widgets = posts;
    });
  }

  // 隔离的入口点。
  // The entry point for the isolate.
  static Future<void> dataLoader(SendPort sendPort) async {
    // 打开接收端口以接收传入消息。
    // Open the ReceivePort for incoming messages.
    final ReceivePort port = ReceivePort();

    // 通知任何其他隔离此隔离侦听的端口。
    // Notify any other isolates what port this isolate listens to.
    sendPort.send(port.sendPort);

    await for (final message in port.cast<IsolateMessage>()) {
      final Response response = await get(Uri.parse(message.url));
      final decoded = jsonDecode(response.body);
      final List<dynamic> jsonList = decoded is List ? decoded : <Object?>[];
      final List<PostItem> posts = jsonList
          .whereType<Map<String, Object?>>()
          .map(PostItem.fromJson)
          .toList();
      message.replyPort.send(posts);
    }
  }
}
