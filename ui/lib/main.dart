import 'package:dynamic_color/dynamic_color.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:ui/router.dart';

void main() {
  GoRouter.optionURLReflectsImperativeAPIs = true;

  runApp(ProviderScope(child: const MainApp()));
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return DynamicColorBuilder(
      builder: (colorScheme, _) {
        final ThemeData theme = ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: colorScheme?.primary ?? Colors.lightBlueAccent,
          ),
          textTheme: GoogleFonts.googleSansTextTheme(),
          typography: Typography.material2021(),
        );

        final ThemeData darkTheme = theme.copyWith(
          colorScheme: ColorScheme.fromSeed(
            seedColor: colorScheme?.primary ?? Colors.lightBlueAccent,
            brightness: .dark,
          ),
        );

        return Consumer(
          builder: (context, ref, _) => MaterialApp.router(
            themeMode: .dark,
            darkTheme: darkTheme,
            theme: theme,
            routerConfig: ref.watch(goRouterProvider),
          ),
        );
      },
    );
  }
}
