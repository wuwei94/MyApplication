import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/animations/fade_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/rotation_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/scale_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/size_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/slide_transition_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/backdrop_filter_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/clip_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/decoratedbox_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/opacity_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/shader_mask_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/alert_dialog_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/cupertino_dialogs_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/custom_dialog_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/date_picker_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/modal_bottom_sheet_example.dart';
import 'package:basic_flutter/demos/layout/gesture_interaction/gesturedetector_example.dart';
import 'package:basic_flutter/demos/layout/gesture_interaction/pop_scope_example.dart';
import 'package:flutter/widgets.dart';

final List<CatalogItem> layoutInteractionCatalogItems = <CatalogItem>[
  CatalogItem.catalog(
    path: 'gestures',
    title: '手势交互',
    subtitle: 'GestureDetector、PopScope',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'gesture-detector',
        title: 'GestureDetector',
        subtitle: '手势检测',
        pageBuilder: (BuildContext context) =>
            const GestureDetectorExample(title: 'GestureDetector'),
      ),
      CatalogItem.page(
        path: 'pop-scope',
        title: 'PopScope',
        subtitle: '返回拦截',
        pageBuilder: (BuildContext context) =>
            const PopScopeExample(title: 'PopScope'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'animations',
    title: '动画效果',
    subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'fade-transition',
        title: 'FadeTransition',
        subtitle: '淡入淡出动画',
        pageBuilder: (BuildContext context) =>
            const FadeTransitionExample(title: 'FadeTransition'),
      ),
      CatalogItem.page(
        path: 'scale-transition',
        title: 'ScaleTransition',
        subtitle: '缩放动画',
        pageBuilder: (BuildContext context) =>
            const ScaleTransitionExample(title: 'ScaleTransition'),
      ),
      CatalogItem.page(
        path: 'rotation-transition',
        title: 'RotationTransition',
        subtitle: '旋转动画',
        pageBuilder: (BuildContext context) =>
            const RotationTransitionExample(title: 'RotationTransition'),
      ),
      CatalogItem.page(
        path: 'size-transition',
        title: 'SizeTransition',
        subtitle: '尺寸动画',
        pageBuilder: (BuildContext context) =>
            const SizeTransitionExample(title: 'SizeTransition'),
      ),
      CatalogItem.page(
        path: 'slide-transition',
        title: 'SlideTransition',
        subtitle: '滑动动画',
        pageBuilder: (BuildContext context) =>
            const SlideTransitionExample(title: 'SlideTransition'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'dialogs',
    title: '弹窗与底部面板',
    subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'alert-dialog',
        title: 'AlertDialog',
        subtitle: 'Material弹窗',
        pageBuilder: (BuildContext context) =>
            const AlertDialogExample(title: 'AlertDialog'),
      ),
      CatalogItem.page(
        path: 'cupertino-dialogs',
        title: 'Cupertino Dialogs',
        subtitle: 'iOS风格弹窗',
        pageBuilder: (BuildContext context) =>
            const CupertinoDialogsExample(title: 'Cupertino Dialogs'),
      ),
      CatalogItem.page(
        path: 'custom-dialog',
        title: 'Custom Dialog',
        subtitle: '自定义弹窗',
        pageBuilder: (BuildContext context) =>
            const CustomDialogExample(title: 'Custom Dialog'),
      ),
      CatalogItem.page(
        path: 'date-picker',
        title: 'DatePicker',
        subtitle: '日期选择器',
        pageBuilder: (BuildContext context) =>
            const DatePickerExample(title: 'DatePicker'),
      ),
      CatalogItem.page(
        path: 'modal-bottom-sheet',
        title: 'ModalBottomSheet',
        subtitle: '底部面板',
        pageBuilder: (BuildContext context) =>
            const ModalBottomSheetExample(title: 'ModalBottomSheet'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'decorations',
    title: '装饰效果',
    subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'decorated-box',
        title: 'DecoratedBox',
        subtitle: '装饰盒子',
        pageBuilder: (BuildContext context) =>
            const DecoratedBoxExample(title: 'DecoratedBox'),
      ),
      CatalogItem.page(
        path: 'opacity',
        title: 'Opacity',
        subtitle: '透明度',
        pageBuilder: (BuildContext context) =>
            const OpacityExample(title: 'Opacity'),
      ),
      CatalogItem.page(
        path: 'clip',
        title: 'Clip',
        subtitle: '裁剪',
        pageBuilder: (BuildContext context) => const ClipExample(title: 'Clip'),
      ),
      CatalogItem.page(
        path: 'backdrop-filter',
        title: 'BackdropFilter',
        subtitle: '背景滤镜',
        pageBuilder: (BuildContext context) =>
            const BackdropFilterExample(title: 'BackdropFilter'),
      ),
      CatalogItem.page(
        path: 'shader-mask',
        title: 'ShaderMask',
        subtitle: '着色器遮罩',
        pageBuilder: (BuildContext context) =>
            const ShaderMaskExample(title: 'ShaderMask'),
      ),
    ],
  ),
];
