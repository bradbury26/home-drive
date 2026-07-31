// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'list_objects_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ListObjectsResponse _$ListObjectsResponseFromJson(Map<String, dynamic> json) =>
    ListObjectsResponse(
      objects: (json['objects'] as List<dynamic>)
          .map((e) => ObjectModel.fromJson(e as Map<String, dynamic>))
          .toList(),
      continuationToken: json['continuationToken'] as String?,
    );

Map<String, dynamic> _$ListObjectsResponseToJson(
  ListObjectsResponse instance,
) => <String, dynamic>{
  'objects': instance.objects.map((e) => e.toJson()).toList(),
  'continuationToken': instance.continuationToken,
};
