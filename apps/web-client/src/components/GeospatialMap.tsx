import React, { useEffect, useRef } from 'react';
import L from 'leaflet';
import type { Shipment, DisruptionAdvisory, Port } from '../types';

interface GeospatialMapProps {
  shipments: Shipment[];
  disruptions: DisruptionAdvisory[];
  onSelectShipmentForSimulation: (shipment: Shipment) => void;
  selectedShipmentId?: string;
  ports?: Port[];
}

const DEFAULT_PORTS = [
  { code: 'NLRTM', name: 'Port of Rotterdam', lat: 51.95, lon: 4.14, country: 'Netherlands' },
  { code: 'BEANR', name: 'Port of Antwerp-Bruges', lat: 51.2721, lon: 4.3164, country: 'Belgium' },
  { code: 'DEHAM', name: 'Port of Hamburg', lat: 53.5300, lon: 9.9200, country: 'Germany' },
  { code: 'BEZEE', name: 'Port of Zeebrugge', lat: 51.3570, lon: 3.2180, country: 'Belgium' }
];

export const GeospatialMap: React.FC<GeospatialMapProps> = ({
  shipments,
  disruptions,
  onSelectShipmentForSimulation,
  selectedShipmentId,
  ports
}) => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const layerGroupRef = useRef<L.LayerGroup | null>(null);

  // Initialize Map safely with React StrictMode protection
  useEffect(() => {
    const container = mapContainerRef.current;
    if (!container) return;

    // Clean up any stale Leaflet DOM state if StrictMode double-mounted
    if ((container as any)._leaflet_id) {
      delete (container as any)._leaflet_id;
    }

    if (mapRef.current) {
      mapRef.current.remove();
      mapRef.current = null;
    }

    const map = L.map(container, {
      center: [51.8, 3.5], // Center of English Channel / North Sea
      zoom: 6,
      zoomControl: false
    });

    L.control.zoom({ position: 'bottomright' }).addTo(map);

    // Dark nautical basemap tiles (open Esri World Dark Gray canvas)
    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
      attribution: '&copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
      maxZoom: 16
    }).addTo(map);

    const layerGroup = L.layerGroup().addTo(map);
    layerGroupRef.current = layerGroup;
    mapRef.current = map;

    return () => {
      if (layerGroupRef.current) {
        layerGroupRef.current.clearLayers();
        layerGroupRef.current = null;
      }
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
      }
      if (container) {
        delete (container as any)._leaflet_id;
      }
    };
  }, []);

  // Update Layers (Ports, Shipments, Polylines) whenever state changes
  useEffect(() => {
    const map = mapRef.current;
    const layerGroup = layerGroupRef.current;
    if (!map || !layerGroup) return;

    // Clear previous dynamic layers cleanly
    layerGroup.clearLayers();

    // 1. Render Ports (from database-backed API or fallback)
    const effectivePorts = (ports && ports.length > 0)
      ? ports.map(p => ({
          code: p.code,
          name: p.name,
          lat: p.latitude,
          lon: p.longitude,
          country: p.country
        }))
      : DEFAULT_PORTS;

    effectivePorts.forEach(port => {
      const isDisrupted = disruptions.some(d => d.affectedPortCode === port.code);
      const portIcon = L.divIcon({
        className: 'custom-port-marker',
        html: `
          <div class="${isDisrupted ? 'pulse-disrupted' : ''} flex items-center justify-center w-6 h-6 rounded-full ${
            isDisrupted ? 'bg-rose-500' : 'bg-emerald-500'
          } border-2 border-slate-900 shadow-md">
            <div class="w-2 h-2 rounded-full bg-white"></div>
          </div>
        `,
        iconSize: [24, 24],
        iconAnchor: [12, 12]
      });

      L.marker([port.lat, port.lon], { icon: portIcon })
        .addTo(layerGroup)
        .bindPopup(`
          <div class="p-2 font-sans text-xs">
            <strong class="text-sm font-bold text-slate-900">${port.name} (${port.code})</strong>
            <div class="mt-1 text-slate-600">${port.country}</div>
            <div class="mt-2 font-semibold ${isDisrupted ? 'text-red-600' : 'text-emerald-600'}">
              ${isDisrupted ? '⚠️ ACTIVE MARITIME DISRUPTION' : '✓ Normal Port Operations'}
            </div>
          </div>
        `);
    });

    // 2. Render Shipments & Trajectories
    shipments.forEach(shipment => {
      if (shipment.currentLatitude == null || shipment.currentLongitude == null) return;

      const isSelected = shipment.id === selectedShipmentId;

      let statusColor = '#3b82f6'; // blue
      if (shipment.currentStatus === 'AT_RISK') statusColor = '#ef4444'; // red
      if (shipment.currentStatus === 'REROUTED') statusColor = '#f59e0b'; // amber
      if (shipment.currentStatus === 'ARRIVED') statusColor = '#10b981'; // green

      const shipIcon = L.divIcon({
        className: 'custom-ship-marker',
        html: `
          <div class="relative flex items-center justify-center p-1.5 rounded-full shadow-lg cursor-pointer ${
            isSelected ? 'ring-4 ring-blue-400 scale-110' : ''
          }" style="background-color: ${statusColor};">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 13l2 8h14l2-8M5 13V6a2 2 0 012-2h10a2 2 0 012 2v7M9 13h6" />
            </svg>
          </div>
        `,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });

      const marker = L.marker([shipment.currentLatitude, shipment.currentLongitude], { icon: shipIcon })
        .addTo(layerGroup);

      marker.on('click', () => {
        onSelectShipmentForSimulation(shipment);
      });

      marker.bindPopup(`
        <div class="p-2 font-sans text-xs space-y-1">
          <div class="font-bold text-sm text-slate-900">${shipment.trackingNumber}</div>
          <div class="text-slate-600">Carrier: ${shipment.carrierCode} | Mode: ${shipment.transportMode}</div>
          <div class="text-slate-600">Route: ${shipment.originPort} &rarr; <strong>${shipment.destinationPort}</strong></div>
          <div class="font-semibold text-xs mt-1" style="color: ${statusColor};">Status: ${shipment.currentStatus}</div>
        </div>
      `);

      // Draw trajectory line if waypoints exist
      if (shipment.waypoints && shipment.waypoints.length > 0) {
        const lineCoords: [number, number][] = [
          [shipment.currentLatitude, shipment.currentLongitude],
          ...shipment.waypoints.map(w => [w.latitude, w.longitude] as [number, number])
        ];

        L.polyline(lineCoords, {
          color: statusColor,
          weight: 2,
          dashArray: shipment.currentStatus === 'REROUTED' ? '6, 6' : undefined,
          opacity: 0.75
        }).addTo(layerGroup);
      }
    });

  }, [shipments, disruptions, selectedShipmentId, onSelectShipmentForSimulation, ports]);

  // Smoothly center the map when a shipment is selected
  useEffect(() => {
    const map = mapRef.current;
    if (!map || !selectedShipmentId) return;

    const selected = shipments.find(s => s.id === selectedShipmentId);
    if (selected && selected.currentLatitude != null && selected.currentLongitude != null) {
      map.flyTo([selected.currentLatitude, selected.currentLongitude], 7, {
        duration: 1.2
      });
    }
  }, [selectedShipmentId, shipments]);

  return (
    <div className="relative w-full h-full rounded-xl overflow-hidden border border-slate-800 shadow-2xl">
      <div ref={mapContainerRef} className="w-full h-full min-h-[480px] bg-slate-950" />

      {/* Map Legend Overlay */}
      <div className="absolute top-4 left-4 bg-slate-900/90 backdrop-blur-md border border-slate-800 p-3 rounded-lg shadow-xl text-xs z-[1000] text-slate-300">
        <div className="font-semibold text-slate-100 mb-2 flex items-center gap-1.5">
          <span>Geospatial Logistics Canvas</span>
        </div>
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500 animate-pulse"></span>
            <span>Disrupted Port (Strike/Storm)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            <span>Operational Alternative Hub</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-400"></span>
            <span>Vessel At Risk (ETA &lt; 48h)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-400"></span>
            <span>Rerouted Vessel (Diverting)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-blue-500"></span>
            <span>Normal In-Transit</span>
          </div>
        </div>
      </div>
    </div>
  );
};
