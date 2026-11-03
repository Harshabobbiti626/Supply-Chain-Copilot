import React from 'react';
import { Warehouse as WarehouseIcon, Layers } from 'lucide-react';
import type { InventoryStock } from '../types';

interface InventoryRiskMatrixProps {
  inventory: InventoryStock[];
}

export const InventoryRiskMatrix: React.FC<InventoryRiskMatrixProps> = ({ inventory }) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-xl flex flex-col h-full">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-3">
        <div className="flex items-center gap-2">
          <WarehouseIcon className="w-4 h-4 text-emerald-400" />
          <h3 className="text-xs font-bold text-slate-100 uppercase tracking-wider">
            Terminal Yard Capacity & ACID Allocations
          </h3>
        </div>
        <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">
          PostgreSQL ACID Locked
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 overflow-y-auto max-h-[360px] pr-1">
        {inventory.length === 0 ? (
          <div className="text-xs text-slate-500 py-6 text-center col-span-2">
            Loading terminal yard stock...
          </div>
        ) : (
          inventory.map(stock => {
            const wh = stock.warehouse;
            const occupancyPct = Math.round((wh.currentOccupancyTeu / wh.maxCapacityTeu) * 100);
            const isHighRisk = occupancyPct >= 85;

            return (
              <div
                key={stock.id}
                className="p-3 rounded-lg bg-slate-950/70 border border-slate-800/80 hover:border-slate-700 transition space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-slate-200">{wh.name}</h4>
                    <p className="text-[10px] text-slate-400 font-mono">{wh.city}, {wh.country} ({wh.code})</p>
                  </div>
                  <span
                    className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded ${
                      isHighRisk
                        ? 'bg-rose-950/70 text-rose-300 border border-rose-800/50'
                        : 'bg-emerald-950/70 text-emerald-300 border border-emerald-800/50'
                    }`}
                  >
                    {occupancyPct}% Yard Load
                  </span>
                </div>

                {/* Capacity Progress Bar */}
                <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isHighRisk ? 'bg-rose-500' : 'bg-emerald-500'
                    }`}
                    style={{ width: `${Math.min(occupancyPct, 100)}%` }}
                  />
                </div>

                <div className="flex items-center justify-between text-[10px] font-mono text-slate-400 pt-1 border-t border-slate-900">
                  <span className="flex items-center gap-1">
                    <Layers className="w-3 h-3 text-slate-500" />
                    <span>Avail: <strong className="text-slate-200">{stock.availableQuantity} TEU</strong></span>
                  </span>
                  <span>
                    Alloc: <strong className="text-indigo-300">{stock.allocatedQuantity} TEU</strong>
                  </span>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
