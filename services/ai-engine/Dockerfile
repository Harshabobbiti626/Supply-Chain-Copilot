# Multi-stage production build for SupplyChain Copilot AI & Optimization Engine
FROM python:3.11-slim AS builder

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Stage 2: Minimal Runtime
FROM python:3.11-slim
WORKDIR /app

# Run as non-root user
RUN useradd -m -u 1000 aiuser
USER aiuser

COPY --from=builder /root/.local /home/aiuser/.local
COPY --chown=aiuser:aiuser app ./app

ENV PATH=/home/aiuser/.local/bin:$PATH \
    PYTHONUNBUFFERED=1

EXPOSE 8000

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]

