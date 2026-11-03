export type TransportMode = 'MARITIME' | 'ROAD' | 'RAIL' | 'AIR';
export type ShipmentStatus = 'IN_TRANSIT' | 'DELAYED' | 'REROUTED' | 'ARRIVED' | 'AT_RISK';

export interface Waypoint {
  id?: string;
  sequenceOrder: number;
  locationName: string;
  latitude: number;
  longitude: number;
  passed: boolean;
  estimatedArrival?: string;
}

export interface LogisticsCorridor {
  id: string;
  name: string;
  originRegion: string;
  destinationRegion: string;
  active: boolean;
}

export interface Shipment {
  id: string;
  trackingNumber: string;
  carrierCode: string;
  transportMode: TransportMode;
  currentStatus: ShipmentStatus;
  currentLatitude: number;
  currentLongitude: number;
  originPort: string;
  destinationPort: string;
  estimatedArrival: string;
  corridor?: LogisticsCorridor;
  waypoints?: Waypoint[];
  version: number;
}

export interface DisruptionAdvisory {
  id: string;
  title: string;
  content: string;
  sourceUrl?: string;
  eventType: string;
  severityScore: number;
  affectedPortCode?: string;
  publishedAt: string;
}

export interface Warehouse {
  id: string;
  name: string;
  code: string;
  city: string;
  country: string;
  maxCapacityTeu: number;
  currentOccupancyTeu: number;
}

export interface InventoryStock {
  id: string;
  warehouse: Warehouse;
  sku: string;
  totalQuantity: number;
  allocatedQuantity: number;
  availableQuantity: number;
}

export interface Port {
  id?: string;
  code: string;
  name: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  draftDepthMeters?: number;
  maxCapacityTeu?: number;
  availableCapacityTeu?: number;
  costPerTeu?: number;
  transitDelayHours?: number;
  active?: boolean;
}

export interface AlternativeRouteOption {
  destinationPort: string;
  portName: string;
  warehouseId?: string;
  allocatedTeu: number;
  additionalCostUsd: number;
  transitDelayHours: number;
  confidenceScore: number;
  rationale: string;
  waypoints?: { name: string; lat: number; lon: number }[];
}

export interface SimulationResponse {
  simulationId: string;
  shipmentId: string;
  disruptedPort: string;
  recommendedOptions: AlternativeRouteOption[];
  executiveSummary: string;
  computationTimeMs: number;
  solverEngine: string;
}

