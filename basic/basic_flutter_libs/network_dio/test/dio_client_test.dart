import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:network_dio/network_dio.dart';
import 'package:test/test.dart';

void main() {
  test('发送表单请求并保留响应信息', () async {
    final _RecordingAdapter adapter = _RecordingAdapter(
      body: '{"errorCode":0,"errorMsg":"","data":{"value":1}}',
      statusCode: 200,
    );
    final Dio dio = Dio()..httpClientAdapter = adapter;
    final DioClient client = DioClient(
      dio: dio,
      baseUrl: 'https://example.com',
    );

    final NetworkResponse<Map<String, dynamic>> response = await client.post(
      '/login',
      body: <String, String>{'username': 'demo'},
      bodyType: RequestBodyType.form,
    );

    expect(adapter.lastRequest?.method, 'POST');
    expect(adapter.lastRequest?.contentType, Headers.formUrlEncodedContentType);
    expect(response.data, <String, dynamic>{'value': 1});
    expect(response.code, NetworkResponse.successCode);
    expect(response.isSuccess, isTrue);
  });

  test('非 2xx 响应转换为 NetworkException', () async {
    final Dio dio = Dio()
      ..httpClientAdapter = _RecordingAdapter(
        body: '{"message":"invalid input"}',
        statusCode: 422,
      );
    final DioClient client = DioClient(dio: dio);

    await expectLater(
      client.get<dynamic>('https://example.com/users'),
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

  test('decoder 转换业务类型', () async {
    final Dio dio = Dio()
      ..httpClientAdapter = _RecordingAdapter(
        body: '{"errorCode":0,"errorMsg":"","data":{"name":"Ada"}}',
        statusCode: 200,
      );
    final DioClient client = DioClient(dio: dio);

    final NetworkResponse<String> response = await client.get<String>(
      'https://example.com/user',
      decoder: (dynamic data) =>
          (data as Map<String, dynamic>)['name'] as String,
    );

    expect(response.data, 'Ada');
  });

  test('非零业务码保留在响应中', () async {
    final Dio dio = Dio()
      ..httpClientAdapter = _RecordingAdapter(
        body: '{"errorCode":10001,"errorMsg":"not logged in","data":null}',
        statusCode: 200,
      );
    final DioClient client = DioClient(dio: dio);

    final NetworkResponse<dynamic> response = await client.get<dynamic>(
      'https://example.com/user',
    );

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
    final Dio dio = Dio()
      ..httpClientAdapter = _RecordingAdapter(
        body: '{"errorCode":10001,"errorMsg":"","data":null}',
        statusCode: 200,
      );
    final DioClient client = DioClient(dio: dio);
    final NetworkResponse<void> response = await client.get<void>(
      'https://example.com/user',
    );

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
    final Dio dio = Dio()
      ..httpClientAdapter = _RecordingAdapter(
        body: '{"errorCode":0,"errorMsg":"","data":{"value":1}}',
        statusCode: 200,
      );
    final DioClient client = DioClient(dio: dio);

    await expectLater(
      client.get<Map<String, dynamic>>(
        'https://example.com/user',
        decoder: (_) => throw const FormatException('data parse failed'),
      ),
      throwsA(
        isA<NetworkException>().having(
          (NetworkException error) => error.code,
          'code',
          NetworkException.parseError,
        ),
      ),
    );
  });

  test('Dio 超时错误映射为统一错误码', () async {
    final Dio dio = Dio()..httpClientAdapter = _TimeoutAdapter();
    final DioClient client = DioClient(dio: dio);

    await expectLater(
      client.get<dynamic>('https://example.com/slow'),
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

  test('关闭包装器后拒绝新请求且不关闭注入的 Dio', () async {
    final _RecordingAdapter adapter = _RecordingAdapter(
      body: '{"errorCode":0,"errorMsg":"","data":null}',
      statusCode: 200,
    );
    final Dio dio = Dio()..httpClientAdapter = adapter;
    final DioClient client = DioClient(dio: dio)..close();

    await expectLater(
      client.get<dynamic>('https://example.com/'),
      throwsA(isA<StateError>()),
    );
    expect(adapter.closed, isFalse);
  });
}

class _RecordingAdapter implements HttpClientAdapter {
  _RecordingAdapter({required this.body, required this.statusCode});

  final String body;
  final int statusCode;
  RequestOptions? lastRequest;
  bool closed = false;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    lastRequest = options;
    return ResponseBody.fromString(
      body,
      statusCode,
      headers: <String, List<String>>{
        Headers.contentTypeHeader: <String>[Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {
    closed = true;
  }
}

class _TimeoutAdapter implements HttpClientAdapter {
  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) {
    throw DioException(
      requestOptions: options,
      type: DioExceptionType.connectionTimeout,
    );
  }

  @override
  void close({bool force = false}) {}
}
