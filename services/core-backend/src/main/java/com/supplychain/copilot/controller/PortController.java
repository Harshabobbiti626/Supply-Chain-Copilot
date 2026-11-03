package com.supplychain.copilot.controller;

import com.supplychain.copilot.domain.model.Port;
import com.supplychain.copilot.service.PortService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ports")
@CrossOrigin(origins = "*")
public class PortController {

    private final PortService portService;

    public PortController(PortService portService) {
        this.portService = portService;
    }

    @GetMapping
    public ResponseEntity<List<Port>> getAllPorts() {
        return ResponseEntity.ok(portService.getAllActivePorts());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Port> getPortByCode(@PathVariable String code) {
        return portService.getPortByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

