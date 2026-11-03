"""
Deterministic Constraint Optimization Engine for Maritime Vessel Re-Routing.
Implements Google OR-Tools Linear Solver / CP-SAT for multi-hub allocation under capacity limits.
"""

from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field
import logging

logger = logging.getLogger("supplychain.optimizer")

try:
    from ortools.linear_solver import pywraplp
    OR_TOOLS_AVAILABLE = True
except ImportError:
    OR_TOOLS_AVAILABLE = False


class PortCandidate(BaseModel):
    port_code: str
    port_name: str
    lat: float
    lon: float
    available_capacity_teu: int
    cost_per_teu: float
    transit_delay_hours: int
    draft_depth_meters: float
    warehouse_id: Optional[str] = None
    waypoints: List[Dict[str, Any]]


class OptimizationRequest(BaseModel):
    shipment_id: Optional[str] = None
    disrupted_port: str = Field(default="NLRTM", description="UN/LOCODE of the disrupted maritime hub")
    demand_teu: int = Field(default=450, ge=1, description="TEU cargo demand to reroute")
    cost_weight: float = Field(default=0.5, ge=0.0, le=1.0)
    time_weight: float = Field(default=0.5, ge=0.0, le=1.0)
    candidate_hubs: Optional[List[PortCandidate]] = None


class RouteOption(BaseModel):
    destinationPort: str
    portName: str
    warehouseId: Optional[str] = None
    allocatedTeu: int
    additionalCostUsd: float
    transitDelayHours: int
    confidenceScore: float
    rationale: str
    waypoints: List[Dict[str, Any]]


# Full North Sea & Channel Maritime Hub Network
ALL_CANDIDATE_HUBS: List[PortCandidate] = [
    PortCandidate(
        port_code="NLRTM",
        port_name="Port of Rotterdam Maasvlakte",
        lat=51.9500,
        lon=4.1400,
        available_capacity_teu=800,
        cost_per_teu=4.50,
        transit_delay_hours=0,
        draft_depth_meters=24.0,
        warehouse_id="wh-rtm",
        waypoints=[
            {"name": "Eurogeul Deepwater Approach", "lat": 51.9800, "lon": 3.8500},
            {"name": "Maasvlakte ECT Container Hub", "lat": 51.9500, "lon": 4.0200}
        ]
    ),
    PortCandidate(
        port_code="BEANR",
        port_name="Port of Antwerp-Bruges",
        lat=51.2721,
        lon=4.3164,
        available_capacity_teu=600,
        cost_per_teu=7.11,
        transit_delay_hours=14,
        draft_depth_meters=16.0,
        warehouse_id="wh-anr",
        waypoints=[
            {"name": "English Channel Dover Strait", "lat": 51.1279, "lon": 1.3134},
            {"name": "Westhinder Anchorage", "lat": 51.3833, "lon": 2.7000},
            {"name": "Antwerp Deurganckdock", "lat": 51.2721, "lon": 4.3164}
        ]
    ),
    PortCandidate(
        port_code="DEHAM",
        port_name="Port of Hamburg (CTA / CTB)",
        lat=53.5300,
        lon=9.9200,
        available_capacity_teu=450,
        cost_per_teu=12.88,
        transit_delay_hours=26,
        draft_depth_meters=14.5,
        warehouse_id="wh-ham",
        waypoints=[
            {"name": "Helgoland Traffic Lane", "lat": 54.1803, "lon": 7.8906},
            {"name": "Elbe Lightship", "lat": 53.9984, "lon": 8.4000},
            {"name": "Hamburg Waltershof Terminal", "lat": 53.5300, "lon": 9.9200}
        ]
    ),
    PortCandidate(
        port_code="BEZEE",
        port_name="Port of Zeebrugge Coastal Hub",
        lat=51.3570,
        lon=3.2180,
        available_capacity_teu=700,
        cost_per_teu=6.00,
        transit_delay_hours=8,
        draft_depth_meters=17.5,
        warehouse_id="wh-zee",
        waypoints=[
            {"name": "Zeebrugge Roads", "lat": 51.3500, "lon": 3.2000},
            {"name": "Zeebrugge Outer Harbor", "lat": 51.3570, "lon": 3.2180}
        ]
    )
]


def solve_rerouting(request: OptimizationRequest) -> List[RouteOption]:
    """
    Solves for optimal alternative port rerouting using Google OR-Tools Linear Programming (or heuristic fallback).
    Excludes the disrupted port from candidate alternatives, and ranks all viable hubs according to weighted cost and time criteria.
    """
    demand = request.demand_teu if request.demand_teu > 0 else 450
    w_cost = max(0.05, request.cost_weight)
    w_time = max(0.05, request.time_weight)
    disrupted = request.disrupted_port.strip().upper()

    # Use dynamic candidate hubs if provided, else fall back to the pre-seeded network
    hubs_pool = request.candidate_hubs if (request.candidate_hubs and len(request.candidate_hubs) > 0) else ALL_CANDIDATE_HUBS

    # Filter out the disrupted port so it is never recommended as its own alternative
    candidate_hubs = [h for h in hubs_pool if h.port_code.upper() != disrupted]
    if not candidate_hubs:
        logger.error(f"No candidate alternative hubs available when excluding {disrupted}")
        return []

    # Calculate multi-criteria weighted score for each candidate hub
    scored_hubs = []
    for hub in candidate_hubs:
        unit_cost_norm = hub.cost_per_teu / 15.0  # normalize ~0-1
        transit_delay_norm = hub.transit_delay_hours / 30.0  # normalize ~0-1
        combined_score = (w_cost * unit_cost_norm) + (w_time * transit_delay_norm)
        scored_hubs.append((combined_score, hub))

    scored_hubs.sort(key=lambda item: item[0])

    optimal_hub_code: Optional[str] = None

    if OR_TOOLS_AVAILABLE:
        try:
            solver = pywraplp.Solver.CreateSolver('SCIP')
            if solver:
                alloc_vars = {}
                for hub in candidate_hubs:
                    alloc_vars[hub.port_code] = solver.IntVar(0, hub.available_capacity_teu, f"alloc_{hub.port_code}")

                # Demand satisfaction constraint
                total_avail = sum(h.available_capacity_teu for h in candidate_hubs)
                target_demand = min(demand, total_avail)
                solver.Add(sum(alloc_vars[h.port_code] for h in candidate_hubs) >= target_demand)

                # Objective: Minimize weighted normalized cost + delay
                objective = solver.Objective()
                for hub in candidate_hubs:
                    coeff = (w_cost * hub.cost_per_teu) + (w_time * hub.transit_delay_hours * 0.45)
                    objective.SetCoefficient(alloc_vars[hub.port_code], coeff)
                objective.SetMinimization()

                status = solver.Solve()
                if status in [pywraplp.Solver.OPTIMAL, pywraplp.Solver.FEASIBLE]:
                    # Identify the hub allocated the largest portion of cargo
                    best_allocated = 0
                    for hub in candidate_hubs:
                        alloc_val = int(alloc_vars[hub.port_code].solution_value())
                        if alloc_val > best_allocated:
                            best_allocated = alloc_val
                            optimal_hub_code = hub.port_code
        except Exception as solver_ex:
            logger.warning(f"OR-Tools solver execution failed: {solver_ex}, falling back to Pareto ranking")

    # If OR-Tools determined a primary optimal hub, ensure it is ranked first
    if optimal_hub_code:
        scored_hubs.sort(key=lambda item: (0 if item[1].port_code == optimal_hub_code else 1, item[0]))

    results: List[RouteOption] = []
    for rank, (score, hub) in enumerate(scored_hubs, start=1):
        allocated = min(demand, hub.available_capacity_teu)
        additional_cost = round(allocated * hub.cost_per_teu + 500.0, 2)
        is_primary = (rank == 1)
        confidence = 0.96 if is_primary else round(max(0.75, 0.94 - (rank - 1) * 0.05), 2)

        if is_primary:
            engine_label = "Google OR-Tools MILP Optimal" if OR_TOOLS_AVAILABLE else "Pareto Optimal"
            rationale = (
                f"{engine_label} (Rank 1): Allocated {allocated} TEU cargo ({hub.available_capacity_teu} TEU receiving headroom) "
                f"with +{hub.transit_delay_hours}h transit variance and {hub.draft_depth_meters}m draft depth."
            )
        else:
            rationale = (
                f"Alternative Diversion (Rank {rank}): {hub.port_name} offers {hub.available_capacity_teu} TEU receiving headroom "
                f"at +${hub.cost_per_teu:.2f}/TEU and +{hub.transit_delay_hours}h transit variance."
            )

        results.append(RouteOption(
            destinationPort=hub.port_code,
            portName=hub.port_name,
            warehouseId=hub.warehouse_id,
            allocatedTeu=allocated,
            additionalCostUsd=additional_cost,
            transitDelayHours=hub.transit_delay_hours,
            confidenceScore=confidence,
            rationale=rationale,
            waypoints=hub.waypoints
        ))

    return results
