import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart';

BaseClient createClient(Ref ref) => throw UnsupportedError(
  'Cannot create a client without dart:js_interop or dart:io.',
);
