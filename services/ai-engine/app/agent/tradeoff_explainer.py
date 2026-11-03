"""
Natural Language Trade-off Reasoning Agent.
Synthesizes deterministic OR-Tools solver results and RAG maritime advisories
into actionable, enterprise-grade executive briefs using Gemini or deterministic fallback.
"""

from typing import List, Optional
import os
import logging
from app.solver.route_optimizer import RouteOption
from app.rag.vector_store import AdvisoryDocument

logger = logging.getLogger("supplychain.ai-engine.tradeoff_agent")


def _synthesize_deterministic_summary(
    disrupted_port: str,
    demand_teu: int,
    options: List[RouteOption],
    advisories: Optional[List[AdvisoryDocument]] = None
) -> str:
    """
    Resilient multi-criteria deterministic synthesizer incorporating RAG advisories,
    operational dwell risks, and solver cost/transit metrics.
    """
    if not options:
        return f"OPERATIONAL ALERT: No feasible alternative maritime hubs found for disrupted port {disrupted_port}."

    primary = options[0]
    alternatives = options[1:] if len(options) > 1 else []

    # 1. RAG Context Formulation
    advisory_context = ""
    if advisories and len(advisories) > 0:
        top_advisory = advisories[0]
        advisory_context = (
            f"Active incident report [{top_advisory.event_type}]: {top_advisory.title}. "
        )

    # 2. Alternative Options Trade-off
    alt_text = ""
    if alternatives:
        alt_names = [
            f"{a.portName} (+{a.transitDelayHours}h, +${a.additionalCostUsd:,.0f})"
            for a in alternatives
        ]
        alt_text = f" Secondary hubs evaluated: {'; '.join(alt_names)}."

    # 3. Demurrage & Financial Risk Avoidance
    avoidance_est = max(demand_teu * 150, 25000)

    summary = (
        f"OPERATIONAL RE-ROUTING BRIEF: Severe disruption detected at {disrupted_port}. {advisory_context}"
        f"The OR-Tools MILP solver evaluated regional candidate container terminals for {demand_teu} TEU freight. "
        f"Recommended diversion: {primary.portName} ({primary.destinationPort}) with +{primary.transitDelayHours}h transit variance "
        f"and +${primary.additionalCostUsd:,.2f} freight cost delta ({primary.allocatedTeu} TEU allocated at {primary.confidenceScore * 100:.0f}% confidence).{alt_text} "
        f"Action: Proactive rerouting mitigates an estimated ${avoidance_est:,.0f} in terminal demurrage fees; "
        f"inland rail slots and cross-dock receiving gates prioritized at {primary.destinationPort}."
    )
    return summary


def _call_gemini_agent(
    disrupted_port: str,
    demand_teu: int,
    options: List[RouteOption],
    advisories: Optional[List[AdvisoryDocument]],
    api_key: str
) -> Optional[str]:
    """
    Calls the Google Gemini API to synthesize multi-criteria reasoning over RAG and solver data.
    """
    try:
        from google import genai
        client = genai.Client(api_key=api_key)

        primary = options[0]
        alternatives = options[1:] if len(options) > 1 else []

        advisory_bullet_points = "None reported."
        if advisories and len(advisories) > 0:
            advisory_bullet_points = "\n".join(
                f"- [{adv.event_type} - Severity {adv.severity_score}]: {adv.title} ({adv.content})"
                for adv in advisories[:3]
            )

        alt_bullet_points = "None available."
        if alternatives:
            alt_bullet_points = "\n".join(
                f"- {alt.portName} ({alt.destinationPort}): +{alt.transitDelayHours}h transit delay, +${alt.additionalCostUsd:,.2f} cost, {alt.allocatedTeu} TEU"
                for alt in alternatives
            )

        prompt = f"""You are a Senior Maritime Logistics & Supply Chain Risk Strategist for a Fortune 500 carrier network.
A critical port disruption requires immediate container rerouting.

Disrupted Port: {disrupted_port}
Freight Volume: {demand_teu} TEU

Active Maritime RAG Telemetry & Advisories:
{advisory_bullet_points}

Optimization Solver (Google OR-Tools MILP) Recommendations:
- Primary Winner: {primary.portName} ({primary.destinationPort})
  - Transit Delay: +{primary.transitDelayHours} hours
  - Cost Delta: +${primary.additionalCostUsd:,.2f}
  - Volume Allocated: {primary.allocatedTeu} TEU
  - Solver Confidence: {primary.confidenceScore * 100:.0f}%
- Secondary Viable Alternatives:
{alt_bullet_points}

Instructions:
Write an authoritative, 3 to 4 sentence operational executive briefing for supply chain leadership.
1. Reference the specific root cause and severity from the maritime advisory (e.g. strike, storm, blockage).
2. Justify why the primary port ({primary.portName}) was chosen over secondary alternatives based on cost, transit time, and container dwell capacity.
3. Quantify financial risk mitigation (demurrage fee avoidance) and confirm immediate inland actions (rail interchange, warehouse dock scheduling).
Do not use bullet points or markdown headings. Provide a single unified, professional paragraph.
"""

        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt
        )

        if response and response.text:
            text = response.text.strip()
            if len(text) > 30:
                logger.info(f"Successfully generated executive summary via Gemini 2.5 Flash for port {disrupted_port}")
                return text

    except Exception as exc:
        logger.warning(f"Gemini LLM call failed or unavailable ({exc}); falling back to deterministic synthesizer.")

    return None


def generate_executive_tradeoff_summary(
    disrupted_port: str,
    demand_teu: int,
    options: List[RouteOption],
    advisories: Optional[List[AdvisoryDocument]] = None
) -> str:
    """
    Generates structured executive insights with cost, transit variance, and risk assessments.
    Leverages Gemini LLM when GEMINI_API_KEY is available, with seamless graceful degradation.
    """
    if not options:
        return f"Warning: No feasible alternative hubs found for disrupted port {disrupted_port}."

    # 1. Attempt LLM generation if API key is present
    api_key = os.environ.get("GEMINI_API_KEY") or os.environ.get("GOOGLE_API_KEY")
    if api_key and api_key.strip():
        llm_summary = _call_gemini_agent(
            disrupted_port=disrupted_port,
            demand_teu=demand_teu,
            options=options,
            advisories=advisories,
            api_key=api_key.strip()
        )
        if llm_summary:
            return llm_summary

    # 2. Resilient Deterministic Fallback
    return _synthesize_deterministic_summary(
        disrupted_port=disrupted_port,
        demand_teu=demand_teu,
        options=options,
        advisories=advisories
    )
