import 'package:flutter/material.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:ui/model/files/object_model.dart';
import 'package:ui/model/sort_model.dart';
import 'package:ui/services/api_client.dart';

part 'file_view_model.g.dart';

@riverpod
class FileListSortNotifier extends _$FileListSortNotifier {
  static final sortDefinitions = {
    'objectName': SortDefinition(
      expression: 'objectName',
      name: "Name",
      ascendingText: "A to Z",
      descendingText: "Z to A",
    ),
    'lastUpdated': SortDefinition(
      expression: 'lastUpdated',
      name: 'Date modified',
      ascendingText: 'Old to New',
      descendingText: 'New to Old',
      initialAscendingSort: false,
    ),
    'contentLength': SortDefinition(
      expression: 'contentLength',
      name: 'Size',
      ascendingText: 'Small to Large',
      descendingText: 'Large to Small',
      initialAscendingSort: false,
    ),
  };

  @override
  Sort build(String? parentId) {
    var defaultSortDefinition = sortDefinitions['objectName']!;

    return Sort(
      defaultSortDefinition.expression,
      defaultSortDefinition.name,
      defaultSortDefinition.initialAscendingSort,
    );
  }

  void updateSort(SortDefinition sortDefinition) {
    state = Sort(
      sortDefinition.expression,
      sortDefinition.name,
      sortDefinition.initialAscendingSort,
    );
  }

  void sortAscending() {
    state = state.sortAscending();
  }

  void sortDescending() {
    state = state.sortDescending();
  }
}

@riverpod
class FileListNotifier extends _$FileListNotifier {
  static final totalItems = 1000;
  String? _continuationToken;

  @override
  Future<List<ObjectModel>> build(String? parentId) async {
    return _loadObjects(ref, parentId, null);
  }

  bool onScroll(ScrollNotification scrollNotification) {
    if (scrollNotification.metrics.pixels ==
        scrollNotification.metrics.maxScrollExtent) {
      _onScrollEnd();
    }

    return false;
  }

  Future<void> _onScrollEnd() async {
    if (state.isLoading || (state.hasValue && _continuationToken == null)) {
      return;
    }

    state = AsyncValue.loading();

    var currentItems = await future;
    var newItems = await _loadObjects(ref, parentId, _continuationToken);

    state = AsyncValue.data([...currentItems, ...newItems]);
  }

  Future<List<ObjectModel>> _loadObjects(
    Ref ref,
    String? parentId,
    String? continuationToken,
  ) async {
    var apiClient = ref.read(apiClientProvider);
    var sort = ref.watch(fileListSortProvider(parentId));

    var listObjectResponse = await apiClient.listObjects(
      totalItems: totalItems,
      parentId: parentId,
      continuationToken: continuationToken,
      sort: sort.apiString(),
    );

    _continuationToken = listObjectResponse.continuationToken;

    return List.unmodifiable(listObjectResponse.objects);
  }
}

@riverpod
Future<ObjectModel> fileDetails(Ref ref, String id) async {
  var apiClient = ref.read(apiClientProvider);

  return apiClient.getObjectDetails(id);
}

// class FileViewModel extends ChangeNotifier {
//   final ApiClient _apiClient;
//   final String? directoryId;
//   fiSortDefinitionModele<ObjectModel>? directoryDetailsFuture;
//
//   final List<ObjectModel> _files = [];
//   String? _continuationToken;
//   SortModel _selectedSort = sortModels.first;
//
//   SortModel get selectedSort => _selectedSort;
//
//   bool _ascendingSort = true;
//
//   bool get ascendingSort => _ascendingSort;
//
//   List<ObjectModel> get files => List.unmodifiable(_files);
//   bool loading = false;
//
//   FileViewModel({required this._apiClient, this.directoryId})
//     : directoryDetailsFuture = directoryId != null
//           ? _apiClient.getObjectDetails(directoryId)
//           : null;
//
//   Future<void> init() async {
//     loading = true;
//     notifyListeners();
//
//     await _loadFiles();
//
//     loading = false;
//     notifyListeners();
//   }
//
//   bool onScroll(ScrollNotification scrollNotification) {
//     if (scrollNotification.metrics.pixels ==
//         scrollNotification.metrics.maxScrollExtent) {
//       _onScrollEnd();
//     }
//
//     return false;
//   }
//
//   Future<void> _onScrollEnd() async {
//     await _loadFiles();
//     notifyListeners();
//   }
//
//   Future<void> refresh() async {
//     _continuationToken = null;
//     _files.clear();
//
//     await _loadFiles();
//
//     notifyListeners();
//   }
//
//   Future<void> updateSort(SortModel sortModel) async {
//     _selectedSort = sortModel;
//     _ascendingSort = sortModel.initialAscendingSort;
//
//     await refresh();
//   }
//
//   Future<void> sortAscending() async {
//     if (!_ascendingSort) {
//       _ascendingSort = true;
//
//       await refresh();
//     }
//   }
//
//   Future<void> sortDescending() async {
//     if (_ascendingSort) {
//       _ascendingSort = false;
//
//       await refresh();
//     }
//   }
//
//   Future<void> _loadFiles() async {
//     if (_files.isNotEmpty && _continuationToken == null) {
//       return;
//     }
//
//     var listObjectResponse = await _apiClient.listObjects(
//       totalItems: totalItems,
//       parentId: directoryId,
//       continuationToken: _continuationToken,
//       sort: _fileSort,
//     );
//
//     _files.addAll(listObjectResponse.objects);
//
//     _continuationToken = listObjectResponse.continuationToken;
//   }
//
//   String get _fileSort {
//     return '${selectedSort.expression},${ascendingSort ? 'asc' : 'desc'}';
//   }
// }
