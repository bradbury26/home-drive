import 'package:json_annotation/json_annotation.dart';
import 'package:ui/model/files/object_model.dart';

part 'list_objects_response.g.dart';

@JsonSerializable(explicitToJson: true)
class ListObjectsResponse {
  final List<ObjectModel> objects;
  final String? continuationToken;

  ListObjectsResponse({required this.objects, this.continuationToken});

  factory ListObjectsResponse.fromJson(Map<String, dynamic> json) =>
      _$ListObjectsResponseFromJson(json);

  Map<String, dynamic> toJson() => _$ListObjectsResponseToJson(this);
}
