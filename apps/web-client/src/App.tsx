import { useEffect, useState, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { Navbar } from './components/Navbar';
import { GeospatialMap } from './components/GeospatialMap';
import { SimulationSandbox } from './components/SimulationSandbox';
import { DisruptionFeed } from './components/DisruptionFeed';
import { InventoryRiskMatrix } from './components/InventoryRiskMatrix';
import { fetchShipments, fetchDisruptions, fetchInventory, fetchPorts } from './services/api';
import type { Shipment, DisruptionAdvisory, InventoryStock, AlternativeRouteOption, Port } from './types';

export function App() {
  const [currentTenant, setCurrentTenant] = useState<string>('GOFL');
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [disruptions, setDisruptions] = useState<DisruptionAdvisory[]>([]);
  const [inventory, setInventory] = useState<InventoryStock[]>([]);
  const [ports, setPorts] = useState<Port[]>([]);
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);
  const [isLiveStreaming, setIsLiveStreaming] = useState<boolean>(false);
  const stompClientRef = useRef<Client | null>(null);

  const loadInitialData = useCallback(async () => {
    const [fetchedShipments, fetchedDisruptions, fetchedInventory, fetchedPorts] = await Promise.all([
      fetchShipments(),
      fetchDisruptions(),
      fetchInventory(),
      fetchPorts()
    ]);

    setShipments(prev => {
      // Preserve any local re-routes committed in demo mode
      if (prev.length > 0) {
        return fetchedShipments.map(fs => {
          const existing = prev.find(p => p.id === fs.id);
          return existing && existing.currentStatus === 'REROUTED' ? existing : fs;
        });
      }
      return fetchedShipments;
    });

    setDisruptions(fetchedDisruptions);
    setInventory(fetchedInventory);
    setPorts(fetchedPorts);

    // Auto-select the initial vessel for immediate simulation exploration
    setSelectedShipment(current => {
      if (current) return current;
      const atRisk = fetchedShipments.find(s => s.currentStatus === 'AT_RISK') || fetchedShipments[0] || null;
      return atRisk;
    });
  }, []);

  useEffect(() => {
    let ignore = false;

    async function fetchInitial() {
      const [fetchedShipments, fetchedDisruptions, fetchedInventory, fetchedPorts] = await Promise.all([
        fetchShipments(),
        fetchDisruptions(),
        fetchInventory(),
        fetchPorts()
      ]);

      if (!ignore) {
        setShipments(fetchedShipments);
        setDisruptions(fetchedDisruptions);
        setInventory(fetchedInventory);
        setPorts(fetchedPorts);
        const atRisk = fetchedShipments.find(s => s.currentStatus === 'AT_RISK') || fetchedShipments[0] || null;
        setSelectedShipment(atRisk);
      }
    }

    fetchInitial();

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    // Establish WebSocket STOMP client with silent fallback
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const brokerURL = window.location.port === '5173'
      ? `${protocol}//${window.location.hostname}:8081/ws-telemetry`
      : `${protocol}//${window.location.host}/ws-telemetry`;

    const stompClient = new Client({
      brokerURL,
      reconnectDelay: 10000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: () => {}, // Suppress console noise when backend is starting
      onConnect: () => {
        setIsLiveStreaming(true);

        stompClient.subscribe('/topic/telemetry', message => {
          try {
            const updatedShipment: Shipment = JSON.parse(message.body);
            setShipments(prev => {
              const idx = prev.findIndex(s => s.id === updatedShipment.id);
              if (idx >= 0) {
                const next = [...prev];
                next[idx] = updatedShipment;
                return next;
              }
              return [...prev, updatedShipment];
            });

            setSelectedShipment(current => {
              if (current && current.id === updatedShipment.id) {
                return updatedShipment;
              }
              return current;
            });
          } catch {
            // Silently handle parse anomalies
          }
        });

        stompClient.subscribe('/topic/disruptions', message => {
          try {
            const newAlert: DisruptionAdvisory = JSON.parse(message.body);
            setDisruptions(prev => [newAlert, ...prev]);
          } catch {
            // Silently handle parse anomalies
          }
        });
      },
      onDisconnect: () => {
        setIsLiveStreaming(false);
      },
      onStompError: () => {
        setIsLiveStreaming(false);
      },
      onWebSocketClose: () => {
        setIsLiveStreaming(false);
      }
    });

    try {
      stompClient.activate();
      stompClientRef.current = stompClient;
    } catch {
      // Backend offline; continue gracefully
    }

    // Local telemetry generator if backend WebSocket is offline
    const telemetryInterval = setInterval(() => {
      if (!isLiveStreaming) {
        setShipments(prev =>
          prev.map(s => {
            if (s.currentLatitude == null || s.currentLongitude == null) return s;
            const latDelta = (Math.random() - 0.3) * 0.004;
            const lonDelta = (Math.random() + 0.1) * 0.006;
            return {
              ...s,
              currentLatitude: +(s.currentLatitude + latDelta).toFixed(6),
              currentLongitude: +(s.currentLongitude + lonDelta).toFixed(6)
            };
          })
        );
      }
    }, 4000);

    const pollingInterval = setInterval(() => {
      loadInitialData();
    }, 12000);

    return () => {
      clearInterval(telemetryInterval);
      clearInterval(pollingInterval);
      if (stompClientRef.current) {
        try {
          stompClientRef.current.deactivate();
        } catch {
          // ignore shutdown error
        }
      }
    };
  }, [loadInitialData, isLiveStreaming]);

  const handleSimulationCommitted = (option: AlternativeRouteOption) => {
    if (selectedShipment && option) {
      setShipments(prev =>
        prev.map(s =>
          s.id === selectedShipment.id
            ? { ...s, currentStatus: 'REROUTED', destinationPort: option.destinationPort }
            : s
        )
      );
      setSelectedShipment(prev =>
        prev && prev.id === selectedShipment.id
          ? { ...prev, currentStatus: 'REROUTED', destinationPort: option.destinationPort }
          : prev
      );
    }
    loadInitialData();
  };

  const atRiskCount = shipments.filter(s => s.currentStatus === 'AT_RISK').length;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500 selection:text-white">
      <Navbar
        currentTenant={currentTenant}
        onTenantChange={setCurrentTenant}
        activeShipmentsCount={shipments.length}
        atRiskCount={atRiskCount}
        isLiveStreaming={isLiveStreaming}
      />

      <main className="flex-1 p-4 md:p-6 space-y-6 max-w-[1700px] w-full mx-auto">
        {/* Top Grid: Geospatial Canvas & Disruption Sandbox */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Geospatial Map Canvas (7 cols on desktop) */}
          <div className="lg:col-span-7 h-[520px]">
            <GeospatialMap
              shipments={shipments}
              disruptions={disruptions}
              ports={ports}
              onSelectShipmentForSimulation={setSelectedShipment}
              selectedShipmentId={selectedShipment?.id}
            />
          </div>

          {/* Simulation Sandbox (5 cols on desktop) */}
          <div className="lg:col-span-5">
            <SimulationSandbox
              selectedShipment={selectedShipment}
              ports={ports}
              disruptions={disruptions}
              onSimulationCommitted={handleSimulationCommitted}
            />
          </div>
        </div>

        {/* Bottom Grid: Continuous RAG Disruption Feed & Inventory Yard Capacity */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <div className="lg:col-span-6">
            <DisruptionFeed disruptions={disruptions} />
          </div>

          <div className="lg:col-span-6">
            <InventoryRiskMatrix inventory={inventory} />
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
