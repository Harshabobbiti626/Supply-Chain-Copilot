import React from 'react';
import { AlertCircle, ExternalLink, Flame, CloudLightning, Anchor } from 'lucide-react';
import type { DisruptionAdvisory } from '../types';

interface DisruptionFeedProps {
  disruptions: DisruptionAdvisory[];
}

export const DisruptionFeed: React.FC<DisruptionFeedProps> = ({ disruptions }) => {
  const getEventIcon = (type: string) => {
    switch (type) {
      case 'LABOR_STRIKE':
        return <Flame className="w-4 h-4 text-rose-400" />;
      case 'WEATHER_ANOMALY':
        return <CloudLightning className="w-4 h-4 text-amber-400" />;
      default:
        return <Anchor className="w-4 h-4 text-blue-400" />;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-xl flex flex-col h-full">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-3">
        <div className="flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-400" />
          <h3 className="text-xs font-bold text-slate-100 uppercase tracking-wider">
            Maritime & Weather RAG Advisories
          </h3>
        </div>
        <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">
          Continuous pgvector Feed
        </span>
      </div>

      <div className="space-y-3 overflow-y-auto max-h-[360px] pr-1">
        {disruptions.length === 0 ? (
          <div className="text-xs text-slate-500 py-6 text-center">No active disruptions detected.</div>
        ) : (
          disruptions.map(advisory => (
            <div
              key={advisory.id}
              className="p-3 rounded-lg bg-slate-950/70 border border-slate-800/80 hover:border-slate-700 transition space-y-1.5"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5 font-semibold text-xs text-slate-200">
                  {getEventIcon(advisory.eventType)}
                  <span className="truncate">{advisory.title}</span>
                </div>
                <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-rose-950/60 text-rose-300 border border-rose-800/40 shrink-0">
                  SEV {(advisory.severityScore * 10).toFixed(1)}
                </span>
              </div>

              <p className="text-xs text-slate-400 leading-snug line-clamp-2">
                {advisory.content}
              </p>

              <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-900">
                <span>Port: <strong className="text-slate-300">{advisory.affectedPortCode || 'REGIONAL'}</strong></span>
                {advisory.sourceUrl && (
                  <a
                    href={advisory.sourceUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="flex items-center gap-1 text-blue-400 hover:text-blue-300"
                  >
                    <span>Source</span>
                    <ExternalLink className="w-3 h-3" />
                  </a>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
