import 'package:flutter/material.dart';
import 'causeofdeathjson.dart' as causeofdeathjson;


class CauseOfDeathWidget extends StatefulWidget {
  const CauseOfDeathWidget({super.key});

  @override
  State<CauseOfDeathWidget> createState() => _CauseOfDeathWidgetState();
}

class _CauseOfDeathWidgetState extends State<CauseOfDeathWidget> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Oracle AI For Healthcare - Flutter + Spring Boot Version',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Causes of Death'),
        ),
        body: Center(
          child: FutureBuilder<List<causeofdeathjson.AnnualDeathCause>>(
            future: causeofdeathjson.fetchAnnualDeathCauses(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                return DataTable(
                  columns: const <DataColumn>[
                    DataColumn(label: Text('Entity')),
                    DataColumn(label: Text('Year')),
                    DataColumn(label: Text('Meningitis')),
                    DataColumn(label: Text('Alzheimers')),
                    DataColumn(label: Text('Parkinsons')),
                    DataColumn(label: Text('Nutritional Deficiciences')),
                    DataColumn(label: Text('Malaria')),
                    DataColumn(label: Text('Maternal Disorders')),
                    // Add more columns for other fields if needed
                  ],
                  rows: snapshot.data!.map((cause) {
                    return DataRow(
                      cells: <DataCell>[
                        DataCell(Text(cause.entity)),
                        DataCell(Text('${cause.year}')),
                        DataCell(Text('${cause.deathMeningitis}')),
                        DataCell(Text('${cause.deathAlzheimersDisease}')),
                        DataCell(Text('${cause.deathParkinsonDisease}')),
                        DataCell(Text('${cause.deathNutritionalDeficiencies}')),
                        DataCell(Text('${cause.deathMalaria}')),
                        DataCell(Text('${cause.deathMaternalDisorders}')),
                        // Add more cells for other fields if needed
                      ],
                    );
                  }).toList(),
                );
              } else if (snapshot.hasError) {
                return Text('${snapshot.error}');
              }
              // By default, show a loading spinner.
              return const CircularProgressIndicator();
            },
          ),
        ),
      ),
    );
  }
}