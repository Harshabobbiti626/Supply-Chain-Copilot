-- V1__init_schema.sql: Initial Core Supply Chain Relational Schema

CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,
    tier VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS logistics_corridors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    origin_region VARCHAR(100) NOT NULL,
    destination_region VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    corridor_id UUID NOT NULL REFERENCES logistics_corridors(id) ON DELETE RESTRICT,
    tracking_number VARCHAR(100) UNIQUE NOT NULL,
    carrier_code VARCHAR(50) NOT NULL,
    transport_mode VARCHAR(50) NOT NULL, -- MARITIME, ROAD, RAIL, AIR
    current_status VARCHAR(50) NOT NULL, -- IN_TRANSIT, DELAYED, REROUTED, ARRIVED
    current_latitude DECIMAL(9, 6),
    current_longitude DECIMAL(9, 6),
    origin_port VARCHAR(100) NOT NULL,
    destination_port VARCHAR(100) NOT NULL,
    estimated_arrival TIMESTAMP WITH TIME ZONE,
    version INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipment_waypoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    passed BOOLEAN DEFAULT FALSE,
    estimated_arrival TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    max_capacity_teu INT NOT NULL,
    current_occupancy_teu INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    total_quantity INT NOT NULL,
    allocated_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_positive_available CHECK (available_quantity >= 0),
    CONSTRAINT uk_warehouse_sku UNIQUE (warehouse_id, sku)
);

CREATE TABLE IF NOT EXISTS disruption_advisories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(500),
    event_type VARCHAR(100) NOT NULL, -- LABOR_STRIKE, WEATHER_ANOMALY, CANAL_BLOCKAGE, GEOPOLITICAL
    severity_score DECIMAL(3, 2) NOT NULL, -- 0.00 to 1.00
    affected_port_code VARCHAR(20),
    published_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactional_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PUBLISHED, FAILED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_shipments_corridor ON shipments(corridor_id);
CREATE INDEX idx_shipments_status ON shipments(current_status);
CREATE INDEX idx_outbox_pending ON transactional_outbox(status, created_at);

