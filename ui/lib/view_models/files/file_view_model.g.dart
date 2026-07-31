// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'file_view_model.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(FileListSortNotifier)
final fileListSortProvider = FileListSortNotifierFamily._();

final class FileListSortNotifierProvider
    extends $NotifierProvider<FileListSortNotifier, Sort> {
  FileListSortNotifierProvider._({
    required FileListSortNotifierFamily super.from,
    required String? super.argument,
  }) : super(
         retry: null,
         name: r'fileListSortProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$fileListSortNotifierHash();

  @override
  String toString() {
    return r'fileListSortProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  FileListSortNotifier create() => FileListSortNotifier();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(Sort value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<Sort>(value),
    );
  }

  @override
  bool operator ==(Object other) {
    return other is FileListSortNotifierProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$fileListSortNotifierHash() =>
    r'cc74918b2b5f0efd5b6c6614938eb54dc52ab7b5';

final class FileListSortNotifierFamily extends $Family
    with $ClassFamilyOverride<FileListSortNotifier, Sort, Sort, Sort, String?> {
  FileListSortNotifierFamily._()
    : super(
        retry: null,
        name: r'fileListSortProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  FileListSortNotifierProvider call(String? parentId) =>
      FileListSortNotifierProvider._(argument: parentId, from: this);

  @override
  String toString() => r'fileListSortProvider';
}

abstract class _$FileListSortNotifier extends $Notifier<Sort> {
  late final _$args = ref.$arg as String?;
  String? get parentId => _$args;

  Sort build(String? parentId);
  @$mustCallSuper
  @override
  WhenComplete runBuild() {
    final ref = this.ref as $Ref<Sort, Sort>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<Sort, Sort>,
              Sort,
              Object?,
              Object?
            >;
    return element.handleCreate(ref, () => build(_$args));
  }
}

@ProviderFor(FileListNotifier)
final fileListProvider = FileListNotifierFamily._();

final class FileListNotifierProvider
    extends $AsyncNotifierProvider<FileListNotifier, List<ObjectModel>> {
  FileListNotifierProvider._({
    required FileListNotifierFamily super.from,
    required String? super.argument,
  }) : super(
         retry: null,
         name: r'fileListProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$fileListNotifierHash();

  @override
  String toString() {
    return r'fileListProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  FileListNotifier create() => FileListNotifier();

  @override
  bool operator ==(Object other) {
    return other is FileListNotifierProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$fileListNotifierHash() => r'ea1cc4ac4af20a051f9395e9b8656ed4d91e89e3';

final class FileListNotifierFamily extends $Family
    with
        $ClassFamilyOverride<
          FileListNotifier,
          AsyncValue<List<ObjectModel>>,
          List<ObjectModel>,
          FutureOr<List<ObjectModel>>,
          String?
        > {
  FileListNotifierFamily._()
    : super(
        retry: null,
        name: r'fileListProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  FileListNotifierProvider call(String? parentId) =>
      FileListNotifierProvider._(argument: parentId, from: this);

  @override
  String toString() => r'fileListProvider';
}

abstract class _$FileListNotifier extends $AsyncNotifier<List<ObjectModel>> {
  late final _$args = ref.$arg as String?;
  String? get parentId => _$args;

  FutureOr<List<ObjectModel>> build(String? parentId);
  @$mustCallSuper
  @override
  WhenComplete runBuild() {
    final ref =
        this.ref as $Ref<AsyncValue<List<ObjectModel>>, List<ObjectModel>>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<AsyncValue<List<ObjectModel>>, List<ObjectModel>>,
              AsyncValue<List<ObjectModel>>,
              Object?,
              Object?
            >;
    return element.handleCreate(ref, () => build(_$args));
  }
}

@ProviderFor(fileDetails)
final fileDetailsProvider = FileDetailsFamily._();

final class FileDetailsProvider
    extends
        $FunctionalProvider<
          AsyncValue<ObjectModel>,
          ObjectModel,
          FutureOr<ObjectModel>
        >
    with $FutureModifier<ObjectModel>, $FutureProvider<ObjectModel> {
  FileDetailsProvider._({
    required FileDetailsFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'fileDetailsProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$fileDetailsHash();

  @override
  String toString() {
    return r'fileDetailsProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<ObjectModel> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<ObjectModel> create(Ref ref) {
    final argument = this.argument as String;
    return fileDetails(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is FileDetailsProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$fileDetailsHash() => r'36cc1a3124c6ad792a983d4a514db8224704f517';

final class FileDetailsFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<ObjectModel>, String> {
  FileDetailsFamily._()
    : super(
        retry: null,
        name: r'fileDetailsProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  FileDetailsProvider call(String id) =>
      FileDetailsProvider._(argument: id, from: this);

  @override
  String toString() => r'fileDetailsProvider';
}
