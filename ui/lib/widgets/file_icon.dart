import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class FileIcon extends StatelessWidget {
  final MaterialColor color;
  final IconData? iconData;
  final String? text;

  const FileIcon({
    super.key,
    this.color = Colors.lightBlue,
    this.iconData,
    this.text,
  }) : assert(
         iconData == null || text == null,
         'Only one of iconData or text can be set',
       );

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :brightness) = Theme.of(context);
    var brightnessColor = brightness == .dark ? color.shade300 : color.shade700;

    Widget child;

    if (iconData != null) {
      child = Icon(iconData, color: brightnessColor);
    } else if (text != null) {
      child = Padding(
        padding: EdgeInsets.all(10),
        child: Container(
          decoration: BoxDecoration(
            color: brightnessColor,
            borderRadius: BorderRadius.circular(2),
          ),
          child: Center(
            child: Text(
              text!,
              style: GoogleFonts.barlow(
                fontWeight: .w900,
                fontSize: 10,
                color: colorScheme.surfaceContainerHigh,
              ),
            ),
          ),
        ),
      );
    } else {
      child = Icon(Icons.question_mark);
    }

    return Container(
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: child,
    );
  }
}
