import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:ui/view_models/files/file_view_model.dart';

class BreadcrumbAppBar extends ConsumerWidget {
  final String directoryId;

  const BreadcrumbAppBar({super.key, required this.directoryId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);
    var fileDetails = ref.watch(fileDetailsProvider(directoryId));

    return SliverAppBar(
      floating: true,
      backgroundColor: colorScheme.surfaceContainer,
      title: fileDetails.whenOrNull(data: (data) => Text(data.name)),
      leading: IconButton(
        onPressed: () => context.pop(),
        icon: Icon(Icons.arrow_back),
      ),
    );
  }
}
