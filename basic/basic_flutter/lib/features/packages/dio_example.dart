import 'package:basic_flutter/core/constants/urls.dart';
import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/core/utils/network/dio.dart';
import 'package:flutter/material.dart';

/// dio
/// https://pub.dev/packages/dio
class DioExample extends StatelessWidget {
  const DioExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const DioRoute(title: 'Dio Example');
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
      final response = await DioClient.instance.post<dynamic>(
        Urls.login,
        data: <String, String>{
          Urls.keyUsername: Urls.valueUsername,
          Urls.keyPassword: Urls.valuePassword,
        },
      );

      setState(() {
        info = response.data.toString();
      });
    } on NetworkException catch (e) {
      // 统一使用 DioClient 封装后的 NetworkException
      logError("网络请求失败", e);
      setState(() {
        info = e.message;
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
