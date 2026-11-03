"""
Unit tests for the RAG-grounded and LLM-powered Trade-off Reasoning Agent.
"""

import unittest
from unittest.mock import patch, MagicMock
import sys
from pathlib import Path
import os

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

from app.solver.route_optimizer import OptimizationRequest, solve_rerouting
from app.rag.vector_store import AdvisoryDocument, vector_store
from app.agent.tradeoff_explainer import (
    generate_executive_tradeoff_summary,
    _synthesize_deterministic_summary,
    _call_gemini_agent
)
from fastapi.testclient import TestClient
from app.main import app


class TestTradeoffAgent(unittest.TestCase):

    def setUp(self):
        self.req = OptimizationRequest(
            disrupted_port="NLRTM",
            demand_teu=400,
            cost_weight=0.6,
            time_weight=0.4
        )
        self.options = solve_rerouting(self.req)

        self.sample_advisory = AdvisoryDocument(
            id="ADV-TEST-01",
            title="Rotterdam ECT Terminal Wildcat Strike",
            content="Gantry crane operators commenced an unscheduled strike halting gate operations.",
            event_type="LABOR_STRIKE",
            severity_score=0.92,
            affected_port_code="NLRTM"
        )

    def test_deterministic_summary_with_rag_advisories(self):
        summary = _synthesize_deterministic_summary(
            disrupted_port="NLRTM",
            demand_teu=400,
            options=self.options,
            advisories=[self.sample_advisory]
        )
        self.assertIn("NLRTM", summary)
        self.assertIn("LABOR_STRIKE", summary)
        self.assertIn("Rotterdam ECT Terminal Wildcat Strike", summary)
        self.assertIn("demurrage", summary.lower())
        self.assertIn("400 TEU", summary)
        self.assertTrue(any(opt.portName in summary for opt in self.options))

    def test_deterministic_summary_without_advisories(self):
        summary = _synthesize_deterministic_summary(
            disrupted_port="DEHAM",
            demand_teu=250,
            options=self.options,
            advisories=None
        )
        self.assertIn("DEHAM", summary)
        self.assertIn("250 TEU", summary)
        self.assertIn("demurrage", summary.lower())

    def test_empty_options_handling(self):
        summary = generate_executive_tradeoff_summary(
            disrupted_port="UNKNOWN",
            demand_teu=100,
            options=[]
        )
        self.assertIn("Warning: No feasible alternative hubs", summary)

    @patch("google.genai.Client")
    def test_gemini_agent_invocation_success(self, mock_client_cls):
        mock_client = MagicMock()
        mock_response = MagicMock()
        mock_response.text = (
            "Diverting 400 TEU from strike-affected Rotterdam to Antwerp avoids severe terminal congestion "
            "and an estimated $60,000 in container detention penalties with only a 6-hour transit variance."
        )
        mock_client.models.generate_content.return_value = mock_response
        mock_client_cls.return_value = mock_client

        result = _call_gemini_agent(
            disrupted_port="NLRTM",
            demand_teu=400,
            options=self.options,
            advisories=[self.sample_advisory],
            api_key="test-api-key-12345"
        )

        self.assertIsNotNone(result)
        self.assertIn("Antwerp", result)
        self.assertIn("strike-affected Rotterdam", result)
        mock_client.models.generate_content.assert_called_once()

    @patch("google.genai.Client")
    def test_gemini_agent_fallback_on_api_error(self, mock_client_cls):
        mock_client = MagicMock()
        mock_client.models.generate_content.side_effect = RuntimeError("Quota exceeded or connection error")
        mock_client_cls.return_value = mock_client

        # Direct call to _call_gemini_agent should return None without crashing
        result = _call_gemini_agent(
            disrupted_port="NLRTM",
            demand_teu=400,
            options=self.options,
            advisories=[self.sample_advisory],
            api_key="test-api-key-12345"
        )
        self.assertIsNone(result)

        # Full generate_executive_tradeoff_summary should seamlessly fall back
        with patch.dict(os.environ, {"GEMINI_API_KEY": "test-api-key-12345"}):
            fallback_summary = generate_executive_tradeoff_summary(
                disrupted_port="NLRTM",
                demand_teu=400,
                options=self.options,
                advisories=[self.sample_advisory]
            )
            self.assertIn("OPERATIONAL RE-ROUTING BRIEF", fallback_summary)
            self.assertIn("Rotterdam ECT Terminal Wildcat Strike", fallback_summary)

    def test_fastapi_optimize_endpoint_incorporates_rag(self):
        client = TestClient(app)
        payload = {
            "shipmentId": "SHP-RAG-TEST",
            "disruptedPort": "NLRTM",
            "demandTeu": 500,
            "costWeight": 0.5,
            "timeWeight": 0.5
        }
        res = client.post("/api/v1/optimize", json=payload)
        self.assertEqual(res.status_code, 200)

        data = res.json()
        self.assertEqual(data["disruptedPort"], "NLRTM")
        self.assertTrue(len(data["recommendedOptions"]) > 0)
        self.assertIn("executiveSummary", data)
        # RAG seed advisory for NLRTM mentions strike or gale
        summary = data["executiveSummary"]
        self.assertTrue(
            "strike" in summary.lower() or "incident" in summary.lower() or "disruption" in summary.lower()
        )

