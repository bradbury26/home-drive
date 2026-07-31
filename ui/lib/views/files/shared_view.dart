import 'package:flutter/material.dart';
import 'package:ui/views/home/home_shell.dart';

class SharedView extends StatelessWidget {
  const SharedView({super.key});

  @override
  Widget build(BuildContext context) {
    return HomeShell(
      child: Center(
        child: Text('Shared', style: TextStyle(color: Colors.white)),
      ),
    );
  }
}
