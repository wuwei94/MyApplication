import 'package:basic_flutter/core/constants/urls.dart';
import 'package:flutter/material.dart';

/// http
/// https://pub.dev/packages/http
class HttpExample extends StatelessWidget {
  const HttpExample({super.key});

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

  Map<String, String> _buildLoginData() {
    return <String, String>{
      Urls.keyUsername: Urls.valueUsername,
      Urls.keyPassword: Urls.valuePassword,
    };
  }

  Future<void> _handleLogin() async {}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: _buildBody(),
      floatingActionButton: _buildFab(),
    );
  }

  Widget _buildBody() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text(info, textAlign: TextAlign.center),
          ),
        ],
      ),
    );
  }

  Widget _buildFab() {
    return FloatingActionButton(
      onPressed: isLoading ? null : _handleLogin,
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
    );
  }
}
