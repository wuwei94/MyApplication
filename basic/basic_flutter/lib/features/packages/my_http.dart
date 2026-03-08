import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/core/constants/urls.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

/// http
/// https://pub.dev/packages/http
class MyHttp extends StatelessWidget {
  const MyHttp({super.key});

  @override
  Widget build(BuildContext context) {
    return const HttpRoute(title: 'Http Example');
  }
}

class HttpRoute extends StatefulWidget {
  const HttpRoute({super.key, required this.title});

  final String title;

  @override
  State<HttpRoute> createState() => _HttpRouteState();
}

class _HttpRouteState extends State<HttpRoute> {
  String info = "";
  bool isLoading = false;

  Future<void> _loginByHttp() async {
    if (isLoading) return;

    setState(() {
      isLoading = true;
      info = "加载中...";
    });

    try {
      final response = await http.post(
        Uri.parse(Urls().login),
        // headers: <String, String>{
        //   'Content-Type': 'application/x-www-form-urlencoded',
        // },
        body: <String, String>{
          Urls().keyUsername: Urls().valueUsername,
          Urls().keyPassword: Urls().valuePassword,
        },
      );

      setState(() {
        info = response.body.toString();
      });
    } on http.ClientException catch (e) {
      logError("Http请求失败", e);
      setState(() {
        info = "网络连接失败，请检查网络";
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
        onPressed: isLoading ? null : () => _loginByHttp(),
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
