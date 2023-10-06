import 'package:flutter/material.dart';
import 'hospitalsgeojson.dart' as geocachejson;
import 'package:http/http.dart' as http;
import 'dart:convert';



class OCIAIFormWidget extends StatefulWidget {
  @override
  _OCIAIFormWidgetState createState() => _OCIAIFormWidgetState();
}

class _OCIAIFormWidgetState extends State<OCIAIFormWidget> {
  final _formKey = GlobalKey<FormState>();
  String _name = '';
  String _image = '';
  double _latitude = 0;
  double _longitude = 0;

  void _submitForm() async {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save();

      geocachejson.FeatureCollection featureCollection = geocachejson.FeatureCollection(
        type: 'FeatureCollection',
        features: [
          geocachejson.Feature(
            type: 'Feature',
            geometry: geocachejson.Geometry(
              type: 'Point',
              coordinates: [_longitude, _latitude],
            ),
            properties: geocachejson.Properties(
              name: _name,
              image: _image,
            ),
          ),
        ],
      );

      var url = Uri.parse('http://129.80.34.130:8080/health/askai');
      var response = await http.post(
        url,
        headers: {"Content-Type": "application/json"},
        body: jsonEncode(featureCollection.toJson()),
      );

      if (response.statusCode == 200) {
        print('Successful POST request');
      } else {
        print('Failed POST request');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor:  Colors.blue,
        title: Text('Medical Documents - todo show document image and fields and create expense report option'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: <Widget>[
           
              TextFormField(
                initialValue:
                    'https://somefile.png',
                decoration:
                    InputDecoration(labelText: 'Enter image URL'),
                onSaved: (value) {
                  _image = value!;
                },
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter some text';
                  }
                  return null;
                },
              ),
              ElevatedButton(
                onPressed: _submitForm,
                child: Text(
                    'Submit'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}















