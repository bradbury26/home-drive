import 'package:flutter/material.dart';

class Logo extends StatelessWidget {
  final double size;

  const Logo({super.key, required this.size});

  @override
  Widget build(BuildContext context) {
    final double padding = size / 6;

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.primaryContainer,
        borderRadius: BorderRadius.all(Radius.circular(15)),
      ),
      child: Stack(
        alignment: .center,
        children: [
          Icon(
            Icons.folder_rounded,
            color: Theme.of(context).colorScheme.primary,
            size: size - padding,
          ),
          Icon(
            Icons.home_work_rounded,
            color: Theme.of(context).colorScheme.onPrimary,
            size: (size / 1.8) - 16,
          ),
        ],
      ),
    );
  }
}
