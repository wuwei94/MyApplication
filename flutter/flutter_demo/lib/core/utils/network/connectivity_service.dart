import 'dart:collection';

import 'package:connectivity_plus/connectivity_plus.dart';

/// 网络连接状态工具
class ConnectivityService {
  ConnectivityService._();

  static final ConnectivityService instance = ConnectivityService._();

  static const List<ConnectivityResult> _preferredOrder = <ConnectivityResult>[
    ConnectivityResult.wifi,
    ConnectivityResult.mobile,
    ConnectivityResult.ethernet,
    ConnectivityResult.vpn,
    ConnectivityResult.bluetooth,
    ConnectivityResult.satellite,
    ConnectivityResult.other,
    ConnectivityResult.none,
  ];

  final Connectivity _connectivity = Connectivity();

  Stream<List<ConnectivityResult>> get onConnectivityChanged {
    return _connectivity.onConnectivityChanged;
  }

  Future<List<ConnectivityResult>> checkConnectivity() async {
    final List<ConnectivityResult> results = await _connectivity
        .checkConnectivity();
    return normalizeResults(results);
  }

  List<ConnectivityResult> normalizeResults(List<ConnectivityResult> results) {
    final LinkedHashSet<ConnectivityResult> deduplicatedResults =
        LinkedHashSet<ConnectivityResult>.from(results);
    final List<ConnectivityResult> normalizedResults =
        deduplicatedResults.toList()..sort(
          (ConnectivityResult left, ConnectivityResult right) =>
              _sortIndexOf(left).compareTo(_sortIndexOf(right)),
        );

    if (normalizedResults.isEmpty) {
      return const <ConnectivityResult>[ConnectivityResult.none];
    }

    if (normalizedResults.length > 1 &&
        normalizedResults.contains(ConnectivityResult.none)) {
      normalizedResults.remove(ConnectivityResult.none);
    }

    return normalizedResults;
  }

  bool hasConnection(List<ConnectivityResult> results) {
    final List<ConnectivityResult> normalizedResults = normalizeResults(
      results,
    );
    return normalizedResults.length != 1 ||
        normalizedResults.first != ConnectivityResult.none;
  }

  String summaryOf(List<ConnectivityResult> results) {
    final List<ConnectivityResult> normalizedResults = normalizeResults(
      results,
    );
    if (!hasConnection(normalizedResults)) {
      return '无网络连接';
    }

    return normalizedResults.map(labelOf).join(' / ');
  }

  String labelOf(ConnectivityResult result) {
    switch (result) {
      case ConnectivityResult.bluetooth:
        return '蓝牙';
      case ConnectivityResult.wifi:
        return 'Wi-Fi';
      case ConnectivityResult.ethernet:
        return '以太网';
      case ConnectivityResult.mobile:
        return '移动网络';
      case ConnectivityResult.none:
        return '无网络';
      case ConnectivityResult.vpn:
        return 'VPN';
      case ConnectivityResult.satellite:
        return '卫星网络';
      case ConnectivityResult.other:
        return '其他网络';
    }
  }

  int _sortIndexOf(ConnectivityResult result) {
    return _preferredOrder.indexOf(result);
  }
}
