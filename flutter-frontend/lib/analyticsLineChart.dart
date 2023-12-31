import 'package:flutter/material.dart';
import 'package:syncfusion_flutter_charts/charts.dart';
import 'package:syncfusion_flutter_charts/sparkcharts.dart';
import 'constants.dart' as constants;

class _ChartApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(primarySwatch: Colors.blue),
      home: AnslyiticsChartWidget(),
    );
  }
}

class AnslyiticsChartWidget extends StatefulWidget {
  // ignore: prefer_const_constructors_in_immutables
  AnslyiticsChartWidget({Key? key}) : super(key: key);

  @override
  _AnslyiticsChartWidgetState createState() => _AnslyiticsChartWidgetState();
}

class _AnslyiticsChartWidgetState extends State<AnslyiticsChartWidget> {
  List<_SalesData> severeUpperData = [
    _SalesData('Jan', 2),
    _SalesData('Feb', 4),
    _SalesData('Mar', 22),
    _SalesData('Apr', 12),
    _SalesData('May', 10)
  ];
  List<_SalesData> severeLowerData = [
    _SalesData('Jan', 2),
    _SalesData('Feb', 4),
    _SalesData('Mar', 22),
    _SalesData('Apr', 12),
    _SalesData('May', 10)
  ];
  List<_SalesData> fatalityUpperData = [
    _SalesData('Jan', 23),
    _SalesData('Feb', 4),
    _SalesData('Mar', 2),
    _SalesData('Apr', 12),
    _SalesData('May', 1)
  ];
  List<_SalesData> fatalityLowerData = [
    _SalesData('Jan', 35),
    _SalesData('Feb', 8),
    _SalesData('Mar', 34),
    _SalesData('Apr', 2),
    _SalesData('May', 40)
  ];
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Oracle AI For Healthcare - Flutter + Spring Boot Version',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Analytics - Line Chart'),
          ),
          body: Column(children: [
            //Initialize the chart widget
            SfCartesianChart(
                primaryXAxis: CategoryAxis(),
                // Chart title
                title: ChartTitle(
                    text: 'Lower and Upper Bounds of Severe and Fatal'),
                // Enable legend
                legend: Legend(isVisible: true),
                // Enable tooltip
                tooltipBehavior: TooltipBehavior(enable: true),
                series: <ChartSeries<_SalesData, String>>[
                  LineSeries<_SalesData, String>(
                      dataSource: severeUpperData,
                      xValueMapper: (_SalesData sales, _) => sales.year,
                      yValueMapper: (_SalesData sales, _) => sales.sales,
                      name: 'Server Upper Bound', //Sales
                      // Enable data label
                      dataLabelSettings: DataLabelSettings(isVisible: true)),
                  LineSeries<_SalesData, String>(
                      dataSource: severeLowerData,
                      xValueMapper: (_SalesData sales, _) => sales.year,
                      yValueMapper: (_SalesData sales, _) => sales.sales,
                      name: 'Server Lower Bound', //Sales
                      // Enable data label
                      dataLabelSettings: DataLabelSettings(isVisible: true)),
                  LineSeries<_SalesData, String>(
                      dataSource: fatalityUpperData,
                      xValueMapper: (_SalesData sales, _) => sales.year,
                      yValueMapper: (_SalesData sales, _) => sales.sales,
                      name: 'Fatality Upper Bound', //Sales
                      // Enable data label
                      dataLabelSettings: DataLabelSettings(isVisible: true)),
                  LineSeries<_SalesData, String>(
                      dataSource: fatalityLowerData,
                      xValueMapper: (_SalesData sales, _) => sales.year,
                      yValueMapper: (_SalesData sales, _) => sales.sales,
                      name: 'Fatality Lower Bound', //Sales
                      // Enable data label
                      dataLabelSettings: DataLabelSettings(isVisible: true))
                ]),
            // Expanded(
            //   child: Padding(
            //     padding: const EdgeInsets.all(8.0),
            //     //Initialize the spark charts widget
            //     child: SfSparkLineChart.custom(
            //       //Enable the trackball
            //       trackball: SparkChartTrackball(
            //           activationMode: SparkChartActivationMode.tap),
            //       //Enable marker
            //       marker: SparkChartMarker(
            //           displayMode: SparkChartMarkerDisplayMode.all),
            //       //Enable data label
            //       labelDisplayMode: SparkChartLabelDisplayMode.all,
            //       xValueMapper: (int index) => data[index].year,
            //       yValueMapper: (int index) => data[index].sales,
            //       dataCount: 5,
            //     ),
            //   ),
            // )
          ])),
    );
  }
}

class _SalesData {
  _SalesData(this.year, this.sales);

  final String year;
  final double sales;
}
