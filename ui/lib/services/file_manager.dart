import 'dart:io';

import 'package:background_downloader/background_downloader.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:ui/services/api_client.dart';
import 'package:ui/util/app_storage.dart';

part 'file_manager.g.dart';

@riverpod
class DownloadProgress extends _$DownloadProgress {
  @override
  double build(String id) {
    return -1;
  }

  set progress(double progress) => state = progress;
}

@riverpod
Future<void> downloadFile(Ref ref, String id) async {
  var fileDownloader = FileDownloader().configureNotification(
    running: TaskNotification('Downloading', 'file: {filename}'),
    complete: TaskNotification('Download complete', 'file: {filename}'),
    progressBar: true,
    tapOpensFile: false,
  );
  var apiClient = ref.read(apiClientProvider);
  var downloadProgress = ref.read(downloadProgressProvider(id).notifier);

  await _checkNotificationPermission();

  if (await _checkStoragePermission()) {
    var headers = <String, String>{};
    var rememberMe = await AppStorage.rememberMe;

    if (rememberMe != null) {
      headers[HttpHeaders.cookieHeader] = 'remember-me=$rememberMe';
    }

    var task = DownloadTask(
      taskId: id,
      url: await apiClient.createObjectUrl(id),
      headers: headers,
      filename: DownloadTask.suggestedFilename,
      allowPause: true,
      retries: 1,
      updates: .statusAndProgress,
    );

    var result = await fileDownloader.download(
      task,
      onProgress: (progress) => downloadProgress.progress = progress,
      onStatus: (status) {
        if ([
          TaskStatus.complete,
          TaskStatus.canceled,
          TaskStatus.failed,
        ].contains(status)) {
          ref.invalidate(downloadProgressProvider(id), asReload: true);
        }
      },
    );

    if (result.status == .complete) {
      await fileDownloader.moveToSharedStorage(
        result.task as DownloadTask,
        SharedStorage.downloads,
      );
    }
  }
}

Future<bool> _checkNotificationPermission() async {
  var status = await Permission.notification.status;

  if (status.isDenied) {
    status = await Permission.notification.request();
  }

  return status.isGranted;
}

Future<bool> _checkStoragePermission() async {
  var status = await Permission.manageExternalStorage.status;

  if (status.isDenied) {
    status = await Permission.manageExternalStorage.request();
  }

  return status.isGranted;
}
