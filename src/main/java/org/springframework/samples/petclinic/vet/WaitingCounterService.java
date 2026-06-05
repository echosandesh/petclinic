package org.springframework.samples.petclinic.vet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class WaitingCounterService {

	private final Map<Integer, AtomicInteger> counts = new ConcurrentHashMap<>();

	private final Map<Integer, Integer> visitVets = new ConcurrentHashMap<>();

	private final AtomicInteger nextVetIndex = new AtomicInteger(0);

	public void increment(int vetId) {
		counts.computeIfAbsent(vetId, id -> new AtomicInteger(0)).incrementAndGet();
	}

	public void decrement(int vetId) {
		AtomicInteger count = counts.computeIfAbsent(vetId, id -> new AtomicInteger(0));
		count.updateAndGet(value -> Math.max(0, value - 1));
	}

	public int getCount(int vetId) {
		AtomicInteger count = counts.get(vetId);
		return count == null ? 0 : count.get();
	}

	public Map<Integer, Integer> getAllCounts() {
		Map<Integer, Integer> snapshot = new ConcurrentHashMap<>();
		counts.forEach((vetId, count) -> snapshot.put(vetId, count.get()));
		return Collections.unmodifiableMap(snapshot);
	}

	public void checkIn(int visitId, int vetId) {
		increment(vetId);
		visitVets.put(visitId, vetId);
	}

	public void checkOut(int visitId) {
		Integer vetId = visitVets.remove(visitId);
		if (vetId != null) {
			decrement(vetId);
		}
	}

	public int pickVetId(List<Integer> vetIds) {
		if (vetIds.isEmpty()) {
			return 1;
		}
		return vetIds.get(Math.floorMod(nextVetIndex.getAndIncrement(), vetIds.size()));
	}

}
