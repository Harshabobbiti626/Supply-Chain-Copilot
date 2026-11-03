import React, { useState } from 'react';
import {
  Play,
  CheckCircle2,
  Sliders,
  Sparkles,
  Clock,
  DollarSign,
  Box,
  ShieldAlert,
  Award,
  TrendingDown,
  ShieldCheck,
  AlertTriangle
} from 'lucide-react';
import type { Shipment, SimulationResponse, AlternativeRouteOption, Port, DisruptionAdvisory } from '../types';
import { runSimulation, commitSimulation } from '../services/api';

interface SimulationSandboxProps {
  selectedShipment: Shipment | null;
  onSimulationCommitted: (option: AlternativeRouteOption) => void;
  ports?: Port[];
  disruptions?: DisruptionAdvisory[];
}

export const SimulationSandbox: React.FC<SimulationSandboxProps> = ({
  selectedShipment,
  onSimulationCommitted,
  ports,
  disruptions
}) => {
  const [disruptedPort, setDisruptedPort] = useState<string>('NLRTM');
  const [costWeight, setCostWeight] = useState<number>(0.5);
  const [timeWeight, setTimeWeight] = useState<number>(0.5);
  const [demandTeu, setDemandTeu] = useState<number>(450);
  const [loading, setLoading] = useState<boolean>(false);
  const [committing, setCommitting] = useState<boolean>(false);
  const [simulationResult, setSimulationResult] = useState<SimulationResponse | null>(null);
  const [commitSuccess, setCommitSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const effectivePort = selectedShipment?.destinationPort || disruptedPort;
  const activeIncident = disruptions?.find(
    d => d.affectedPortCode && d.affectedPortCode.toUpperCase() === effectivePort.toUpperCase()
  );

  const handleRunSimulation = async () => {
    setLoading(true);
    setError(null);
    setCommitSuccess(null);

    const shipmentId = selectedShipment?.id || 'SHP-SIM-DEMO';

    try {
      const response = await runSimulation({
        shipmentId,
        disruptedPort: effectivePort,
        demandTeu,
        costWeight,
        timeWeight,
        reason: 'Labor Strike & High Wave Anomaly'
      });
      setSimulationResult(response);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Simulation execution failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleCommit = async (option: AlternativeRouteOption) => {
    if (!simulationResult) return;

    setCommitting(true);
    setError(null);
    const shipmentId = selectedShipment?.id || 'SHP-SIM-DEMO';

    try {
      await commitSimulation(simulationResult.simulationId, shipmentId, option);

      setCommitSuccess(
        `Successfully committed ACID inventory reservation and re-routed to ${option.portName} (${option.destinationPort})!`
      );
      onSimulationCommitted(option);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Transaction commitment failed';
      setError(msg);
    } finally {
      setCommitting(false);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-xl space-y-5">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-md bg-indigo-500/20 text-indigo-400">
            <Sliders className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-100">Disruption Simulation Sandbox</h2>
            <p className="text-xs text-slate-400">Constraint-based alternative route & inventory solver</p>
          </div>
        </div>

        <div className="text-right">
          <span className="text-xs font-mono px-2 py-1 rounded bg-blue-950/60 text-blue-300 border border-blue-800/50">
            {selectedShipment 
              ? `Target: ${selectedShipment.trackingNumber} (${selectedShipment.carrierCode})`
              : 'Target: Corridor Fleet Simulation'}
          </span>
        </div>
      </div>

      {error && (
        <div className="p-3 rounded-lg bg-rose-950/50 border border-rose-800/60 text-rose-300 text-xs flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {commitSuccess && (
        <div className="p-3 rounded-lg bg-emerald-950/50 border border-emerald-800/60 text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
          <span>{commitSuccess}</span>
        </div>
      )}

      {/* Control Parameters */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">Disrupted Destination</label>
          <select
            value={effectivePort}
            onChange={(e) => setDisruptedPort(e.target.value)}
            className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:ring-1 focus:ring-indigo-500"
          >
            {ports && ports.length > 0 ? (
              ports.map(port => (
                <option key={port.code} value={port.code}>
                  {port.code} - {port.name} {port.code === 'NLRTM' ? '(Strike Active)' : ''}
                </option>
              ))
            ) : (
              <>
                <option value="NLRTM">NLRTM - Port of Rotterdam (Strike Active)</option>
                <option value="BEANR">BEANR - Port of Antwerp-Bruges</option>
                <option value="DEHAM">DEHAM - Port of Hamburg</option>
                <option value="BEZEE">BEZEE - Port of Zeebrugge</option>
              </>
            )}
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">
            Freight Demand (TEU): <span className="text-slate-200 font-semibold">{demandTeu}</span>
          </label>
          <input
            type="range"
            min="100"
            max="1200"
            step="50"
            value={demandTeu}
            onChange={(e) => setDemandTeu(Number(e.target.value))}
            className="w-full accent-indigo-500 cursor-pointer"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">
            Priority Trade-off: <span className="text-slate-200 font-semibold">{costWeight > timeWeight ? 'Cost Minimization' : 'Transit Speed'}</span>
          </label>
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span>Cheapest</span>
            <input
              type="range"
              min="0.1"
              max="0.9"
              step="0.1"
              value={costWeight}
              onChange={(e) => {
                const val = Number(e.target.value);
                setCostWeight(val);
                setTimeWeight(Number((1 - val).toFixed(1)));
              }}
              className="w-full accent-indigo-500 cursor-pointer"
            />
            <span>Fastest</span>
          </div>
        </div>
      </div>

      {/* Active Port Disruption Banner (RAG Grounding) */}
      {activeIncident && (
        <div className="p-3 rounded-lg bg-rose-950/40 border border-rose-800/60 text-rose-300 text-xs flex items-start gap-2.5">
          <AlertTriangle className="w-4 h-4 shrink-0 text-rose-400 mt-0.5" />
          <div className="space-y-0.5">
            <div className="font-semibold text-rose-200 flex items-center gap-2">
              <span>Active Maritime Advisory: {activeIncident.title}</span>
              <span className="text-[10px] px-1.5 py-0.2 rounded bg-rose-900/60 text-rose-300 font-mono">
                {activeIncident.eventType}
              </span>
            </div>
            <div className="text-[11px] text-rose-300/80 leading-relaxed">
              {activeIncident.content}
            </div>
          </div>
        </div>
      )}

      <button
        onClick={handleRunSimulation}
        disabled={loading}
        className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-600 text-white font-medium text-xs rounded-lg shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer"
      >
        <Play className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        <span>{loading ? 'Running Google OR-Tools CP-SAT Solver...' : 'Run Constraint Optimization'}</span>
      </button>

      {/* Simulation Result Presentation */}
      {simulationResult && (
        <div className="space-y-4 pt-3 border-t border-slate-800">
          {/* Financial ROI & Risk Telemetry Card */}
          {(() => {
            const primaryOpt = simulationResult.recommendedOptions[0];
            const primaryCost = primaryOpt?.additionalCostUsd || 0;
            const estimatedDemurrageAvoidance = Math.max(demandTeu * 150 * 2, 35000);
            const netBenefit = Math.max(0, estimatedDemurrageAvoidance - primaryCost);

            return (
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3 p-3.5 rounded-xl bg-slate-950 border border-slate-800 shadow-inner">
                <div className="space-y-0.5">
                  <span className="text-[10px] text-slate-400 uppercase tracking-wider flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" /> Est. D&D Avoidance
                  </span>
                  <div className="text-sm font-bold text-emerald-400 font-mono">
                    +${estimatedDemurrageAvoidance.toLocaleString()}
                  </div>
                  <span className="text-[10px] text-slate-500">2-day port halt averted</span>
                </div>

                <div className="space-y-0.5">
                  <span className="text-[10px] text-slate-400 uppercase tracking-wider flex items-center gap-1">
                    <DollarSign className="w-3.5 h-3.5 text-amber-400" /> Reroute Cost Delta
                  </span>
                  <div className="text-sm font-bold text-amber-300 font-mono">
                    +${primaryCost.toLocaleString()}
                  </div>
                  <span className="text-[10px] text-slate-500">Optimal carrier premium</span>
                </div>

                <div className="col-span-2 md:col-span-1 space-y-0.5 border-t md:border-t-0 md:border-l border-slate-800 pt-2 md:pt-0 md:pl-3">
                  <span className="text-[10px] text-slate-400 uppercase tracking-wider flex items-center gap-1">
                    <TrendingDown className="w-3.5 h-3.5 text-cyan-400" /> Net Risk Savings
                  </span>
                  <div className="text-sm font-bold text-cyan-300 font-mono">
                    +${netBenefit.toLocaleString()}
                  </div>
                  <span className="text-[10px] text-slate-500">Bottom-line benefit</span>
                </div>
              </div>
            );
          })()}

          {/* AI Executive Summary Brief */}
          <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-900/40 relative space-y-2">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-indigo-900/30 pb-2">
              <div className="flex items-center gap-2 text-indigo-400 text-xs font-semibold">
                <Sparkles className="w-4 h-4 text-indigo-400" />
                <span>AI Executive Trade-Off Brief • {simulationResult.solverEngine}</span>
              </div>
              <div className="flex items-center gap-2">
                {activeIncident && (
                  <span className="px-2 py-0.5 rounded text-[10px] bg-rose-500/20 text-rose-300 border border-rose-500/30 font-medium flex items-center gap-1">
                    <AlertTriangle className="w-3 h-3 text-rose-400" /> RAG Grounded
                  </span>
                )}
                <span className="text-slate-500 text-[10px] font-mono">
                  Computed in {simulationResult.computationTimeMs}ms
                </span>
              </div>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed font-sans">
              {simulationResult.executiveSummary}
            </p>
          </div>

          {/* Alternative Route Options Table */}
          <div className="space-y-2">
            <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider">
              Constraint-Optimized Alternative Destinations
            </h3>
            <div className="grid grid-cols-1 gap-2.5">
              {simulationResult.recommendedOptions.map((opt, idx) => {
                const isOptimal = idx === 0;
                return (
                  <div
                    key={idx}
                    className={`p-3.5 rounded-lg transition flex flex-col md:flex-row md:items-center justify-between gap-3 ${
                      isOptimal
                        ? 'bg-slate-950 border border-emerald-500/50 ring-1 ring-emerald-500/20 shadow-lg shadow-emerald-950/30'
                        : 'bg-slate-950 border border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        {isOptimal ? (
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-500 text-slate-950 flex items-center gap-1 shadow-sm">
                            <Award className="w-3 h-3" /> OPTIMAL DISPATCH
                          </span>
                        ) : (
                          <span className="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">
                            Alternative #{idx + 1}
                          </span>
                        )}
                        <span className="text-xs font-bold text-slate-100">{opt.portName}</span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-mono">
                          {opt.destinationPort}
                        </span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-950/70 text-emerald-300 border border-emerald-800/40 font-mono">
                          {Math.round(opt.confidenceScore * 100)}% Match
                        </span>
                      </div>
                      <p className="text-xs text-slate-400">{opt.rationale}</p>
                      <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-300 pt-1">
                        <span className="flex items-center gap-1 text-amber-400">
                          <Clock className="w-3.5 h-3.5" /> +{opt.transitDelayHours}h delay
                        </span>
                        <span className="flex items-center gap-1 text-emerald-400">
                          <DollarSign className="w-3.5 h-3.5" /> +${opt.additionalCostUsd.toLocaleString()}
                        </span>
                        <span className="flex items-center gap-1 text-blue-400">
                          <Box className="w-3.5 h-3.5" /> {opt.allocatedTeu} TEU capacity
                        </span>
                      </div>
                    </div>

                    <button
                      onClick={() => handleCommit(opt)}
                      disabled={committing}
                      className={`px-4 py-2 text-white text-xs font-medium rounded-lg transition shadow-md whitespace-nowrap cursor-pointer flex items-center justify-center gap-1.5 ${
                        isOptimal
                          ? 'bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-800'
                          : 'bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:bg-slate-800'
                      }`}
                    >
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      <span>{committing ? 'Committing ACID Lock...' : isOptimal ? 'Authorize Optimal Re-route' : 'Select Alternative'}</span>
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
