import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:http_interceptor/http_interceptor.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:ui/model/files/list_objects_response.dart';
import 'package:ui/model/files/object_model.dart';
import 'package:ui/util/app_storage.dart';
import 'package:ui/util/http_client_stub.dart'
    if (dart.library.js_interop) 'package:ui/util/browser_http_client.dart'
    if (dart.library.io) 'package:ui/util/io_http_client.dart';

part 'api_client.g.dart';

@riverpod
ApiClient apiClient(Ref ref) => ApiClient(createClient(ref));

class ApiClient {
  final http.Client _client;

  const ApiClient(this._client);

  Future<bool> refreshToken() async {
    var uri = (await _getBaseUri()).resolve('/api/user/token/refresh');

    var response = await _client.post(uri);

    return response.statusCode == 200;
  }

  Future<bool> login(String username, String password) async {
    var uri = (await _getBaseUri()).resolve('/api/login');

    var formData = <String, String>{};
    formData['username'] = username;
    formData['password'] = password;

    var request = http.MultipartRequest('POST', uri)..fields.addAll(formData);
    var response = await _client.send(request);

    return response.statusCode == 200;
  }

  Future<ListObjectsResponse> listObjects({
    required int totalItems,
    String? parentId,
    String? continuationToken,
    required String sort,
  }) async {
    var queryParameters = <String, String>{};
    queryParameters['size'] = totalItems.toString();
    queryParameters['sort'] = sort;

    if (parentId != null) {
      queryParameters['parentId'] = parentId;
    }

    if (continuationToken != null) {
      queryParameters['continuationToken'] = continuationToken;
    }

    var uri = (await _getBaseUri())
        .resolve('/api/object')
        .addQueryParams(params: queryParameters);

    var response = await _client.get(uri);

    if (response.statusCode == 200) {
      var json = jsonDecode(response.body) as Map<String, dynamic>;
      return ListObjectsResponse.fromJson(json);
    }

    throw Exception('Error');
  }

  Future<ObjectModel> getObjectDetails(String id) async {
    var uri = (await _getBaseUri()).resolve('/api/object/$id/details');

    var response = await _client.get(uri);

    if (response.statusCode == 200) {
      var json = jsonDecode(response.body) as Map<String, dynamic>;
      return ObjectModel.fromJson(json);
    }

    throw Exception('Error');
  }

  Future<String> createObjectUrl(String id) async {
    return (await _getBaseUri()).resolve('/api/object/$id').toString();
  }

  Future<Uri> _getBaseUri() async {
    if (kIsWeb && kDebugMode) {
      return Uri.http("localhost:8080");
    }

    var storedServerAddress = await AppStorage.serverAddress;

    return storedServerAddress != null ? Uri.parse(storedServerAddress) : Uri();
  }
}
