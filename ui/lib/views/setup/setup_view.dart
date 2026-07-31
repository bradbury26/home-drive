import 'package:flutter/material.dart';
import 'package:ui/widgets/logo.dart';

class SetupView extends StatelessWidget {
  final Widget child;

  const SetupView({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    var colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.surface,
      body: Center(
        child: Padding(
          padding: EdgeInsets.all(8),
          child: SizedBox(
            width: 600,
            child: SingleChildScrollView(
              child: Card(
                color: colorScheme.surfaceContainer,
                child: Padding(
                  padding: EdgeInsets.all(32),
                  child: Column(
                    mainAxisSize: .min,
                    children: [Logo(size: 96), SizedBox(height: 16), child],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
