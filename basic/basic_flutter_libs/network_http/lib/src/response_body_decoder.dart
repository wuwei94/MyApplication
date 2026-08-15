import 'dart:convert';

dynamic decodeNetworkResponseBody(String responseBody, String? contentType) {
  if (responseBody.isEmpty) return null;

  final String mimeType = (contentType ?? '')
      .split(';')
      .first
      .trim()
      .toLowerCase();
  if (mimeType != 'application/json' && !mimeType.endsWith('+json')) {
    return responseBody;
  }

  try {
    return jsonDecode(responseBody);
  } on FormatException {
    return responseBody;
  }
}
