import 'package:flutter/material.dart';
import 'documentjson.dart' as documentjson;


class DocumentAnalysisWidget extends StatefulWidget {
  const DocumentAnalysisWidget({super.key});

  @override
  State<DocumentAnalysisWidget> createState() => DocumentAnalysisWidgetState();
}

class DocumentAnalysisWidgetState extends State<DocumentAnalysisWidget> {

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<documentjson.DocumentAnalysisResult>>(
      future: documentjson.fetchDocumentAnalysisResults(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return CircularProgressIndicator();
        } else if (snapshot.hasError) {
          return Text("Error: ${snapshot.error}");
        } else {
          List<documentjson.DocumentAnalysisResult> data = snapshot.data!;
          return ListView.builder(
            itemCount: data.length,
            itemBuilder: (context, index) {
     return Column(
  children: [
    if (data[index].fileName != null)
      Text("File Name: ${data[index].fileName}"),
    if (data[index].detectedDocumentTypes != null && data[index].detectedDocumentTypes!.isNotEmpty)
      Text("Document Type: ${data[index].detectedDocumentTypes!.map((e) => e.documentType).join(", ")}"),
    if (data[index].documentFields != null)
      Column(
        children: data[index].documentFields!.map((field) => 
          Row(
            children: [
              Text("${field.name} :"),
              Text("${field.text}"),
            ],
          )
        ).toList(),
      ),
  ],
);

            },
          );
        }
      },
    );
  }
}