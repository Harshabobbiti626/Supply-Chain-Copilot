package com.supplychain.copilot.controller;

import com.supplychain.copilot.domain.model.DisruptionAdvisory;
import com.supplychain.copilot.service.DisruptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/disruptions")
@CrossOrigin(origins = "*")
public class DisruptionController {

    private final DisruptionService disruptionService;

    public DisruptionController(DisruptionService disruptionService) {
        this.disruptionService = disruptionService;
    }

    @GetMapping
    public ResponseEntity<List<DisruptionAdvisory>> getDisruptions() {
        return ResponseEntity.ok(disruptionService.getLatestAdvisories());
    }

    @PostMapping
    public ResponseEntity<DisruptionAdvisory> reportDisruption(@RequestBody DisruptionAdvisory advisory) {
        return ResponseEntity.ok(disruptionService.recordAdvisory(advisory));
    }
}

