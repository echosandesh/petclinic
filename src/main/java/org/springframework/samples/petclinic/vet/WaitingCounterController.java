package org.springframework.samples.petclinic.vet;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waiting")
class WaitingCounterController {

	private final WaitingCounterService waitingCounterService;

	WaitingCounterController(WaitingCounterService waitingCounterService) {
		this.waitingCounterService = waitingCounterService;
	}

	@GetMapping
	Map<Integer, Integer> getAllCounts() {
		return waitingCounterService.getAllCounts();
	}

}
