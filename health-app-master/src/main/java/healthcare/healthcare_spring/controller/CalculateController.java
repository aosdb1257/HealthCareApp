package healthcare.healthcare_spring.controller;

import healthcare.healthcare_spring.domain.Calculate;
import healthcare.healthcare_spring.dto.request.CalculateRequest;
import healthcare.healthcare_spring.service.CalculateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CalculateController {
    private final CalculateService calculateService;

    public CalculateController(CalculateService calculateService) {
        this.calculateService = calculateService;
    }

    @PostMapping("/calculate")
    public void saveCalculate(@RequestBody CalculateRequest request) {
        calculateService.saveCalculate(request);
    }
    @GetMapping("/calculate2")
    public List<Calculate> getAllCalculates() {
        return calculateService.getAllCalculates();
    }
}
