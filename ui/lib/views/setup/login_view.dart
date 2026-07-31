import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:ui/services/security.dart';
import 'package:ui/util/app_storage.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  final _formKey = GlobalKey<FormState>();
  final _usernameTextEditingController = TextEditingController();
  final _passwordTextEditingController = TextEditingController();

  bool _loginInvalid = false;

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);
    return Consumer(
      builder: (context, ref, _) => Form(
        key: _formKey,
        child: Column(
          mainAxisSize: .min,
          children: [
            Text(
              style: GoogleFonts.googleSans(
                textStyle: textTheme.displayMedium?.copyWith(
                  fontWeight: .bold,
                  color: colorScheme.secondary,
                ),
              ),
              "Login",
            ),
            SizedBox(height: 64),
            if (_loginInvalid)
              Padding(
                padding: EdgeInsets.only(bottom: 32),
                child: Card(
                  color: colorScheme.errorContainer,
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Row(
                      children: [
                        Icon(
                          Icons.error_outline_rounded,
                          color: colorScheme.onErrorContainer,
                          size: 32,
                        ),
                        Expanded(
                          child: Padding(
                            padding: EdgeInsets.symmetric(horizontal: 16),
                            child: Text(
                              'Invalid Username or Password',
                              style: textTheme.bodyLarge?.copyWith(
                                color: colorScheme.onErrorContainer,
                              ),
                            ),
                          ),
                        ),
                        IconButton(
                          onPressed: () =>
                              setState(() => _loginInvalid = false),
                          tooltip: 'Close',
                          icon: Icon(
                            Icons.close_rounded,
                            color: colorScheme.onErrorContainer,
                            size: 24,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            TextFormField(
              style: TextStyle(color: colorScheme.onSurface),
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: "Username",
              ),
              validator: _validateText,
              controller: _usernameTextEditingController,
              onFieldSubmitted: (_) => _submitForm(context, ref),
            ),
            SizedBox(height: 32),
            TextFormField(
              obscureText: true,
              style: TextStyle(color: colorScheme.onSurface),
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: "Password",
              ),
              validator: _validateText,
              controller: _passwordTextEditingController,
              onFieldSubmitted: (_) => _submitForm(context, ref),
            ),
            SizedBox(height: 32),
            FilledButton(
              onPressed: () => _submitForm(context, ref),
              child: Padding(
                padding: EdgeInsetsGeometry.symmetric(
                  vertical: 12,
                  horizontal: 24,
                ),
                child: Text(
                  style: TextStyle(fontSize: 18, color: colorScheme.onPrimary),
                  'Login',
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String? _validateText(String? value) {
    if (value == null || value.isEmpty) {
      return "Please enter some text";
    }

    return null;
  }

  Future<void> _submitForm(BuildContext context, WidgetRef ref) async {
    var username = _usernameTextEditingController.text.trim();
    var password = _passwordTextEditingController.text.trim();

    var loginSuccessful = await ref.read(
      loginProvider(username, password).future,
    );

    setState(() => _loginInvalid = !loginSuccessful);

    if (loginSuccessful) {
      AppStorage.setUsername(username);

      if (context.mounted) {
        context.go('/');
      }
    } else {
      AppStorage.clearUsername();
    }
  }
}
