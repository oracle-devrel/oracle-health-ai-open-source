import 'package:flutter/material.dart';
import 'analyticscancerjson.dart' as analyticscancerjson;
import 'causeofdeathjson.dart' as causeofdeathjson;
import 'constants.dart' as constants;

import 'package:fl_chart/fl_chart.dart';

class AnalyticsCancerResearchWidget extends StatefulWidget {
  const AnalyticsCancerResearchWidget({super.key});

  @override
  State<AnalyticsCancerResearchWidget> createState() =>
      _AnalyticsCancerResearchWidgetState();
}

class _AnalyticsCancerResearchWidgetState extends State<AnalyticsCancerResearchWidget> {
  late List<analyticscancerjson.CancerOpenResearch> cancerOpenResearches;

  @override
  void initState() {
    super.initState();
    analyticscancerjson.fetchCancerOpenResearch().then((data) {
      setState(() {
        cancerOpenResearches = data;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Oracle AI For Healthcare - Flutter + Spring Boot Version',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
            title: const Text('Analytics - Bar Chart'),
        ),
        body: Center(
          child: Column(children: [
            buildLegend(), 
            Expanded( //todo bug, unnecessary/unrelated call...
              child: FutureBuilder<List<causeofdeathjson.AnnualDeathCause>>(
                future: causeofdeathjson.fetchAnnualDeathCauses(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return BarChart(
                      BarChartData(
                        titlesData: FlTitlesData(
                          show: true,
                          bottomTitles: AxisTitles(
                            sideTitles: bottomTitles,
                          ),
                        ),
                        barGroups: cancerOpenResearches
                            .map((research) => BarChartGroupData(
                                  x: research.monthNumber,
                                  //       x: int.parse(research.monthNumber), // assuming month is a number
                                  barRods: [
                                    BarChartRodData(
                                        toY: research.sumSevereLowerBound
                                            .toDouble(),
                                        width: 15,
                                        color: Colors.amber),
                                    BarChartRodData(
                                        toY: research.sumSevereUpperBound
                                            .toDouble(),
                                        width: 15,
                                        color: Colors.orange),
                                    BarChartRodData(
                                        toY: research.sumFatalityLowerBound
                                            .toDouble(),
                                        width: 15,
                                        color: Color.fromARGB(255, 98, 184, 241)),
                                    BarChartRodData(
                                        toY: research.sumFatalityUpperBound
                                            .toDouble(),
                                        width: 15,
                                        color:
                                            Colors.blue),
                                  ],
                                ))
                            .toList(),
                      ),
                    );
                  } else if (snapshot.hasError) {
                    return Text('${snapshot.error}');
                  }
                  // By default, show a loading spinner.
                  return const CircularProgressIndicator();
                },
              ),
            ),
          ]),
        ),
      ),
    );
  }

  Widget buildLegend() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        SizedBox(width: 16),
        LegendItem('Severe Lower Bound  ', Colors.amber),
        LegendItem('Severe Upper Bound'  , Colors.orange),
        LegendItem('Fatality Lower Bound  ', Color.fromARGB(255, 98, 184, 241)),
        LegendItem('Fatality Upper Bound  ',Colors.blue),
      ],
    );
  }

  SideTitles get bottomTitles => SideTitles(
        showTitles: true,
        reservedSize: 32,
        interval: 1,
        getTitlesWidget: bottomTitleWidgets,
      );

  Widget bottomTitleWidgets(double value, TitleMeta meta) {
    const style = TextStyle(
      fontWeight: FontWeight.bold,
      fontSize: 16,
    );
    Widget text;
    switch (value.toInt()) {
      case 1:
        text = const Text('January', style: style);
        break;
      case 2:
        text = const Text('February', style: style);
        break;
      case 3:
        text = const Text('March', style: style);
        break;
      case 4:
        text = const Text('April', style: style);
        break;
      case 5:
        text = const Text('May', style: style);
        break;
      case 6:
        text = const Text('June', style: style);
        break;
      case 7:
        text = const Text('July', style: style);
        break;
      case 8:
        text = const Text('August', style: style);
        break;
      case 9:
        text = const Text('September', style: style);
        break;
      case 10:
        text = const Text('October', style: style);
        break;
      case 11:
        text = const Text('November', style: style);
        break;
      case 12:
        text = const Text('December', style: style);
        break;
      default:
        text = const Text('');
        break;
    }

    return SideTitleWidget(
      axisSide: meta.axisSide,
      space: 10,
      child: text,
    );
  }
}

class LegendItem extends StatelessWidget {
  final String text;
  final Color color;

  LegendItem(this.text, this.color);

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 20,
          height: 20,
          color: color,
        ),
        SizedBox(width: 8),
        Text(text),
      ],
    );
  }
}
