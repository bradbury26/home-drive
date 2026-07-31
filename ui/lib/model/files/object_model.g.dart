// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectModel _$ObjectModelFromJson(Map<String, dynamic> json) => ObjectModel(
  id: json['id'] as String,
  name: json['name'] as String,
  objectType: json['objectType'] as String,
  mediaType: json['mediaType'] as String?,
  contentLength: (json['contentLength'] as num).toInt(),
  lastModified: DateTime.parse(json['lastModified'] as String),
);

Map<String, dynamic> _$ObjectModelToJson(ObjectModel instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'objectType': instance.objectType,
      'mediaType': instance.mediaType,
      'contentLength': instance.contentLength,
      'lastModified': instance.lastModified.toIso8601String(),
    };
