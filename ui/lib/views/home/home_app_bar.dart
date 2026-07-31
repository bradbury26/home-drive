import 'package:flutter/material.dart';
import 'package:ui/util/breakpoints.dart';

class HomeAppBar extends StatelessWidget {
  const HomeAppBar({super.key});

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return SliverAppBar(
      toolbarHeight: 72,
      backgroundColor: colorScheme.surfaceContainer,
      floating: true,
      centerTitle: true,
      title: LayoutBuilder(
        builder: (context, constraints) {
          var breakpointType = Breakpoints.forBoxConstraints(constraints);

          return switch (breakpointType) {
            .compact => _mobileSearchButton(context),
            BreakpointType() => _desktopSearchBar(context),
          };
        },
      ),
      actions: [
        Padding(padding: EdgeInsets.only(right: 16), child: CircleAvatar()),
      ],
    );
  }

  Widget _desktopSearchBar(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return SearchBar(
      backgroundColor: WidgetStatePropertyAll(colorScheme.surfaceContainerHigh),
      leading: Icon(Icons.search),
      elevation: WidgetStatePropertyAll(0),
      hintText: 'Search in Drive',
      hintStyle: WidgetStatePropertyAll(
        textTheme.bodyLarge?.copyWith(
          color: colorScheme.secondary,
          fontWeight: .w500,
        ),
      ),
    );
  }

  Widget _mobileSearchButton(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return FilledButton(
      onPressed: () {},
      style: FilledButton.styleFrom(
        backgroundColor: colorScheme.surfaceContainerHighest,
      ),
      child: SizedBox(
        height: 56,
        child: Center(
          child: Text(
            'Search in Drive',
            style: textTheme.bodyLarge?.copyWith(
              color: colorScheme.secondary,
              fontWeight: .w500,
            ),
          ),
        ),
      ),
    );
  }
}
