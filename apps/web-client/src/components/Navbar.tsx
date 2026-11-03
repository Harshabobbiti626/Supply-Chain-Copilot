import React from 'react';
import { Ship, Radio, AlertTriangle, ShieldCheck } from 'lucide-react';

interface NavbarProps {
  currentTenant: string;
  onTenantChange: (tenant: string) => void;
  activeShipmentsCount: number;
  atRiskCount: number;
  isLiveStreaming: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTenant,
  onTenantChange,
  activeShipmentsCount,
  atRiskCount,
  isLiveStreaming
}) => {
  return (
    <header className="bg-slate-900 border-b border-slate-800 px-6 py-3 flex flex-wrap items-center justify-between gap-4 sticky top-0 z-50 shadow-lg">
      <div className="flex items-center gap-3">
        <div className="p-2 bg-blue-600/20 text-blue-400 rounded-lg border border-blue-500/30">
          <Ship className="w-6 h-6 animate-pulse" />
        </div>
        <div>
          <h1 className="text-lg font-bold text-slate-100 flex items-center gap-2">
            SupplyChain Copilot
            <span className="text-xs px-2 py-0.5 rounded-full font-mono bg-blue-900/60 text-blue-300 border border-blue-700/50">
              v1.0 • Event-Driven Hub
            </span>
          </h1>
          <p className="text-xs text-slate-400">
            Real-Time Logistics Disruption & Deterministic Route Optimization
          </p>
        </div>
      </div>

      <div className="flex items-center gap-6">
        {/* Live telemetry badge */}
        <div className="flex items-center gap-2 text-xs font-mono px-3 py-1.5 rounded-md bg-slate-800/80 border border-slate-700 text-slate-300">
          <Radio className={`w-3.5 h-3.5 ${isLiveStreaming ? 'text-emerald-400 animate-ping' : 'text-amber-400'}`} />
          <span>{isLiveStreaming ? 'LIVE KAFKA STREAM' : 'RESILIENCE MODE'}</span>
        </div>

        {/* Fleet KPI badges */}
        <div className="hidden sm:flex items-center gap-3 text-xs">
          <div className="px-3 py-1.5 rounded-md bg-slate-800/50 border border-slate-700/60">
            <span className="text-slate-400">Tracked Vessels: </span>
            <span className="font-semibold text-slate-100">{activeShipmentsCount}</span>
          </div>

          <div className={`px-3 py-1.5 rounded-md border flex items-center gap-1.5 font-medium ${
            atRiskCount > 0 
              ? 'bg-rose-950/40 border-rose-700/50 text-rose-300' 
              : 'bg-emerald-950/40 border-emerald-700/50 text-emerald-300'
          }`}>
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>{atRiskCount} At Risk</span>
          </div>
        </div>

        {/* 3PL Tenant Isolation Switcher */}
        <div className="flex items-center gap-2 bg-slate-950/70 p-1.5 rounded-lg border border-slate-800 text-xs">
          <ShieldCheck className="w-4 h-4 text-indigo-400 ml-1" />
          <span className="text-slate-400 hidden md:inline">Tenant Context:</span>
          <select
            value={currentTenant}
            onChange={(e) => onTenantChange(e.target.value)}
            className="bg-slate-900 text-slate-200 border border-slate-700 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500 font-medium"
          >
            <option value="GOFL">GOFL (Global Ocean Freight)</option>
            <option value="BW3PL">BlueWater 3PL Partner</option>
          </select>
        </div>
      </div>
    </header>
  );
};

