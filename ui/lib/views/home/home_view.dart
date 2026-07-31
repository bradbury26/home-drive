import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class HomeView extends StatelessWidget {
  final StatefulNavigationShell _navigationShell;

  const HomeView({super.key, required this._navigationShell});

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return LayoutBuilder(
      builder: (context, constraints) {
        var desktop = constraints.maxWidth >= 1200;

        return Scaffold(
          backgroundColor: colorScheme.surfaceContainer,
          drawer: Drawer(),
          drawerEnableOpenDragGesture: false,
          floatingActionButton: true
              ? SizedBox(
                  width: 80,
                  height: 80,
                  child: FloatingActionButton(
                    onPressed: () {},
                    child: Icon(Icons.add, size: 32),
                  ),
                )
              : null,
          body: SafeArea(child: _navigationShell),
          bottomNavigationBar: desktop
              ? null
              : NavigationBar(
                  selectedIndex: _navigationShell.currentIndex,
                  backgroundColor: colorScheme.surfaceContainerLow,
                  destinations: [
                    NavigationDestination(
                      icon: Icon(Icons.folder_open),
                      selectedIcon: Icon(Icons.folder),
                      label: 'Files',
                    ),
                    NavigationDestination(
                      icon: Icon(Icons.star_outline),
                      selectedIcon: Icon(Icons.star),
                      label: 'Starred',
                    ),
                    NavigationDestination(
                      icon: Icon(Icons.people_outline),
                      selectedIcon: Icon(Icons.people),
                      label: 'Shared',
                    ),
                  ],
                  onDestinationSelected: (selectedIndex) {
                    var initialLocation =
                        selectedIndex == _navigationShell.currentIndex;

                    _navigationShell.goBranch(
                      selectedIndex,
                      initialLocation: initialLocation,
                    );
                  },
                ),
        );
      },
    );
  }
}
