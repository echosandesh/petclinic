# Live Vet Waiting Counter — Config and Deployment Notes

## Overview

In-memory waiting counts per veterinarian. Counts increment when a visit is booked and decrement when a visit is completed. No database persistence.

## Enable / Disable

The feature is always on when the application starts. There is no feature flag or `application.properties` toggle.

## Configuration

| Setting | Location | Default | Notes |
|---------|----------|---------|-------|
| Poll interval | `templates/fragments/waitingCounter.html` (`intervalMs`) | 5000 ms | Change in the fragment JS to adjust UI refresh rate |
| Vet assignment | `WaitingCounterService.pickVetId()` | Round-robin | Visits are assigned to vets in rotation; no vet field on the visit form |
| Storage | `WaitingCounterService` | In-memory `ConcurrentHashMap` | Resets on app restart |

## API

- `GET /api/waiting` — JSON map of `{ vetId: count }`
- `POST /owners/{ownerId}/pets/{petId}/visits/{visitId}/complete` — check-out (decrement)

## Deployment Notes

- **Single instance only:** Counts live in JVM memory. Multiple replicas will show different counts unless a shared store is added later.
- **Restart:** All waiting counts and visit-to-vet assignments are lost on restart.
- **Performance:** Lightweight; one in-memory map read per poll per page with the fragment. At default 5s polling on welcome and vets pages, load is negligible for typical traffic.
- **Security:** `/api/waiting` is unauthenticated (same as other PetClinic endpoints). Restrict at the reverse proxy if needed.

## UI

The waiting counter fragment is included on:

- `/` (welcome)
- `/vets.html` (veterinarians list)

Check-out uses the **Complete** button on the owner details page for each visit.

## Future Enhancements

- Persistent store (Redis/DB) for multi-instance deployments
- Configurable poll interval via `application.properties`
- Explicit vet selection on visit booking
