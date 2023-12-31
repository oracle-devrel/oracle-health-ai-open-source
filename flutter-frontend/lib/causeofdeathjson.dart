import 'package:http/http.dart' as http;
import 'dart:convert';
import 'constants.dart' as constants;

class AnnualDeathCause {
  final String entity;
  final int year;
  final int deathMeningitis;
  final int deathAlzheimersDisease;
  final int deathParkinsonDisease;
  final int deathNutritionalDeficiencies;
  final int deathMalaria;
  final int deathMaternalDisorders;

  AnnualDeathCause({
    required this.entity,
    required this.year,
    required this.deathMeningitis,
    required this.deathAlzheimersDisease,
    required this.deathParkinsonDisease,
    required this.deathNutritionalDeficiencies,
    required this.deathMalaria,
    required this.deathMaternalDisorders,
  });

  factory AnnualDeathCause.fromJson(Map<String, dynamic> json) {
    return AnnualDeathCause(
      entity: json['entity'],
      year: json['year'],
      deathMeningitis: json['deathMeningitis'],
      deathAlzheimersDisease: json['deathAlzheimersDisease'],
      deathParkinsonDisease: json['deathParkinsonDisease'],
      deathNutritionalDeficiencies: json['deathNutritionalDeficiencies'],
      deathMalaria: json['deathMalaria'],
      deathMaternalDisorders: json['deathMaternalDisorders'],
    );
  }
}


Future<List<AnnualDeathCause>> fetchAnnualDeathCauses() async {
  final response = await http.get(
      Uri.parse(constants.Constants.backendEndpointAddress + '/data/getCausesOfDeath'));

  if (response.statusCode == 200) {
    List<dynamic> jsonData = json.decode(response.body);
    List<AnnualDeathCause> annualDeathCauses = jsonData
        .map((dynamic item) => AnnualDeathCause.fromJson(item))
        .toList();

    for (var cause in annualDeathCauses) {
      print('Entity: ${cause.entity}');
      print('Year: ${cause.year}');
      print('Death Meningitis: ${cause.deathMeningitis}');
      // ... print other fields
    }

    return annualDeathCauses;
  } else {
    throw Exception('Failed to load data');
  }
}





