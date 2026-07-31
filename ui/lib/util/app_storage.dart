import 'package:shared_preferences/shared_preferences.dart';

class AppStorage {
  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();

  static final String _serverAddressKey = 'server_address';
  static final String _rememberMeKey = 'remember_me';
  static final String _usernameKey = 'username';

  static Future<String?> get serverAddress async =>
      await _prefs.getString(_serverAddressKey);

  static Future<void> setServerAddress(String serverAddress) async =>
      await _prefs.setString(_serverAddressKey, serverAddress);

  static Future<void> clearServerAddress() async =>
      _prefs.remove(_serverAddressKey);

  static Future<String?> get rememberMe async =>
      await _prefs.getString(_rememberMeKey);

  static Future<void> setRememberMe(String rememberMe) async =>
      await _prefs.setString(_rememberMeKey, rememberMe);

  static Future<void> clearRememberMe() async =>
      await _prefs.remove(_rememberMeKey);

  static Future<String?> get username async =>
      await _prefs.getString(_usernameKey);

  static Future<void> setUsername(String username) async =>
      await _prefs.setString(_usernameKey, username);

  static Future<void> clearUsername() async =>
      await _prefs.remove(_usernameKey);
}
