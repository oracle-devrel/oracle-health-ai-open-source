import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:flutter_map/flutter_map.dart';

import 'hospitalsgeojson.dart' as geocachejson;
import 'package:http/http.dart' as http;
import 'constants.dart' as constants;

class MapWidget extends StatefulWidget {
  @override
  State<MapWidget> createState() => _MapState();
}

class _MapState extends State<MapWidget> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor:  Colors.blue,
        title: Text('Hospitals'),
      ),
      body: Center(
        child: Container(
          child: Column(
            children: [
              Flexible(
                  child: FlutterMap(
                options: MapOptions(
                  center: LatLng( 29.9575, -90.0618),
                  zoom: 4.2,
                ),
                children: [
                  TileLayer(
                    urlTemplate:
                        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                    userAgentPackageName: 'com.example.app',
                  ),
                  FutureBuilder<geocachejson.FeatureCollection>(
                    future: geocachejson.fetchFeatureCollection(),
                    builder: (context, snapshot) {
                      if (snapshot.connectionState == ConnectionState.waiting) {
                        return Center(
                            child:
                                CircularProgressIndicator()); // Show a loading spinner while waiting
                      } else if (snapshot.hasError) {
                        return Text(
                            'Error: ${snapshot.error}'); // Show an error message if something went wrong
                      } else {
                        // Create the MarkerLayer only when the data is available
                        geocachejson.FeatureCollection featureCollection = snapshot.data!;
                        return MarkerLayer(
                          markers: featureCollection.features.map((feature) {
                            return Marker(
                              point: LatLng(feature.geometry.coordinates[1],
                                  feature.geometry.coordinates[0]),
                              width: 30,
                              height: 30,
                              builder: (ctx) => Tooltip(
        message: feature.properties.name, 
                                child: IconButton(
                                  icon: Icon(Icons.location_on),
                                  color: Colors.blue,
                                  hoverColor: Colors.orange.withOpacity(0.3),
                                  onPressed: () async {
                                    var url = Uri.http(constants.Constants.backendEndpointAddress,
                                        '/data/addGeoCacheJournalEntry', {
                                      'creatorname': feature.properties.name,
                                      //currently just using "somevisitor" but logged in username could be used for track/follow backs, etc.
                                      'visitorname': 'somevisitor',
                                      'imageurl': feature.properties.image,
                                      'longitude': feature
                                          .geometry.coordinates[1]
                                          .toString(),
                                      'latitude': feature
                                          .geometry.coordinates[0]
                                          .toString(),
                                    });
                                    var response = await http.post(url);
                                    String message;
                                    if (response.statusCode == 200) {
                                      message = 'HTTP request was successful!';
                                    } else {
                                      message =
                                          'HTTP request failed with status: ${response.statusCode}';
                                    }
                                    showDialog(
                                      context: context,
                                      builder: (context) {
                                        return AlertDialog(
                                          title: Text(
                                              'Hospital Name: ' +
                                                  feature.properties.name), 
                                          content: Image.network(
                                              feature.properties.image),
                                        );
                                      },
                                    );
                                  },
                                ),
                              ),
                            );
                          }).toList(),
                        );
                      }
                    },
                  ),




                ],




              )),
            ],
          ),
        ),
        
      ),
    );
  }

}






