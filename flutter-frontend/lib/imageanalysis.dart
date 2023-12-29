import 'dart:io';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:file_picker/file_picker.dart';
import 'dart:typed_data'; // for Uint8List
import 'piechart.dart' as piechart; 
import 'urllauncher.dart' as urllauncher;
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
  String? dropdownValue;
  Uint8List? fileBytes; 
  String? serverResponse;
  Map<String, dynamic>? parsedResponse ;
  List<dynamic> ?labels;

  Future<void> submitForm() async {
    var request = http.MultipartRequest(
      'POST', Uri.parse(constants.Constants.backendEndpointAddress + '/imageanalysis/analyzeimage'));

    if (fileBytes != null) {
      request.files.add(http.MultipartFile.fromBytes(
        'file', 
        fileBytes!,
        filename: 'imageforanalysis.png', 
      ));
    }

    request.fields['model'] = dropdownValue ?? '';

    var response = await request.send();
    final respStr = await response.stream.bytesToString();  


    setState(() {
      serverResponse = respStr;
   //   parsedResponse = jsonDecode(respStr);
   //   var labels = parsedResponse['labels'] as List?;

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
        backgroundColor:  Colors.blue, title: Text('Image Analysis')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            DropdownButton<String>(
              value: dropdownValue,
              hint: Text('Select Model'),
              onChanged: (String? newValue) {
                setState(() {
                  dropdownValue = newValue!;
                });
              },
              items: [
                DropdownMenuItem<String>(
                  value: 'breastcancer',
                  child: Text('Breast Cancer and Normal Breast Model'),
                ),
                DropdownMenuItem<String>(
                  value: 'covid',
                  child: Text('Covid, Pneumonia, and Normal Chest Model'),
                ),
                DropdownMenuItem<String>(
                  value: 'lungcancer',
                  child: Text('Lung Cancer and Normal Lung Model'),
                ),
              ],
            ),
            ElevatedButton(
              onPressed: () async {
                FilePickerResult? result = await FilePicker.platform.pickFiles(withData: true);

                if (result != null) {
                  setState(() {
                    fileBytes = result.files.single.bytes!;
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
