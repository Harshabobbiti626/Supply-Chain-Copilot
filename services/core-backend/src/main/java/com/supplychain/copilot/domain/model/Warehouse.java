package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "max_capacity_teu", nullable = false)
    private Integer maxCapacityTeu;

    @Column(name = "current_occupancy_teu", nullable = false)
    private Integer currentOccupancyTeu = 0;

    public Warehouse() {}

    public Warehouse(UUID id, String name, String code, String city, String country, Integer maxCapacityTeu, Integer currentOccupancyTeu) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.city = city;
        this.country = country;
        this.maxCapacityTeu = maxCapacityTeu;
        this.currentOccupancyTeu = currentOccupancyTeu != null ? currentOccupancyTeu : 0;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getMaxCapacityTeu() { return maxCapacityTeu; }
    public void setMaxCapacityTeu(Integer maxCapacityTeu) { this.maxCapacityTeu = maxCapacityTeu; }

    public Integer getCurrentOccupancyTeu() { return currentOccupancyTeu; }
    public void setCurrentOccupancyTeu(Integer currentOccupancyTeu) { this.currentOccupancyTeu = currentOccupancyTeu; }

    public static WarehouseBuilder builder() {
        return new WarehouseBuilder();
    }

    public static class WarehouseBuilder {
        private UUID id;
        private String name;
        private String code;
        private String city;
        private String country;
        private Integer maxCapacityTeu;
        private Integer currentOccupancyTeu = 0;

        public WarehouseBuilder id(UUID id) { this.id = id; return this; }
        public WarehouseBuilder name(String name) { this.name = name; return this; }
        public WarehouseBuilder code(String code) { this.code = code; return this; }
        public WarehouseBuilder city(String city) { this.city = city; return this; }
        public WarehouseBuilder country(String country) { this.country = country; return this; }
        public WarehouseBuilder maxCapacityTeu(Integer maxCapacityTeu) { this.maxCapacityTeu = maxCapacityTeu; return this; }
        public WarehouseBuilder currentOccupancyTeu(Integer currentOccupancyTeu) { this.currentOccupancyTeu = currentOccupancyTeu; return this; }

        public Warehouse build() {
            return new Warehouse(id, name, code, city, country, maxCapacityTeu, currentOccupancyTeu);
        }
    }
}
