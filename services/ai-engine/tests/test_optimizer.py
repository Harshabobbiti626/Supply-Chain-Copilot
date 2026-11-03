import unittest
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

from app.solver.route_optimizer import OptimizationRequest, PortCandidate, solve_rerouting, ALL_CANDIDATE_HUBS
from app.agent.tradeoff_explainer import generate_executive_tradeoff_summary
from app.rag.vector_store import vector_store

class TestRouteOptimizer(unittest.TestCase):

    def test_solve_rerouting_excludes_disrupted_port_rotterdam(self):
        req = OptimizationRequest(
            shipment_id="TEST-SHP-001",
            disrupted_port="NLRTM",
            demand_teu=450,
            cost_weight=0.5,
            time_weight=0.5
        )
        options = solve_rerouting(req)
        self.assertTrue(len(options) > 0)

        # Disrupted port MUST NOT be in the recommended options
        destinations = [opt.destinationPort.upper() for opt in options]
        self.assertNotIn("NLRTM", destinations)
        self.assertIn("BEANR", destinations)

        primary = options[0]
        self.assertGreater(primary.allocatedTeu, 0)
        self.assertGreater(primary.confidenceScore, 0.8)
        self.assertTrue(len(primary.waypoints) > 0)

    def test_solve_rerouting_excludes_disrupted_port_antwerp(self):
        req = OptimizationRequest(
            shipment_id="TEST-SHP-002",
            disrupted_port="BEANR",
            demand_teu=300
        )
        options = solve_rerouting(req)
        self.assertTrue(len(options) > 0)

        # Antwerp must be excluded; Rotterdam or Hamburg or Zeebrugge should be present
        destinations = [opt.destinationPort.upper() for opt in options]
        self.assertNotIn("BEANR", destinations)
        self.assertTrue(any(d in destinations for d in ["NLRTM", "DEHAM", "BEZEE"]))

    def test_tradeoff_summary_generation(self):
        req = OptimizationRequest(disrupted_port="NLRTM", demand_teu=300)
        options = solve_rerouting(req)
        summary = generate_executive_tradeoff_summary("NLRTM", 300, options)
        self.assertIn("NLRTM", summary)
        self.assertIn("TEU", summary)

    def test_dynamic_candidate_hubs_solving(self):
        custom_hubs = [
            PortCandidate(
                port_code="FRLEH",
                port_name="Port of Le Havre (HAROPA)",
                lat=49.4944,
                lon=0.1079,
                available_capacity_teu=500,
                cost_per_teu=8.50,
                transit_delay_hours=18,
                draft_depth_meters=17.0,
                warehouse_id="wh-leh",
                waypoints=[{"name": "Seine Bay Traffic Separation", "lat": 49.50, "lon": 0.05}]
            ),
            PortCandidate(
                port_code="GBFXT",
                port_name="Port of Felixstowe",
                lat=51.9566,
                lon=1.3060,
                available_capacity_teu=400,
                cost_per_teu=9.20,
                transit_delay_hours=12,
                draft_depth_meters=16.0,
                warehouse_id="wh-fxt",
                waypoints=[{"name": "Sunk Lightship Channel", "lat": 51.85, "lon": 1.50}]
            )
        ]
        req = OptimizationRequest(
            disrupted_port="NLRTM",
            demand_teu=350,
            candidate_hubs=custom_hubs
        )
        options = solve_rerouting(req)
        self.assertEqual(len(options), 2)
        destinations = [opt.destinationPort for opt in options]
        self.assertIn("FRLEH", destinations)
        self.assertIn("GBFXT", destinations)
        self.assertEqual(options[0].allocatedTeu, 350)

    def test_rag_vector_search(self):
        results = vector_store.search_similar("crane operator strike at Rotterdam ECT", top_k=2)
        self.assertTrue(len(results) > 0)
        top_match = results[0]
        self.assertIn("Rotterdam", top_match.document.title)
        self.assertGreater(top_match.similarity_score, 0.0)

if __name__ == '__main__':
    unittest.main()
