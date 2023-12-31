import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'constants.dart' as constants;



class FormWidget extends StatefulWidget {
  @override
  _FormWidgetState createState() => _FormWidgetState();
}

class _FormWidgetState extends State<FormWidget> {
  final _formKey = GlobalKey<FormState>();
  String _question = '';
  String _serverResponse = ''; 


  void _submitForm() async {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save();

      var dataToSubmit = {
        "question": _question,
      };

      var url = Uri.parse(constants.Constants.backendEndpointAddress + '/askai/askai');

      try {
        var response = await http.post(
          url,
          headers: {"Content-Type": "application/json"},
          body: jsonEncode(dataToSubmit),
        );

        setState(() {
          _serverResponse = response.statusCode == 200 ? response.body : 'Failed POST request';
        });
      } catch (e) {
        print("Exception thrown: $e");
        setState(() {
          _serverResponse = 'An exception occurred: $e';
        });
      }
    }
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor:  Colors.blue,
        title: Text('Ask Oracle Gen AI'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: <Widget>[
              TextFormField(
                initialValue: 'I have a headache. What should I do?',
                decoration: InputDecoration(labelText: 'Enter question'),
                onSaved: (value) {
                  _question = value!;
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
                child: Text('Submit'),
              ),
              Text('\n\n $_serverResponse'), 
            ],
          ),
        ),
      ),
    );
  }
}
