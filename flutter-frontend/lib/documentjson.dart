import 'package:http/http.dart' as http;
import 'dart:convert';
import 'constants.dart' as constants;

/** 
class DocumentAnalysisResult {
  final String fileName;
  final List<DocumentField> documentFields;
  final List<DocumentType> detectedDocumentTypes;

  DocumentAnalysisResult({
    required this.fileName,
    required this.documentFields,
    required this.detectedDocumentTypes,
  });

  factory DocumentAnalysisResult.fromJson(Map<String, dynamic> json) {
    var fieldsList = json['documentFields'] as List;
    var typesList = json['detectedDocumentTypes'] as List;
    List<DocumentField> documentFields = fieldsList.map((i) => DocumentField.fromJson(i)).toList();
    List<DocumentType> detectedDocumentTypes = typesList.map((i) => DocumentType.fromJson(i)).toList();

    return DocumentAnalysisResult(
      fileName: json['fileName'],
      documentFields: documentFields,
      detectedDocumentTypes: detectedDocumentTypes,
    );
  }
}

class DocumentField {
  final String name;
  final String text;

  DocumentField({required this.name, required this.text});

  factory DocumentField.fromJson(Map<String, dynamic> json) {
    return DocumentField(
      name: json['name'],
      text: json['text'],
    );
  }
}

class DocumentType {
  final String documentType;

  DocumentType({required this.documentType});

  factory DocumentType.fromJson(Map<String, dynamic> json) {
    return DocumentType(
      documentType: json['documentType'],
    );
  }
}
*/

class DocumentAnalysisResult {
  List<DocumentField>? documentFields;
  String? fileName;
  List<DocumentType>? detectedDocumentTypes;

  DocumentAnalysisResult({this.documentFields, this.fileName, this.detectedDocumentTypes});

  factory DocumentAnalysisResult.fromJson(Map<String, dynamic> json) {
    return DocumentAnalysisResult(
      documentFields: json['documentFields'] != null ? 
          (json['documentFields'] as List)
            .map((e) => DocumentField.fromJson(e))
            .toList() : null,
      fileName: json['fileName'],
      detectedDocumentTypes: json['detectedDocumentTypes'] != null ? 
          (json['detectedDocumentTypes'] as List)
            .map((e) => DocumentType.fromJson(e))
            .toList() : null,
    );
  }
}

class DocumentField {
  String name;
  String text;

  DocumentField({required this.name, required this.text});

  factory DocumentField.fromJson(Map<String, dynamic> json) {
    return DocumentField(
      name: json['name'],
      text: json['text'],
    );
  }
}

class DocumentType {
  String documentType;

  DocumentType({required this.documentType});

  factory DocumentType.fromJson(Map<String, dynamic> json) {
    return DocumentType(
      documentType: json['documentType'],
    );
  }
}


Future<List<DocumentAnalysisResult>> fetchDocumentAnalysisResults() async {
  final response = await http.get(
      Uri.parse(constants.Constants.backendEndpointAddress + '/data/getDocuments'));

  if (response.statusCode == 200) {
    List<dynamic> jsonData = json.decode(response.body);
    return jsonData.map((item) => DocumentAnalysisResult.fromJson(item)).toList();
  } else {
    throw Exception('Failed to load data');
  }
}
