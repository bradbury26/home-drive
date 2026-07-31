import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:ui/util/app_storage.dart';

class ServerSetupView extends StatelessWidget {
  final _formKey = GlobalKey<FormState>();
  final _serverAddressController = TextEditingController();

  ServerSetupView({super.key});

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return Form(
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
            "Setup",
          ),
          SizedBox(height: 64),
          TextFormField(
            style: TextStyle(color: colorScheme.onSurface),
            decoration: InputDecoration(
              border: OutlineInputBorder(),
              labelText: "Server URL",
            ),
            controller: _serverAddressController,
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter a Server URL';
              }

              Uri? uri = Uri.tryParse(value);

              if (uri == null) {
                return 'Invalid URL';
              }

              return null;
            },
          ),
          SizedBox(height: 32),
          FilledButton(
            onPressed: () async {
              if (_formKey.currentState!.validate()) {
                await AppStorage.setServerAddress(
                  _serverAddressController.text.trim(),
                );

                if (context.mounted) {
                  context.go('/login');
                }
              }
            },
            child: Padding(
              padding: EdgeInsetsGeometry.symmetric(
                vertical: 12,
                horizontal: 24,
              ),
              child: Text(
                style: TextStyle(fontSize: 18, color: colorScheme.onPrimary),
                'Continue',
              ),
            ),
          ),
        ],
      ),
    );
  }
}
