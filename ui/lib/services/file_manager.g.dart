// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'file_manager.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(DownloadProgress)
final downloadProgressProvider = DownloadProgressFamily._();

final class DownloadProgressProvider
    extends $NotifierProvider<DownloadProgress, double> {
  DownloadProgressProvider._({
    required DownloadProgressFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'downloadProgressProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$downloadProgressHash();

  @override
  String toString() {
    return r'downloadProgressProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  DownloadProgress create() => DownloadProgress();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(double value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<double>(value),
    );
  }

  @override
  bool operator ==(Object other) {
    return other is DownloadProgressProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$downloadProgressHash() => r'20fb653bb3b04fdd4d77f5a5f007a20f06921985';

final class DownloadProgressFamily extends $Family
    with
        $ClassFamilyOverride<DownloadProgress, double, double, double, String> {
  DownloadProgressFamily._()
    : super(
        retry: null,
        name: r'downloadProgressProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  DownloadProgressProvider call(String id) =>
      DownloadProgressProvider._(argument: id, from: this);

  @override
  String toString() => r'downloadProgressProvider';
}

abstract class _$DownloadProgress extends $Notifier<double> {
  late final _$args = ref.$arg as String;
  String get id => _$args;

  double build(String id);
  @$mustCallSuper
  @override
  WhenComplete runBuild() {
    final ref = this.ref as $Ref<double, double>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<double, double>,
              double,
              Object?,
              Object?
            >;
    return element.handleCreate(ref, () => build(_$args));
  }
}

@ProviderFor(downloadFile)
final downloadFileProvider = DownloadFileFamily._();

final class DownloadFileProvider
    extends $FunctionalProvider<AsyncValue<void>, void, FutureOr<void>>
    with $FutureModifier<void>, $FutureProvider<void> {
  DownloadFileProvider._({
    required DownloadFileFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'downloadFileProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$downloadFileHash();

  @override
  String toString() {
    return r'downloadFileProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<void> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<void> create(Ref ref) {
    final argument = this.argument as String;
    return downloadFile(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is DownloadFileProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$downloadFileHash() => r'33019170c2764fd98044e5ee05232015765359cd';

final class DownloadFileFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<void>, String> {
  DownloadFileFamily._()
    : super(
        retry: null,
        name: r'downloadFileProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  DownloadFileProvider call(String id) =>
      DownloadFileProvider._(argument: id, from: this);

  @override
  String toString() => r'downloadFileProvider';
}
