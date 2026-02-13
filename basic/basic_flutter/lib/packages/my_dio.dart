import 'package:basic_flutter/common/dio.dart';
import 'package:basic_flutter/common/log.dart';
import 'package:basic_flutter/common/urls.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

/// dio
/// https://pub.dev/packages/dio
class MyDio extends StatelessWidget {
  const MyDio({super.key});

  @override
  Widget build(BuildContext context) {
    return const DioRoute(title: 'Flutter Dio Example');
  }
}

class DioRoute extends StatefulWidget {
  const DioRoute({super.key, required this.title});

  final String title;

  @override
  State<DioRoute> createState() => _DioRouteState();
}

class _DioRouteState extends State<DioRoute> {
  String info = "";
  bool isLoading = false;

  Future<void> _loginByDio() async {
    if (isLoading) return;

    setState(() {
      isLoading = true;
      info = "加载中...";
    });

    try {
      final Response<dynamic> response = await DioClient.instance.post<dynamic>(
        Urls().login,
        data: <String, String>{
          Urls().keyUsername: Urls().valueUsername,
          Urls().keyPassword: Urls().valuePassword,
        },
      );

      setState(() {
        info = response.data.toString();
      });
    } on DioException catch (e) {
      String errorMessage;
      switch (e.type) {
        case DioExceptionType.connectionTimeout:
          errorMessage = "连接超时，请检查网络";
          break;
        case DioExceptionType.sendTimeout:
          errorMessage = "发送超时，请重试";
          break;
        case DioExceptionType.receiveTimeout:
          errorMessage = "接收超时，请重试";
          break;
        case DioExceptionType.badResponse:
          final int? statusCode = e.response?.statusCode;
          errorMessage = "服务器错误: $statusCode";
          break;
        case DioExceptionType.cancel:
          errorMessage = "请求已取消";
          break;
        case DioExceptionType.connectionError:
          errorMessage = "网络连接失败，请检查网络";
          break;
        default:
          errorMessage = "请求失败: ${e.message}";
      }
      logError("Dio请求失败", e);
      setState(() {
        info = errorMessage;
      });
    } catch (e) {
      logError("未知错误", e);
      setState(() {
        info = "未知错误: $e";
      });
    } finally {
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text(info, textAlign: TextAlign.center),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: isLoading ? null : () => _loginByDio(),
        tooltip: 'Login',
        child: isLoading
            ? const SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 2,
                ),
              )
            : const Icon(Icons.login),
      ),
    );
  }
}
