package com.supplychain.copilot.service;

import com.supplychain.copilot.domain.model.Port;
import com.supplychain.copilot.repository.PortRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PortService {

    private final PortRepository portRepository;

    public PortService(PortRepository portRepository) {
        this.portRepository = portRepository;
    }

    public List<Port> getAllActivePorts() {
        return portRepository.findByActiveTrue();
    }

    public Optional<Port> getPortByCode(String code) {
        return portRepository.findByCodeIgnoreCase(code);
    }

    @Transactional
    public Port savePort(Port port) {
        return portRepository.save(port);
    }
}

