import 'package:flutter/material.dart';
import 'package:ui/views/home/breadcrumb_app_bar.dart';
import 'package:ui/views/home/home_app_bar.dart';

class HomeShell extends StatefulWidget {
  final Widget _child;
  final String? _directoryId;

  const HomeShell({super.key, required this._child, this._directoryId});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  final GlobalKey<NestedScrollViewState> _nestedScrollViewStateKey =
      GlobalKey<NestedScrollViewState>();

  @override
  void initState() {
    super.initState();

    // WidgetsBinding.instance.addPostFrameCallback((_) {
    _nestedScrollViewStateKey.currentState?.outerController.addListener(
      _onOuterScroll,
    );
    // });
  }

  @override
  void dispose() {
    super.dispose();

    _nestedScrollViewStateKey.currentState?.outerController.removeListener(
      _onOuterScroll,
    );
  }

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return Container(
      color: colorScheme.surfaceContainer,
      child: NestedScrollView(
        key: _nestedScrollViewStateKey,
        floatHeaderSlivers: true,
        headerSliverBuilder: (context, _) {
          return widget._directoryId == null
              ? [const HomeAppBar()]
              : [BreadcrumbAppBar(directoryId: widget._directoryId!)];
        },
        body: widget._child,
      ),
    );
  }

  void _onOuterScroll() {
    var scrollController =
        _nestedScrollViewStateKey.currentState?.outerController;

    if (scrollController?.position.pixels ==
        scrollController?.position.maxScrollExtent) {}

    if (scrollController?.position.pixels ==
        scrollController?.position.minScrollExtent) {}
  }
}
