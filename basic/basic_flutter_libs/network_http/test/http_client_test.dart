import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:network_http/network_http.dart';
import 'package:test/test.dart';

void main() {
  test('解析相对 URL 并发送表单请求', () async {
    late http.Request captured;
    final HttpClient client = HttpClient(
      baseUrl: 'https://example.com/api',
      client: MockClient((http.Request request) async {
        captured = request;
        return http.Response(
          '{"errorCode":0,"errorMsg":"","data":{"ok":true}}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        );
      }),
    );

    final NetworkResponse<Map<String, dynamic>> response = await client
        .post<Map<String, dynamic>>(
          'login',
          queryParameters: <String, dynamic>{'source': 1, 'tag': 'a'},
          body: <String, String>{'username': 'demo'},
          bodyType: RequestBodyType.form,
        )
        .value;

    expect(
      captured.url.toString(),
      'https://example.com/api/login?source=1&tag=a',
    );
    expect(captured.method, 'POST');
    expect(captured.body, 'username=demo');
    expect(response.data, <String, dynamic>{'ok': true});
    expect(response.code, NetworkResponse.successCode);
    expect(response.isSuccess, isTrue);
  });

  test('raw 请求体保留字节内容', () async {
    late http.Request captured;
    final HttpClient client = HttpClient(
      client: MockClient((http.Request request) async {
        captured = request;
        return http.Response(
          '{"errorCode":0,"errorMsg":"","data":null}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        );
      }),
    );

    await client
        .post<void>(
          'https://example.com/raw',
          body: <int>[0, 1, 255],
          bodyType: RequestBodyType.raw,
        )
        .value;

    expect(captured.bodyBytes, <int>[0, 1, 255]);
  });

  test('非 2xx 响应保留状态码和服务端消息', () async {
    final HttpClient client = HttpClient(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode(<String, String>{'message': 'invalid input'}),
          422,
          headers: <String, String>{'content-type': 'application/json'},
        ),
      ),
    );

    await expectLater(
      client.get<dynamic>('https://example.com/users').value,
      throwsA(
        isA<NetworkException>()
            .having((NetworkException error) => error.code, 'code', 422)
            .having(
              (NetworkException error) => error.message,
              'message',
              'invalid input',
            ),
      ),
    );
  });

  test('取消操作会触发底层 AbortableRequest', () async {
    final _AbortTrackingClient transport = _AbortTrackingClient();
    final HttpClient client = HttpClient(client: transport);
    final operation = client.get<dynamic>('https://example.com/slow');

    await transport.started.future;
    await operation.cancel();

    await transport.aborted.future.timeout(const Duration(seconds: 1));
  });

  test('decoder 转换业务类型', () async {
    final HttpClient client = HttpClient(
      client: MockClient(
        (_) async => http.Response(
          '{"errorCode":0,"errorMsg":"","data":{"name":"Ada"}}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        ),
      ),
    );

    final NetworkResponse<String> response = await client
        .get<String>(
          'https://example.com/user',
          decoder: (dynamic data) =>
              (data as Map<String, dynamic>)['name'] as String,
        )
        .value;

    expect(response.data, 'Ada');
  });

  test('非零业务码保留在响应中', () async {
    final HttpClient client = HttpClient(
      client: MockClient(
        (_) async => http.Response(
          '{"errorCode":10001,"errorMsg":"not logged in","data":null}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        ),
      ),
    );

    final NetworkResponse<dynamic> response = await client
        .get<dynamic>('https://example.com/user')
        .value;

    expect(response.code, 10001);
    expect(response.message, 'not logged in');
    expect(response.isSuccess, isFalse);
    expect(
      response.requireSuccess,
      throwsA(
        isA<NetworkException>()
            .having((NetworkException error) => error.code, 'code', 10001)
            .having(
              (NetworkException error) => error.cause,
              'cause',
              isA<ServerResultException>()
                  .having(
                    (ServerResultException error) => error.code,
                    'code',
                    10001,
                  )
                  .having(
                    (ServerResultException error) => error.message,
                    'message',
                    'not logged in',
                  ),
            ),
      ),
    );
  });

  test('空业务错误消息沿用 ApiException 的统一兜底', () async {
    final HttpClient client = HttpClient(
      client: MockClient(
        (_) async => http.Response(
          '{"errorCode":10001,"errorMsg":"","data":null}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        ),
      ),
    );
    final NetworkResponse<void> response = await client
        .get<void>('https://example.com/user')
        .value;

    expect(
      response.requireSuccess,
      throwsA(
        isA<NetworkException>()
            .having(
              (NetworkException error) => error.message,
              'message',
              NetworkException.defaultMessage,
            )
            .having(
              (NetworkException error) => error.cause,
              'cause',
              isA<ServerResultException>().having(
                (ServerResultException error) => error.message,
                'message',
                isEmpty,
              ),
            ),
      ),
    );
  });

  test('decoder 转换失败映射为解析异常', () async {
    final HttpClient client = HttpClient(
      client: MockClient(
        (_) async => http.Response(
          '{"errorCode":0,"errorMsg":"","data":{"value":1}}',
          200,
          headers: <String, String>{'content-type': 'application/json'},
        ),
      ),
    );

    await expectLater(
      client
          .get<Map<String, dynamic>>(
            'https://example.com/user',
            decoder: (_) => throw const FormatException('data parse failed'),
          )
          .value,
      throwsA(
        isA<NetworkException>().having(
          (NetworkException error) => error.code,
          'code',
          NetworkException.parseError,
        ),
      ),
    );
  });

  test('package:http 超时错误映射为统一错误码', () async {
    final HttpClient client = HttpClient(
      client: _HangingClient(),
      timeout: const Duration(milliseconds: 1),
    );

    await expectLater(
      client.get<dynamic>('https://example.com/slow').value,
      throwsA(
        isA<NetworkException>().having(
          (NetworkException error) => error.code,
          'code',
          NetworkException.timeoutError,
        ),
      ),
    );
  });

  test('NetworkException 空消息使用统一兜底', () {
    final StateError cause = StateError('cause');
    final NetworkException error = NetworkException(
      cause,
      NetworkException.unknown,
      message: ' ',
    );

    expect(error.cause, same(cause));
    expect(error.message, NetworkException.defaultMessage);
  });

  test('关闭包装器后拒绝请求且不关闭注入的 Client', () async {
    final _CloseTrackingClient transport = _CloseTrackingClient();
    final HttpClient client = HttpClient(client: transport)..close();

    await expectLater(
      client.get<dynamic>('https://example.com/').value,
      throwsA(isA<StateError>()),
    );
    expect(transport.closed, isFalse);
  });
}

class _AbortTrackingClient extends http.BaseClient {
  final Completer<void> started = Completer<void>();
  final Completer<void> aborted = Completer<void>();

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    started.complete();
    if (request is http.Abortable && request.abortTrigger != null) {
      request.abortTrigger!.then((_) => aborted.complete());
    }
    return Completer<http.StreamedResponse>().future;
  }
}

class _CloseTrackingClient extends http.BaseClient {
  bool closed = false;

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    throw UnimplementedError();
  }

  @override
  void close() {
    closed = true;
  }
}

class _HangingClient extends http.BaseClient {
  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    return Completer<http.StreamedResponse>().future;
  }
}
