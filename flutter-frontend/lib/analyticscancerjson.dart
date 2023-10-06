import 'package:http/http.dart' as http;
import 'dart:convert';
import 'constants.dart' as constants;

class CancerOpenResearch {
  final int monthNumber;
  final String month;
  final int sumSevereLowerBound;
  final int sumSevereUpperBound;
  final int sumFatalityLowerBound;
  final int sumFatalityUpperBound;

  CancerOpenResearch({
    required this.monthNumber,
    required this.month,
    required this.sumSevereLowerBound,
    required this.sumSevereUpperBound,
    required this.sumFatalityLowerBound,
    required this.sumFatalityUpperBound,
  });

  factory CancerOpenResearch.fromJson(Map<String, dynamic> json) {
    return CancerOpenResearch(
      monthNumber: json['monthNumber'],
      month: json['month'],
      sumSevereLowerBound: json['sumSevereLowerBound'],
      sumSevereUpperBound: json['sumSevereUpperBound'],
      sumFatalityLowerBound: json['sumFatalityLowerBound'],
      sumFatalityUpperBound: json['sumFatalityUpperBound'],
    );
  }
}


Future<List<CancerOpenResearch>> fetchCancerOpenResearch() async {
  final response = await http.get(
      Uri.parse(constants.Constants.backendEndpointAddress + '/data/getCancerOpenResearch'));

  if (response.statusCode == 200) {
    List<dynamic> jsonData = json.decode(response.body);
    List<CancerOpenResearch> cancerOpenResearches = jsonData
        .map((dynamic item) => CancerOpenResearch.fromJson(item))
        .toList();

    for (var research in cancerOpenResearches) {
      print('month: ${research.month}');
      // ... print other fields
    }

    return cancerOpenResearches;
  } else {
    throw Exception('Failed to load data');
  }
}





