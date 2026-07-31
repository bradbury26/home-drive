import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:ui/model/sort_model.dart';
import 'package:ui/view_models/files/file_view_model.dart';

class FileViewHeader extends ConsumerWidget {
  final String? parentId;

  const FileViewHeader({super.key, this.parentId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    var ThemeData(:colorScheme) = Theme.of(context);
    var fileListSort = ref.read(fileListSortProvider(parentId).notifier);
    var sort = ref.watch(fileListSortProvider(parentId));

    return Row(
      children: [
        MenuAnchor(
          style: MenuStyle(alignment: .topRight),
          builder: (context, controller, _) {
            return SizedBox(
              height: 28,
              child: TextButton.icon(
                style: ButtonStyle(
                  padding: WidgetStatePropertyAll(
                    EdgeInsets.symmetric(horizontal: 8),
                  ),
                ),
                onPressed: () {
                  if (controller.isOpen) {
                    controller.close();
                  } else {
                    controller.open();
                  }
                },
                label: Text(
                  sort.name,
                  style: TextStyle(
                    color: colorScheme.onSurface,
                    fontWeight: .bold,
                  ),
                ),
                icon: Container(
                  width: 28,
                  height: 28,
                  decoration: BoxDecoration(
                    color: colorScheme.onPrimary,
                    borderRadius: BorderRadius.circular(32),
                  ),
                  child: Icon(
                    sort.ascending ? Icons.arrow_upward : Icons.arrow_downward,
                    size: 18,
                    fontWeight: .bold,
                  ),
                ),
                iconAlignment: .end,
              ),
            );
          },
          menuChildren: [
            _MenuHeader('Sort by'),
            ...FileListSortNotifier.sortDefinitions.values.map(
              (sortDefinition) => _MenuSortItemButton(
                sortDefinition.name,
                onPressed: () => fileListSort.updateSort(sortDefinition),
                selected: sortDefinition.expression == sort.expression,
              ),
            ),
            Divider(),
            ..._sortMenuItems(sort, fileListSort),
          ],
        ),
      ],
    );
  }

  Iterable<Widget> _sortMenuItems(
    Sort sort,
    FileListSortNotifier fileListSort,
  ) {
    var SortDefinition(:ascendingText, :descendingText, :initialAscendingSort) =
        FileListSortNotifier.sortDefinitions[sort.expression]!;
    var ascendingSort = sort.ascending;

    var sortMenuItems = <Widget>[
      _MenuSortItemButton(
        ascendingText,
        onPressed: () => fileListSort.sortAscending(),
        selected: ascendingSort,
      ),
      _MenuSortItemButton(
        descendingText,
        onPressed: () => fileListSort.sortDescending(),
        selected: !ascendingSort,
      ),
    ];

    return initialAscendingSort ? sortMenuItems : sortMenuItems.reversed;
  }
}

class _MenuHeader extends StatelessWidget {
  final String text;

  const _MenuHeader(this.text, {super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Text(
        text,
        style: TextStyle(
          color: Theme.of(context).colorScheme.onSurface,
          fontSize: 14,
          fontWeight: .bold,
        ),
      ),
    );
  }
}

class _MenuSortItemButton extends StatelessWidget {
  final String text;
  final VoidCallback onPressed;
  final bool selected;

  const _MenuSortItemButton(
    this.text, {
    super.key,
    required this.onPressed,
    this.selected = false,
  });

  @override
  Widget build(BuildContext context) {
    var colorScheme = Theme.of(context).colorScheme;

    return MenuItemButton(
      onPressed: onPressed,
      style: MenuItemButton.styleFrom(
        backgroundColor: selected ? colorScheme.surfaceContainerHighest : null,
        padding: EdgeInsets.only(right: 16, left: 8),
        textStyle: TextStyle(color: colorScheme.onSurface, fontSize: 16),
      ),
      leadingIcon: SizedBox(
        width: 48,
        child: selected ? Icon(Icons.check) : null,
      ),
      child: Text(text),
    );
  }
}
