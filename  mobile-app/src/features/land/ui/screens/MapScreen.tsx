```typescript
import React, { useState, useEffect, useRef } from 'react';
import { View, StyleSheet, Button, Alert, Text, Platform, PermissionsAndroid } from 'react-native';
import MapView, { Marker, Polygon, LatLng, Polyline } from 'react-native-maps';
import Geolocation from 'react-native-geolocation-service';
import { RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { database } from '../../../../core/data/local/db';
import GPSData from '../../../../core/data/local/models/GPSData';
import LandRecord from '../../../../core/data/local/models/LandRecord';
import SyncQueueItem from '../../../../core/data/local/models/SyncQueueItem';
import { OfflineIdGenerator } from '../../../../core/sync/OfflineIdGenerator';
import { SYNC_STATUS_PENDING_CREATE, OPERATION_TYPE_CREATE } from '../../../../core/constants/syncConstants';

// Define a placeholder for your RootStackParamList
type RootStackParamList = {
  MapScreen: { landRecordId: string };
  // ... other screens
};

type MapScreenRouteProp = RouteProp<RootStackParamList, 'MapScreen'>;
type MapScreenNavigationProp = StackNavigationProp<RootStackParamList, 'MapScreen'>;

interface Props {
  route: MapScreenRouteProp;
  navigation: MapScreenNavigationProp;
}

const MapScreen: React.FC<Props> = ({ route, navigation }) => {
  const { landRecordId } = route.params;
  const [currentPosition, setCurrentPosition] = useState<LatLng | null>(null);
  const [region, setRegion] = useState({
    latitude: 37.78825,
    longitude: -122.4324,
    latitudeDelta: 0.0922,
    longitudeDelta: 0.0421,
  });
  const [polygonCoordinates, setPolygonCoordinates] = useState<LatLng[]>([]);
  const [isDrawing, setIsDrawing] = useState(false);
  const mapRef = useRef<MapView>(null);

  const requestLocationPermission = async () => {
    if (Platform.OS === 'ios') {
      Geolocation.requestAuthorization('whenInUse'); // or 'always'
      return true;
    }
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            title: 'Location Access Permission',
            message: 'This app needs access to your location to capture GPS data.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          },
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return false;
  };

  useEffect(() => {
    const init = async () => {
      const hasPermission = await requestLocationPermission();
      if (hasPermission) {
        Geolocation.getCurrentPosition(
          position => {
            const { latitude, longitude } = position.coords;
            setCurrentPosition({ latitude, longitude });
            setRegion({
              latitude,
              longitude,
              latitudeDelta: 0.01,
              longitudeDelta: 0.01,
            });
            if (mapRef.current) {
              mapRef.current.animateToRegion({ latitude, longitude, latitudeDelta: 0.01, longitudeDelta: 0.01 });
            }
          },
          error => Alert.alert('Error', error.message),
          { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 },
        );
      } else {
        Alert.alert('Permission Denied', 'Location permission is required to use the map.');
      }
    };
    init();
  }, []);
  
  // REQ-1.3-003: Logic for capturing polygons/polylines
  const handleMapPress = (event: { nativeEvent: { coordinate: LatLng } }) => {
    if (isDrawing) {
      setPolygonCoordinates([...polygonCoordinates, event.nativeEvent.coordinate]);
    }
  };

  const toggleDrawing = () => {
    if (isDrawing && polygonCoordinates.length > 0) {
      // Optionally close the polygon if it's not closed
      // For simplicity, we assume user finishes drawing by pressing again
    }
    setIsDrawing(!isDrawing);
    if (!isDrawing) {
      // Start new drawing, clear old points if any
      setPolygonCoordinates([]);
    }
  };

  const handleClearPolygon = () => {
    setPolygonCoordinates([]);
  };

  // REQ-14-001, REQ-1.3-001: Save GPS Data
  const handleSaveGpsData = async () => {
    if (polygonCoordinates.length < 3) { // A polygon needs at least 3 points
      Alert.alert('Incomplete Data', 'Please draw a polygon with at least 3 points.');
      return;
    }

    try {
      const landRecordExists = await database.get<LandRecord>('land_records').find(landRecordId);
      if (!landRecordExists) {
        Alert.alert('Error', 'Associated Land Record not found.');
        return;
      }

      await database.write(async writer => {
        const localId = OfflineIdGenerator.generate();
        const newGpsData = await writer.collections.get<GPSData>('gps_data').create(gps => {
          gps._raw.id = localId;
          // @ts-ignore // landRecord relation
          gps.landRecord.id = landRecordId;
          gps.type = 'polygon'; // REQ-1.3-003
          gps.data = JSON.stringify(polygonCoordinates); // Storing coordinates as JSON string
          gps.timestamp = Date.now();
          // Geolocation.getCurrentPosition provides these:
          // gps.latitude, gps.longitude, gps.altitude, gps.accuracy can be set if capturing a single point related to the polygon (e.g. centroid or start point)
          // For a polygon, these might be less relevant for the GPSData record itself, or represent an average/center.
          // For simplicity, we are storing the polygon coordinates in 'data'.
          // If individual point accuracy/altitude is needed for each vertex, the model would be more complex.
          gps.sync_status = SYNC_STATUS_PENDING_CREATE;
          gps.last_modified_locally_at = Date.now();
          gps.created_at = Date.now();
          gps.updated_at = Date.now();
        });

        await writer.collections.get<SyncQueueItem>('sync_queue_items').create(item => {
          item.entity_id = newGpsData.id;
          item.entity_type = 'GPSData';
          item.operation_type = OPERATION_TYPE_CREATE;
          item.payload = JSON.stringify({ landRecordId, type: 'polygon', coordinates: polygonCoordinates });
          item.attempt_count = 0;
          item.created_at = Date.now();
        });
      });

      Alert.alert('Success', 'GPS boundary data saved locally.');
      setPolygonCoordinates([]); // Clear after save
      setIsDrawing(false);
      navigation.goBack();
    } catch (error) {
      console.error('Failed to save GPS data:', error);
      Alert.alert('Error', `Could not save GPS data. ${error}`);
    }
  };


  return (
    <View style={styles.container}>
      <MapView
        ref={mapRef}
        style={styles.map}
        region={region}
        onRegionChangeComplete={setRegion}
        onPress={handleMapPress}
        showsUserLocation={true}
        userInterfaceStyle="dark" // Or "light"
      >
        {currentPosition && <Marker coordinate={currentPosition} title="Current Location" />}
        {polygonCoordinates.length > 0 && (
          <Polygon
            coordinates={polygonCoordinates}
            strokeColor="#FF0000" // Red
            fillColor="rgba(255,0,0,0.3)" // Semi-transparent red
            strokeWidth={2}
          />
        )}
        {/* Display existing GPS data for this landRecordId if needed (query and map) */}
      </MapView>
      <View style={styles.controls}>
        <Button title={isDrawing ? 'Finish Drawing' : 'Start Drawing Polygon'} onPress={toggleDrawing} />
        {isDrawing && polygonCoordinates.length > 0 && (
          <Button title="Clear Current Polygon" onPress={handleClearPolygon} color="orange" />
        )}
        {polygonCoordinates.length >= 3 && !isDrawing && (
          <Button title="Save GPS Boundary" onPress={handleSaveGpsData} color="green" />
        )}
         <Text style={styles.instructions}>
          {isDrawing ? `Tap on map to add points. Points: ${polygonCoordinates.length}` : 'Press "Start Drawing" then tap on map.'}
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  controls: {
    position: 'absolute',
    bottom: 20,
    left: 20,
    right: 20,
    backgroundColor: 'rgba(255,255,255,0.8)',
    padding: 10,
    borderRadius: 5,
  },
  instructions: {
    textAlign: 'center',
    marginTop: 5,
    fontSize: 12,
  }
});

export default MapScreen;
```