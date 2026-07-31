import 'package:flutter/material.dart';
import 'package:ui/views/home/home_shell.dart';

class StarredView extends StatelessWidget {
  const StarredView({super.key});

  @override
  Widget build(BuildContext context) {
    return HomeShell(
      child: Center(
        child: Text('Starred', style: TextStyle(color: Colors.white)),
      ),
    );
  }
}
