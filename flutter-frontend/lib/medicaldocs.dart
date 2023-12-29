import 'dart:io';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:file_picker/file_picker.dart';
import 'dart:typed_data'; // for Uint8List
import 'piechart.dart' as piechart; 
import 'constants.dart' as constants;

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FormWidget(),
    );
  }
}

class FormWidget extends StatefulWidget {
  @override
  _FormWidgetState createState() => _FormWidgetState();
}

class _FormWidgetState extends State<FormWidget> {
  Uint8List? fileBytes;
  String? fileName; // Added variable to store the filename
  String? serverResponse;
  Map<String, dynamic>? parsedResponse;
  List<dynamic>? labels;

  Future<void> submitForm() async {
    var request = http.MultipartRequest('POST', 
    Uri.parse(constants.Constants.backendEndpointAddress + '/medicaldocuments/analyzedocument'));

    if (fileBytes != null && fileName != null) {
      request.files.add(http.MultipartFile.fromBytes(
        'file', 
        fileBytes!,
        filename: fileName, // Use the actual filename
      ));
    }
    // Rest of the logic remains the same
    var response = await request.send();
    final respStr = await response.stream.bytesToString();  
    setState(() {
      serverResponse = respStr;
    });
    if (response.statusCode == 200) {
      print("Successfully uploaded");
    } else {
      print("Failed to upload");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor:  Colors.blue, title: Text('Medical Documents')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              onPressed: () async {
                FilePickerResult? result = await FilePicker.platform.pickFiles(withData: true);

                if (result != null) {
                  setState(() {
                    fileBytes = result.files.single.bytes!;
                    fileName = result.files.single.name; // Capture the filename
                  });
                }
              },
              child: Text('Pick a file'),
            ),
            if (fileBytes != null)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0),
                child: Image.memory(fileBytes!, height: 300,), 
              ),
            ElevatedButton(
              onPressed: submitForm,
              child: Text('Submit'),
            ),
            if (serverResponse != null)
              Row(
                children: [
                  Expanded(
                    child: Text(serverResponse!),
                  )
                ],
              ),
          ],
        ),
      ),
    );
  }
}
