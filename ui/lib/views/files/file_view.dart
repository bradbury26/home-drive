import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:ui/model/files/object_model.dart';
import 'package:ui/services/file_manager.dart';
import 'package:ui/view_models/files/file_view_model.dart';
import 'package:ui/views/files/file_view_header.dart';
import 'package:ui/views/home/home_shell.dart';
import 'package:ui/widgets/file_icon.dart';

class FileView extends StatelessWidget {
  final String? parentId;

  const FileView({super.key, this.parentId});

  @override
  Widget build(BuildContext context) {
    return HomeShell(
      directoryId: parentId,
      child: Consumer(
        builder: (context, ref, _) {
          var ThemeData(:colorScheme, :textTheme) = Theme.of(context);
          var fileList = fileListProvider(parentId);
          var fileListValue = ref.watch(fileList);

          return fileListValue.when(
            skipLoadingOnReload: true,
            data: (files) => RefreshIndicator(
              onRefresh: () => ref.refresh(fileList.future),
              backgroundColor: colorScheme.onPrimary,
              child: NotificationListener<ScrollNotification>(
                onNotification: ref.read(fileList.notifier).onScroll,
                child: ListView.builder(
                  padding: EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  itemExtent: 64,
                  itemCount: files.length + 2,
                  itemBuilder: (context, index) {
                    if (index == 0) {
                      return Padding(
                        padding: EdgeInsets.symmetric(vertical: 1),
                        child: Container(
                          decoration: BoxDecoration(
                            color: colorScheme.surface,
                            borderRadius: BorderRadius.vertical(
                              top: Radius.circular(16),
                              bottom: Radius.circular(4),
                            ),
                          ),
                          child: const FileViewHeader(),
                        ),
                      );
                    }

                    if (index == files.length + 1) {
                      return Padding(
                        padding: EdgeInsets.symmetric(vertical: 1),
                        child: Container(
                          decoration: BoxDecoration(
                            color: colorScheme.surface,
                            borderRadius: BorderRadius.vertical(
                              top: Radius.circular(4),
                              bottom: Radius.circular(16),
                            ),
                          ),
                          child: fileListValue.whenOrNull(
                            skipLoadingOnReload: true,
                            loading: () =>
                                Center(child: CircularProgressIndicator()),
                          ),
                        ),
                      );
                    }

                    var model = files[index - 1];

                    return _FileListTile(
                      key: ValueKey(model.name),
                      model: model,
                    );
                  },
                ),
              ),
            ),
            error: (_, _) => Container(),
            loading: () => Center(child: CircularProgressIndicator()),
          );
        },
      ),
    );
  }
}

class _FileListTile extends StatelessWidget {
  final ObjectModel model;

  const _FileListTile({super.key, required this.model});

  @override
  Widget build(BuildContext context) {
    var ThemeData(:colorScheme, :textTheme) = Theme.of(context);

    return Padding(
      padding: EdgeInsets.symmetric(vertical: 1),
      child: Material(
        color: colorScheme.surface,
        borderRadius: BorderRadius.circular(4),
        clipBehavior: .hardEdge,
        child: Consumer(
          builder: (context, ref, _) {
            var children = <Widget>[
              ListTile(
                visualDensity: VisualDensity(
                  horizontal: VisualDensity.minimumDensity,
                  vertical: VisualDensity.minimumDensity,
                ),
                contentPadding: EdgeInsets.only(left: 8, top: 3, bottom: 3),
                title: Text(
                  model.name,
                  overflow: .ellipsis,
                  style: textTheme.bodyLarge?.copyWith(
                    color: colorScheme.onSurface,
                  ),
                ),
                subtitle: Text(
                  'Modified ${DateFormat('d MMM yyyy').format(model.lastModified)}',
                  style: textTheme.labelMedium?.copyWith(
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
                leading: _createLeadingIcon(colorScheme),
                trailing: IconButton(
                  onPressed: () {},
                  icon: Icon(Icons.more_vert, color: colorScheme.onSurface),
                ),
                onTap: () {
                  if (model.objectType == 'DIRECTORY') {
                    context.push('/folders/${model.id}');
                  } else {
                    ref.watch(downloadFileProvider(model.id));

                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Downloading ${model.name}')),
                    );
                  }
                },
              ),
            ];

            var downloadProgress = ref.watch(
              downloadProgressProvider(model.id),
            );
            if (downloadProgress != -1) {
              children.add(
                Positioned(
                  bottom: 0,
                  left: 4,
                  right: 4,
                  child: LinearProgressIndicator(
                    year2023: false,
                    value: downloadProgress,
                  ),
                ),
              );
            }

            return Stack(children: children);
          },
        ),
      ),
    );
  }

  Widget _createLeadingIcon(ColorScheme colorScheme) {
    var mediaType = model.mediaType;
    Widget? child;

    if (model.objectType == 'DIRECTORY') {
      child = Icon(Icons.folder, size: 40);
    } else if (mediaType != null) {
      child = _mediaTypeIcon(mediaType);
    }

    return SizedBox(width: 40, height: 40, child: child);
  }

  Widget _mediaTypeIcon(String mediaType) {
    return switch (mediaType) {
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document' =>
        FileIcon(color: Colors.blue, text: 'W'),
      'application/zip' => FileIcon(
        color: Colors.grey,
        iconData: Icons.folder_zip,
      ),
      'application/pdf' => FileIcon(color: Colors.red, text: 'PDF'),
      'image/png' => FileIcon(color: Colors.red, iconData: Icons.image),
      String() => FileIcon(iconData: Icons.insert_drive_file),
    };
  }
}
