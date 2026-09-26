import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

/// ScreenUtil — 屏幕适配尺寸
///
/// 核心机制与避坑点：
/// 1. 初始化前提：`ScreenUtilInit` 必须在使用 `.w` / `.h` / `.sp` 前完成设计稿绑定。
/// 2. 适配语义：`.w` 按宽度、`.h` 按高度、`.sp` 按宽度缩放字号。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_screenutil
class ScreenUtilDemoPage extends BasicLayoutPage {
  const ScreenUtilDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ScreenUtilDemoPage> createState() =>
      _ScreenUtilDemoPageState();
}

class _ScreenUtilDemoPageState extends BasicLayoutPageState<ScreenUtilDemoPage> {
  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);

    return SingleChildScrollView(
      padding: EdgeInsets.all(16.w),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text('ScreenUtil 适配预览', style: theme.textTheme.titleMedium),
          const SizedBox(height: 16),
          Container(
            width: 375.w,
            height: 80.h,
            color: Colors.blue.shade100,
            alignment: Alignment.center,
            child: Text('375.w × 80.h', style: TextStyle(fontSize: 16.sp)),
          ),
          const SizedBox(height: 16),
          Text('12.sp 小字', style: TextStyle(fontSize: 12.sp)),
          const SizedBox(height: 8),
          Text('16.sp 正文字', style: TextStyle(fontSize: 16.sp)),
          const SizedBox(height: 8),
          Text('24.sp 标题字', style: TextStyle(fontSize: 24.sp)),
          const SizedBox(height: 16),
          Text(
            'screenWidth=${1.sw} screenHeight=${1.sh}',
            style: theme.textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}
