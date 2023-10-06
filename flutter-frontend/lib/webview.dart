import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

import 'dart:async';
import 'dart:typed_data';
import 'package:webview_flutter_platform_interface/webview_flutter_platform_interface.dart';

//todo look at https://pub.dev/packages/flutter_inappwebview 
//instead of https://pub.dev/packages/webview_flutter_web 

class BackEndWidget extends StatefulWidget {
  //The unique key is require else Flutter will manage/cache the WebView state internally
  final Key? key;
  final String url;
  final String title;

  const BackEndWidget({this.key, required this.url, required this.title}) : super(key: key);

  @override
  BackEndWidgetState createState() => BackEndWidgetState();
}


class BackEndWidgetState extends State<BackEndWidget> {
  late PlatformWebViewController _controller;
  late String _url;
  late String _title;

  @override
  void initState() {
    super.initState();
    _url = widget.url;
    _title = widget.title;
    _controller = PlatformWebViewController(
      const PlatformWebViewControllerCreationParams(),
    );
    _loadUrl(_url);
  }

  Future<void> _loadUrl(String url) async {
    await _controller.loadRequest(
      LoadRequestParams(
        uri: Uri.parse(url),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor:  Colors.blue,
        title: Text(_title),
        actions: <Widget>[
          _SampleMenu(_controller),
        ],
      ),
      body: PlatformWebViewWidget(
        PlatformWebViewWidgetCreationParams(controller: _controller),
      ).build(context),
    );
  }


}



enum _MenuOptions {
  doPostRequest,
}

class _SampleMenu extends StatelessWidget {
  const _SampleMenu(this.controller);

  final PlatformWebViewController controller;

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<_MenuOptions>(
      onSelected: (_MenuOptions value) {
        switch (value) {
          case _MenuOptions.doPostRequest:
            _onDoPostRequest(controller);
            break;
        }
      },
      itemBuilder: (BuildContext context) => <PopupMenuItem<_MenuOptions>>[
        const PopupMenuItem<_MenuOptions>(
          value: _MenuOptions.doPostRequest,
          child: Text('Post Request'),
        ),
      ],
    );
  }

  Future<void> _onDoPostRequest(PlatformWebViewController controller) async {
    final LoadRequestParams params = LoadRequestParams(
      uri: Uri.parse('https://httpbin.org/post'),
      method: LoadRequestMethod.post,
      headers: const <String, String>{
        'foo': 'bar',
        'Content-Type': 'text/plain'
      },
      body: Uint8List.fromList('Test Body'.codeUnits),
    );
    await controller.loadRequest(params);
  }
}









