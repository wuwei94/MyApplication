import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

/// Toast 工具类
void showToast(String msg) {
  Fluttertoast.showToast(
    msg: msg,
    // 提示消息
    toastLength: Toast.LENGTH_SHORT,
    // 显示时长
    gravity: ToastGravity.CENTER,
    // 显示位置
    timeInSecForIosWeb: 1,
    // iOS/Web 显示时长
    backgroundColor: Colors.white,
    // 背景色
    textColor: Colors.grey[850],
    // 文字颜色
    fontSize: 16.0, // 字体大小
  );
}
