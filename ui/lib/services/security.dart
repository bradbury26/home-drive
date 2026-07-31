import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:ui/services/api_client.dart';

part 'security.g.dart';

@riverpod
Future<bool> login(Ref ref, String username, String password) async {
  var apiClient = ref.read(apiClientProvider);

  return await apiClient.login(username, password);
}

// class SecurityService {
//   final ApiClient apiClient;
//
//   SecurityService({required this.apiClient});
//
//   Future<bool> login(String username, String password) async {
//     return await apiClient.login(username, password);
//   }
// }
