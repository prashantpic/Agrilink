import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polygon, Polyline, GeoJSON } from 'react-leaflet';
import { LatLngExpression } from 'leaflet';
import { Box, Paper, Typography } from '@mui/material';
import { MapData, MapLayer } from '../types/dashboardTypes';
// Import Leaflet CSS - typically done globally in App.tsx or main.tsx
// import 'leaflet/dist/leaflet.css';
// Fix for default marker icon issue with Webpack/Vite
import L from 'leaflet';
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});


interface MapWidgetProps {
  mapData: MapData;
  title?: string;
  height?: string | number;
}

const renderLayer = (layer: MapLayer) => {
  switch (layer.type) {
    case 'marker':
      return (
        <Marker key={layer.id} position={layer.data as LatLngExpression} {...layer.options}>
          {layer.popup && <Popup>{layer.popup}</Popup>}
        </Marker>
      );
    case 'polygon':
      return (
        <Polygon key={layer.id} positions={layer.data as LatLngExpression[][]} {...layer.options}>
          {layer.popup && <Popup>{layer.popup}</Popup>}
        </Polygon>
      );
    case 'polyline':
      return (
        <Polyline key={layer.id} positions={layer.data as LatLngExpression[]} {...layer.options}>
          {layer.popup && <Popup>{layer.popup}</Popup>}
        </Polyline>
      );
    case 'geojson':
      return <GeoJSON key={layer.id} data={layer.data as any} {...layer.options} />;
    default:
      return null;
  }
};

const MapWidget: React.FC<MapWidgetProps> = ({ mapData, title, height = 400 }) => {
  const {
    center,
    zoom,
    layers,
    tileLayerUrl = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    tileLayerAttribution = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  } = mapData;

  return (
    <Paper sx={{ p: title ? 2 : 0, height: title ? `calc(${height}px + 48px)`: `${height}px`, display: 'flex', flexDirection: 'column' }}>
      {/* Important: Leaflet CSS must be imported globally for map to render correctly */}
      {title && (
        <Typography variant="h6" gutterBottom component="div">
          {title}
        </Typography>
      )}
       <Box sx={{ height: `${height}px`, width: '100%', borderRadius: 1, overflow: 'hidden' }}>
        <MapContainer center={center} zoom={zoom} style={{ height: '100%', width: '100%' }}>
          <TileLayer
            attribution={tileLayerAttribution}
            url={tileLayerUrl}
          />
          {layers?.map(renderLayer)}
        </MapContainer>
      </Box>
    </Paper>
  );
};

export default MapWidget;