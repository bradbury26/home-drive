import 'dart:async';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http_interceptor/http_interceptor.dart';
import 'package:ui/router.dart';
import 'package:ui/util/app_storage.dart';

class AuthenticatedInterceptor implements HttpInterceptor {
  final Ref ref;

  const AuthenticatedInterceptor(this.ref);

  @override
  FutureOr<BaseRequest> interceptRequest({required BaseRequest request}) =>
      request;

  @override
  FutureOr<BaseResponse> interceptResponse({
    required BaseResponse response,
  }) async {
    if (response.statusCode == 403) {
      await AppStorage.clearRememberMe();
      await AppStorage.clearUsername();

      ref.read(goRouterProvider).go('/login');
    }

    return response;
  }

  @override
  FutureOr<bool> shouldInterceptRequest({required BaseRequest request}) =>
      false;

  @override
  FutureOr<bool> shouldInterceptResponse({required BaseResponse response}) => ![
    '/api/login',
    '/api/user/token/refresh',
  ].contains(response.request?.url.path);
}

class StoreRememberMeCookieInterceptor implements HttpInterceptor {
  @override
  FutureOr<BaseRequest> interceptRequest({required BaseRequest request}) =>
      request;

  @override
  FutureOr<BaseResponse> interceptResponse({
    required BaseResponse response,
  }) async {
    var headers = response.headersSplitValues;

    if (headers.containsKey('set-cookie')) {
      try {
        var value = headers['set-cookie']!
            .map(Cookie.fromSetCookieValue)
            .firstWhere((cookie) => cookie.name == 'remember-me');

        await AppStorage.setRememberMe(value.value);
      } on StateError {
        // Ignore if missing cookie
      }
    }

    return response;
  }

  @override
  FutureOr<bool> shouldInterceptRequest({required BaseRequest request}) =>
      false;

  @override
  FutureOr<bool> shouldInterceptResponse({required BaseResponse response}) =>
      true;
}

class AttachRememberMeCookieInterceptor implements HttpInterceptor {
  @override
  FutureOr<BaseRequest> interceptRequest({required BaseRequest request}) async {
    var rememberMe = await AppStorage.rememberMe;

    if (rememberMe != null) {
      request.headers[HttpHeaders.cookieHeader] = 'remember-me=$rememberMe';
    }

    return request;
  }

  @override
  FutureOr<BaseResponse> interceptResponse({required BaseResponse response}) =>
      response;

  @override
  FutureOr<bool> shouldInterceptRequest({required BaseRequest request}) => true;

  @override
  FutureOr<bool> shouldInterceptResponse({required BaseResponse response}) =>
      false;
}
