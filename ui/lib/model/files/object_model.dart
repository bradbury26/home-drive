import 'package:json_annotation/json_annotation.dart';

part 'object_model.g.dart';

@JsonSerializable()
class ObjectModel {
  final String id;
  final String name;
  final String objectType;
  final String? mediaType;
  final int contentLength;
  final DateTime lastModified;

  ObjectModel({
    required this.id,
    required this.name,
    required this.objectType,
    this.mediaType,
    required this.contentLength,
    required this.lastModified,
  });

  factory ObjectModel.fromJson(Map<String, dynamic> json) =>
      _$ObjectModelFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectModelToJson(this);
}
