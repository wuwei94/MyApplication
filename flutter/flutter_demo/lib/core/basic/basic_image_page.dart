import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_control_page.dart';
import 'package:flutter_demo/core/basic/demo_action_list.dart';

/// 图片/动画类示例页面基类 — 对应 Android `BasicImageActivity`。
///
/// 布局结构：
/// - 上方展示：图片 / 动画预览区（[buildPreview] / [showImage]）
/// - 下方列表：操作列表（[buildList] / [onRecyclerClick]）
///
/// 约定与规范：
/// 1. 禁止点击上方预览区触发操作，所有演示行为必须由下方列表项触发。
/// 2. [showImage] 用于在交互中更新预览内容。
abstract class BasicImagePage extends BasicControlPage {
  const BasicImagePage({super.key, required super.title});
}

/// [BasicImagePage] 的状态基类。
abstract class BasicImagePageState<T extends BasicImagePage>
    extends BasicControlPageState<T> {
  ImageProvider? _image;

  /// 上方预览区；默认展示 [showImage] 设置的图片，子类可覆写为自定义画布。
  Widget buildPreview() {
    final ImageProvider? image = _image;
    if (image == null) {
      return const Center(child: Icon(Icons.image_outlined, size: 48));
    }
    return Image(image: image, fit: BoxFit.contain);
  }

  /// 在主线程语义下更新展示图片。
  void showImage(ImageProvider? image) {
    setState(() {
      _image = image;
    });
  }

  @override
  Widget buildBody() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Expanded(child: buildPreview()),
        DemoActionList(
          items: buildList(),
          onTap: onRecyclerClick,
          height: operationHeight,
        ),
      ],
    );
  }
}
