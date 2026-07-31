import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/browser_client.dart';
import 'package:http_interceptor/http_interceptor.dart';
import 'package:ui/util/http_interceptors.dart';

BaseClient createClient(Ref ref) => InterceptedClient.build(
  interceptors: [AuthenticatedInterceptor(ref)],
  client: (BrowserClient()..withCredentials = true),
);
