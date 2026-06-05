package org.springframework.samples.petclinic.vet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waiting")
class WaitingCounterController {

	private final WaitingCounterService waitingCounterService;

	private final VetRepository vetRepository;

	WaitingCounterController(WaitingCounterService waitingCounterService, VetRepository vetRepository) {
		this.waitingCounterService = waitingCounterService;
		this.vetRepository = vetRepository;
	}

	@GetMapping
	Map<Integer, Integer> getAllCounts() {
		Map<Integer, Integer> counts = new ConcurrentHashMap<>(waitingCounterService.getAllCounts());
		vetRepository.findAll().forEach(vet -> counts.putIfAbsent(vet.getId(), 0));
		return counts;
	}

}