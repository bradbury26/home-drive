import 'package:flutter/material.dart';

class Breakpoints {
  static BreakpointType forBoxConstraints(BoxConstraints constraints) {
    return forWidth(constraints.maxWidth);
  }

  static BreakpointType forWidth(double width) {
    if (width >= 1600) {
      return .extraLarge;
    }

    if (width >= 1200) {
      return .large;
    }

    if (width >= 840) {
      return .expanded;
    }

    if (width >= 600) {
      return .medium;
    }

    return .compact;
  }
}

enum BreakpointType { compact, medium, expanded, large, extraLarge }
