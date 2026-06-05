Implementation Plan: Live Vet Waiting Counter 

# Must Have Features 

1. Add service to track waiting counts
- Create `WaitingCounterService` in `org.springframework...` that maintains in-memory counts per vet (thread-safe).
- Methods: `increment(vetId)`, `decrement(vetId)`, `getCount(vetId)`, `getAllCounts()`.

2. Expose REST endpoint for current counts
- Add controller `WaitingCounterController` with `GET /api/waiting` returning all counts as JSON.

3. Integrate counter updates into vet workflow
- Identify vet-checkin and check-out points (appointment start/end or visit create/complete).
- Call `increment` on check-in and `decrement` on check-out; keep changes localized to service layer or existing controller handling visits.

4. Add UI fragment for live waiting display
- Create a small Thymeleaf fragment `fragments/waitingCounter.html` that calls `/api/waiting` periodically via JS (fetch every 5s) and updates displayed counts.
- Include fragment in relevant templates (e.g., `vets` or `welcome`) with minimal markup.


6. Document config and deployment notes
- Add short notes in this file describing any new config (e.g., cache size, refresh interval) and where to enable the feature and update in `\features\live-vet-wating-counter\config and deployment notes.md`

Scope and constraints
- Keep changes minimal and localized to new service, controller, a small template fragment, and tests.
- Default to in-memory counts; flag for persistent store left as future enhancement.
