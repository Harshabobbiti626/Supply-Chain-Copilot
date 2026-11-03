"""
SupplyChain Copilot – AI & Route Optimization Engine (FastAPI)
Production-grade REST and continuous RAG service.
"""

import sys
from pathlib import Path

# Ensure ai-engine root is in sys.path
BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import List, Optional
import time
import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("supplychain.ai-engine")

from app.solver.route_optimizer import OptimizationRequest, RouteOption, solve_rerouting
from app.rag.vector_store import vector_store, AdvisoryDocument, SearchResult
from app.agent.tradeoff_explainer import generate_executive_tradeoff_summary

app = FastAPI(
    title="SupplyChain Copilot AI & Optimization Engine",
    description="Deterministic route solving and continuous maritime advisory RAG service",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled error processing {request.method} {request.url.path}: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "title": "AI Engine Internal Error",
            "status": 500,
            "detail": str(exc),
            "instance": str(request.url.path),
            "timestamp": time.time()
        }
    )


class OptimizationResponse(BaseModel):
    simulationId: Optional[str] = None
    shipmentId: Optional[str] = None
    disruptedPort: str
    recommendedOptions: List[RouteOption]
    executiveSummary: str
    computationTimeMs: float
    solverEngine: str = "PYTHON_OR_TOOLS_CPSAT"


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "ai-engine",
        "timestamp": time.time(),
        "indexed_advisories": len(vector_store.documents)
    }


@app.post("/api/v1/optimize", response_model=OptimizationResponse)
def optimize_route(request: OptimizationRequest):
    start_time = time.time()
    logger.info(f"Received optimization request for port {request.disrupted_port} with demand {request.demand_teu} TEU")
    try:
        options = solve_rerouting(request)

        # Ingest contextual RAG advisories for the disrupted port
        search_results = vector_store.search_similar(request.disrupted_port, top_k=3)
        port_advisories = [
            res.document for res in search_results
            if (res.document.affected_port_code and res.document.affected_port_code.upper() == request.disrupted_port.upper())
            or res.similarity_score > 0.05
        ]

        summary = generate_executive_tradeoff_summary(
            disrupted_port=request.disrupted_port,
            demand_teu=request.demand_teu,
            options=options,
            advisories=port_advisories
        )
        duration_ms = round((time.time() - start_time) * 1000, 2)

        return OptimizationResponse(
            shipmentId=request.shipment_id,
            disruptedPort=request.disrupted_port,
            recommendedOptions=options,
            executiveSummary=summary,
            computationTimeMs=duration_ms,
            solverEngine="PYTHON_OR_TOOLS_CPSAT"
        )
    except Exception as e:
        logger.error(f"Solver failure: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Solver failure: {str(e)}")


@app.get("/api/v1/rag/search", response_model=List[SearchResult])
def search_advisories(q: str, top_k: int = 5):
    logger.info(f"RAG search query: '{q}' (top_k={top_k})")
    return vector_store.search_similar(query=q, top_k=top_k)


@app.post("/api/v1/rag/index", response_model=AdvisoryDocument)
def index_advisory(doc: AdvisoryDocument):
    logger.info(f"Indexing maritime advisory: {doc.title} ({doc.affected_port_code})")
    return vector_store.index_advisory(doc)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
