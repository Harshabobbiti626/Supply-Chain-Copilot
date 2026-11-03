-- V2__add_ports_table.sql: Maritime Ports and Operational Terminal Nodes

CREATE TABLE IF NOT EXISTS ports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    draft_depth_meters DECIMAL(4, 1) NOT NULL DEFAULT 15.0,
    max_capacity_teu INT NOT NULL DEFAULT 1000,
    available_capacity_teu INT NOT NULL DEFAULT 500,
    cost_per_teu DECIMAL(7, 2) NOT NULL DEFAULT 5.00,
    transit_delay_hours INT NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ports_code ON ports(code);
CREATE INDEX IF NOT EXISTS idx_ports_active ON ports(active);

-- Seed initial European Hub Network into ports table
INSERT INTO ports (code, name, city, country, latitude, longitude, draft_depth_meters, max_capacity_teu, available_capacity_teu, cost_per_teu, transit_delay_hours, active)
VALUES 
    ('NLRTM', 'Port of Rotterdam Maasvlakte', 'Rotterdam', 'Netherlands', 51.950000, 4.140000, 24.0, 3000, 800, 4.50, 0, TRUE),
    ('BEANR', 'Port of Antwerp-Bruges', 'Antwerp', 'Belgium', 51.272100, 4.316400, 16.0, 2500, 600, 7.11, 14, TRUE),
    ('DEHAM', 'Port of Hamburg (CTA / CTB)', 'Hamburg', 'Germany', 53.530000, 9.920000, 14.5, 2000, 450, 12.88, 26, TRUE),
    ('BEZEE', 'Port of Zeebrugge Coastal Hub', 'Zeebrugge', 'Belgium', 51.357000, 3.218000, 17.5, 1800, 700, 6.00, 8, TRUE)
ON CONFLICT (code) DO NOTHING;

