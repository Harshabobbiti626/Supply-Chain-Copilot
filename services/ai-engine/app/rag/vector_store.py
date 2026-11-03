"""
Maritime Advisory Vector Embedding and Similarity Search Store.
Supports in-memory cosine ranking and PostgreSQL pgvector integration.
"""

from typing import List, Dict, Any, Optional
from pydantic import BaseModel
import math
import re

class AdvisoryDocument(BaseModel):
    id: str
    title: str
    content: str
    source_url: Optional[str] = None
    event_type: str # LABOR_STRIKE, WEATHER_ANOMALY, CANAL_BLOCKAGE, GEOPOLITICAL
    severity_score: float
    affected_port_code: Optional[str] = None
    embedding: Optional[List[float]] = None


class SearchResult(BaseModel):
    document: AdvisoryDocument
    similarity_score: float


class AdvisoryVectorStore:
    def __init__(self):
        self.documents: Dict[str, AdvisoryDocument] = {}
        self._initialize_seed_advisories()

    def _simple_embedding(self, text: str) -> List[float]:
        """
        Deterministic term frequency hash embedding for zero-dependency local operation.
        Produces normalized 64-dimensional dense vectors.
        """
        words = re.findall(r'\b[a-z]{3,}\b', text.lower())
        vec = [0.0] * 64
        for w in words:
            idx = sum(ord(c) for c in w) % 64
            vec[idx] += 1.0

        magnitude = math.sqrt(sum(x * x for x in vec))
        if magnitude > 0:
            vec = [x / magnitude for x in vec]
        return vec

    def _cosine_similarity(self, v1: List[float], v2: List[float]) -> float:
        if not v1 or not v2 or len(v1) != len(v2):
            return 0.0
        return sum(a * b for a, b in zip(v1, v2))

    def index_advisory(self, doc: AdvisoryDocument) -> AdvisoryDocument:
        full_text = f"{doc.title} {doc.content} {doc.event_type} {doc.affected_port_code or ''}"
        doc.embedding = self._simple_embedding(full_text)
        self.documents[doc.id] = doc
        return doc

    def search_similar(self, query: str, top_k: int = 5) -> List[SearchResult]:
        q_vec = self._simple_embedding(query)
        scored = []
        for doc in self.documents.values():
            sim = self._cosine_similarity(q_vec, doc.embedding or [])
            scored.append(SearchResult(document=doc, similarity_score=round(sim, 4)))

        scored.sort(key=lambda x: x.similarity_score, reverse=True)
        return scored[:top_k]

    def _initialize_seed_advisories(self):
        seeds = [
            AdvisoryDocument(
                id="ADV-001",
                title="Port of Rotterdam (NLRTM) Crane Operator Strike – 48 Hour Full Gate Halt",
                content="ECT Delta and Euromax container terminals shut down operations due to an unannounced labor strike. Vessels with ETA within 48h instructed to divert to Antwerp or Hamburg.",
                source_url="https://www.maritime-executive.com/article/rotterdam-port-strike-disruption",
                event_type="LABOR_STRIKE",
                severity_score=0.88,
                affected_port_code="NLRTM"
            ),
            AdvisoryDocument(
                id="ADV-002",
                title="Severe North Sea Gale Storm Warning (Force 10) – Dover Strait to Elbe",
                content="Blowing easterly gale with wave heights exceeding 6.5 meters forecasted over southern North Sea. Coastal container vessels advised to reduce speed to 8 knots.",
                source_url="https://weather.noaa.gov/maritime/northsea-warning",
                event_type="WEATHER_ANOMALY",
                severity_score=0.72,
                affected_port_code="NLRTM"
            ),
            AdvisoryDocument(
                id="ADV-003",
                title="Red Sea & Bab el-Mandeb Security Advisory – Cape of Good Hope Diversions",
                content="Heightened maritime missile threats in the southern Red Sea. Commercial carriers rerouting Asia-Europe loops around southern Africa, adding 10-14 transit days.",
                source_url="https://ukmto.org/advisories/red-sea-transit",
                event_type="GEOPOLITICAL",
                severity_score=0.95,
                affected_port_code="EGSCA"
            )
        ]
        for s in seeds:
            self.index_advisory(s)


vector_store = AdvisoryVectorStore()

