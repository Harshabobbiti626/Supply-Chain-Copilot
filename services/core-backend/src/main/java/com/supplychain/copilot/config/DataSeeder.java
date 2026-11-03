package com.supplychain.copilot.config;

import com.supplychain.copilot.domain.model.*;
import com.supplychain.copilot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final TenantRepository tenantRepository;
    private final LogisticsCorridorRepository corridorRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;
    private final ShipmentRepository shipmentRepository;
    private final DisruptionAdvisoryRepository advisoryRepository;
    private final PortRepository portRepository;

    public DataSeeder(
            TenantRepository tenantRepository,
            LogisticsCorridorRepository corridorRepository,
            WarehouseRepository warehouseRepository,
            InventoryStockRepository stockRepository,
            ShipmentRepository shipmentRepository,
            DisruptionAdvisoryRepository advisoryRepository,
            PortRepository portRepository) {
        this.tenantRepository = tenantRepository;
        this.corridorRepository = corridorRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
        this.shipmentRepository = shipmentRepository;
        this.advisoryRepository = advisoryRepository;
        this.portRepository = portRepository;
    }

    @Override
    public void run(String... args) {
        if (tenantRepository.count() > 0) {
            return;
        }

        log.info("Seeding realistic enterprise supply-chain dataset...");

        // 1. Tenants
        Tenant gofl = tenantRepository.save(Tenant.builder()
                .name("Global Ocean Freight Logistics (GOFL)")
                .tenantCode("GOFL-CORP")
                .tier("ENTERPRISE")
                .build());

        Tenant dhl3pl = tenantRepository.save(Tenant.builder()
                .name("BlueWater 3PL Logistics")
                .tenantCode("BW-3PL")
                .tier("STANDARD")
                .build());

        // 2. Corridors
        LogisticsCorridor euNorthSea = corridorRepository.save(LogisticsCorridor.builder()
                .tenant(gofl)
                .name("Asia-Europe North Sea Maritime Highway")
                .originRegion("East Asia / Singapore")
                .destinationRegion("North Sea European Gateways")
                .active(true)
                .build());

        LogisticsCorridor transPacific = corridorRepository.save(LogisticsCorridor.builder()
                .tenant(dhl3pl)
                .name("Trans-Pacific US West Coast Corridor")
                .originRegion("Shanghai / Ningbo")
                .destinationRegion("Long Beach / Oakland")
                .active(true)
                .build());

        // 3. Warehouses
        Warehouse rtmWh = warehouseRepository.save(Warehouse.builder()
                .name("Rotterdam Maasvlakte Hub")
                .code("RTM")
                .city("Rotterdam")
                .country("Netherlands")
                .maxCapacityTeu(2500)
                .currentOccupancyTeu(2150)
                .build());

        Warehouse anrWh = warehouseRepository.save(Warehouse.builder()
                .name("Antwerp Gateway Distribution Hub")
                .code("ANR")
                .city("Antwerp")
                .country("Belgium")
                .maxCapacityTeu(3000)
                .currentOccupancyTeu(1750)
                .build());

        Warehouse hamWh = warehouseRepository.save(Warehouse.builder()
                .name("Hamburg Waltershof Terminal")
                .code("HAM")
                .city("Hamburg")
                .country("Germany")
                .maxCapacityTeu(2200)
                .currentOccupancyTeu(1300)
                .build());

        Warehouse zeeWh = warehouseRepository.save(Warehouse.builder()
                .name("Zeebrugge Deepsea Logistics Centre")
                .code("ZEE")
                .city("Zeebrugge")
                .country("Belgium")
                .maxCapacityTeu(1600)
                .currentOccupancyTeu(550)
                .build());

        // 4. Inventory Stock
        stockRepository.save(InventoryStock.builder()
                .warehouse(rtmWh)
                .sku("STANDARD-CONTAINER-TEU")
                .totalQuantity(1000)
                .allocatedQuantity(950)
                .availableQuantity(50)
                .build());

        stockRepository.save(InventoryStock.builder()
                .warehouse(anrWh)
                .sku("STANDARD-CONTAINER-TEU")
                .totalQuantity(1200)
                .allocatedQuantity(600)
                .availableQuantity(600)
                .build());

        stockRepository.save(InventoryStock.builder()
                .warehouse(hamWh)
                .sku("STANDARD-CONTAINER-TEU")
                .totalQuantity(800)
                .allocatedQuantity(350)
                .availableQuantity(450)
                .build());

        stockRepository.save(InventoryStock.builder()
                .warehouse(zeeWh)
                .sku("STANDARD-CONTAINER-TEU")
                .totalQuantity(900)
                .allocatedQuantity(200)
                .availableQuantity(700)
                .build());

        // 5. Active Shipments
        Shipment s1 = shipmentRepository.save(Shipment.builder()
                .corridor(euNorthSea)
                .trackingNumber("MAERSK-GL-7782")
                .carrierCode("MAERSK")
                .transportMode(TransportMode.MARITIME)
                .currentStatus(ShipmentStatus.AT_RISK)
                .currentLatitude(BigDecimal.valueOf(50.4500))
                .currentLongitude(BigDecimal.valueOf(-0.1500))
                .originPort("CNSHA")
                .destinationPort("NLRTM")
                .estimatedArrival(Instant.now().plus(18, ChronoUnit.HOURS))
                .build());

        s1.getWaypoints().addAll(List.of(
                ShipmentWaypoint.builder().shipment(s1).sequenceOrder(1).locationName("Strait of Malacca").latitude(BigDecimal.valueOf(1.43)).longitude(BigDecimal.valueOf(102.80)).passed(true).build(),
                ShipmentWaypoint.builder().shipment(s1).sequenceOrder(2).locationName("Bab el-Mandeb").latitude(BigDecimal.valueOf(12.58)).longitude(BigDecimal.valueOf(43.33)).passed(true).build(),
                ShipmentWaypoint.builder().shipment(s1).sequenceOrder(3).locationName("Suez Canal").latitude(BigDecimal.valueOf(29.97)).longitude(BigDecimal.valueOf(32.55)).passed(true).build(),
                ShipmentWaypoint.builder().shipment(s1).sequenceOrder(4).locationName("English Channel Approaches").latitude(BigDecimal.valueOf(50.45)).longitude(BigDecimal.valueOf(-0.15)).passed(false).build(),
                ShipmentWaypoint.builder().shipment(s1).sequenceOrder(5).locationName("Port of Rotterdam (NLRTM)").latitude(BigDecimal.valueOf(51.95)).longitude(BigDecimal.valueOf(4.14)).passed(false).build()
        ));
        shipmentRepository.save(s1);

        Shipment s2 = shipmentRepository.save(Shipment.builder()
                .corridor(euNorthSea)
                .trackingNumber("MSC-TITAN-9011")
                .carrierCode("MSC")
                .transportMode(TransportMode.MARITIME)
                .currentStatus(ShipmentStatus.IN_TRANSIT)
                .currentLatitude(BigDecimal.valueOf(48.2000))
                .currentLongitude(BigDecimal.valueOf(-5.5000))
                .originPort("SGSIN")
                .destinationPort("BEANR")
                .estimatedArrival(Instant.now().plus(32, ChronoUnit.HOURS))
                .build());

        Shipment s3 = shipmentRepository.save(Shipment.builder()
                .corridor(euNorthSea)
                .trackingNumber("CMA-APOLLO-5541")
                .carrierCode("CMA_CGM")
                .transportMode(TransportMode.MARITIME)
                .currentStatus(ShipmentStatus.IN_TRANSIT)
                .currentLatitude(BigDecimal.valueOf(53.2000))
                .currentLongitude(BigDecimal.valueOf(4.8000))
                .originPort("CNNGB")
                .destinationPort("DEHAM")
                .estimatedArrival(Instant.now().plus(12, ChronoUnit.HOURS))
                .build());

        // 6. Disruption Advisory
        advisoryRepository.save(DisruptionAdvisory.builder()
                .title("Port of Rotterdam (NLRTM) Crane Operator Strike – 48 Hour Full Gate Halt")
                .content("Labor union strikes at ECT Delta and Euromax container terminals have suspended all offloading operations indefinitely. Vessels with ETA within 48h are instructed to divert to Antwerp or Hamburg.")
                .sourceUrl("https://www.maritime-executive.com/article/rotterdam-port-strike-disruption")
                .eventType("LABOR_STRIKE")
                .severityScore(BigDecimal.valueOf(0.88))
                .affectedPortCode("NLRTM")
                .build());

        advisoryRepository.save(DisruptionAdvisory.builder()
                .title("Severe North Sea Gale Storm Warning (Force 10) – Dover Strait to Elbe")
                .content("Blowing easterly gale with wave heights exceeding 6.5 meters forecasted over southern North Sea. Coastal container vessels advised to reduce speed to 8 knots.")
                .sourceUrl("https://weather.noaa.gov/maritime/northsea-warning")
                .eventType("WEATHER_ANOMALY")
                .severityScore(BigDecimal.valueOf(0.72))
                .affectedPortCode("NLRTM")
                .build());

        // 7. Maritime Ports Network
        if (portRepository.count() == 0) {
            portRepository.save(Port.builder()
                    .code("NLRTM")
                    .name("Port of Rotterdam Maasvlakte")
                    .city("Rotterdam")
                    .country("Netherlands")
                    .latitude(BigDecimal.valueOf(51.950000))
                    .longitude(BigDecimal.valueOf(4.140000))
                    .draftDepthMeters(BigDecimal.valueOf(24.0))
                    .maxCapacityTeu(3000)
                    .availableCapacityTeu(800)
                    .costPerTeu(BigDecimal.valueOf(4.50))
                    .transitDelayHours(0)
                    .active(true)
                    .build());

            portRepository.save(Port.builder()
                    .code("BEANR")
                    .name("Port of Antwerp-Bruges")
                    .city("Antwerp")
                    .country("Belgium")
                    .latitude(BigDecimal.valueOf(51.272100))
                    .longitude(BigDecimal.valueOf(4.316400))
                    .draftDepthMeters(BigDecimal.valueOf(16.0))
                    .maxCapacityTeu(2500)
                    .availableCapacityTeu(600)
                    .costPerTeu(BigDecimal.valueOf(7.11))
                    .transitDelayHours(14)
                    .active(true)
                    .build());

            portRepository.save(Port.builder()
                    .code("DEHAM")
                    .name("Port of Hamburg (CTA / CTB)")
                    .city("Hamburg")
                    .country("Germany")
                    .latitude(BigDecimal.valueOf(53.530000))
                    .longitude(BigDecimal.valueOf(9.920000))
                    .draftDepthMeters(BigDecimal.valueOf(14.5))
                    .maxCapacityTeu(2000)
                    .availableCapacityTeu(450)
                    .costPerTeu(BigDecimal.valueOf(12.88))
                    .transitDelayHours(26)
                    .active(true)
                    .build());

            portRepository.save(Port.builder()
                    .code("BEZEE")
                    .name("Port of Zeebrugge Coastal Hub")
                    .city("Zeebrugge")
                    .country("Belgium")
                    .latitude(BigDecimal.valueOf(51.357000))
                    .longitude(BigDecimal.valueOf(3.218000))
                    .draftDepthMeters(BigDecimal.valueOf(17.5))
                    .maxCapacityTeu(1800)
                    .availableCapacityTeu(700)
                    .costPerTeu(BigDecimal.valueOf(6.00))
                    .transitDelayHours(8)
                    .active(true)
                    .build());
        }

        log.info("SupplyChain Copilot initial data seeded successfully!");
    }
}

