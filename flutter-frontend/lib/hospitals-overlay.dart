import 'package:flutter/material.dart';


class MapWidget extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: BackgroundWidget(),
    );
  }
}

class BackgroundWidget extends StatefulWidget {
  @override
  _BackgroundWidgetState createState() => _BackgroundWidgetState();
}

class _BackgroundWidgetState extends State<BackgroundWidget> {
  String inputValue = '';
  OverlayEntry? overlayEntry; // Store the overlay entry reference

  void updateInputValue(String value) {
    setState(() {
      inputValue = value;
    });
  }

  void openFormOverlay() {
    overlayEntry = OverlayEntry(
      builder: (context) => FormOverlay(updateInputValue, closeFormOverlay),
    );

    Overlay.of(context)!.insert(overlayEntry!);
  }

  void closeFormOverlay() {
    if (overlayEntry != null) {
      overlayEntry!.remove();
      overlayEntry = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Overlay Example'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Background Value: $inputValue'),
            ElevatedButton(
              onPressed: openFormOverlay,
              child: Text('Open Form'),
            ),
          ],
        ),
      ),
    );
  }
}

class FormOverlay extends StatefulWidget {
  final Function(String) onSubmit;
  final Function() onClose;

  FormOverlay(this.onSubmit, this.onClose);

  @override
  _FormOverlayState createState() => _FormOverlayState();
}

class _FormOverlayState extends State<FormOverlay> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: widget.onClose, // Close the overlay when tapping outside the form
      child: Card(
        elevation: 5,
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: _controller,
                  decoration: InputDecoration(labelText: 'Enter a value'),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter a value';
                    }
                    return null;
                  },
                ),
                SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () {
                    if (_formKey.currentState!.validate()) {
                      widget.onSubmit(_controller.text);
                      widget.onClose(); // Close the overlay
                    }
                  },
                  child: Text('Submit'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
