# Errors and Learnings

## Spring Java Format

**Issue:** `mvn test` failed with formatting violations in new Java files.

**Fix:** Run `.\mvnw.cmd spring-javaformat:apply` before committing.

## I18nPropertiesSyncTest

**Issue:** Hardcoded strings in `waitingCounter.html` ("Waiting:", "Loading...") and `ownerDetails.html` ("Complete") caused `I18nPropertiesSyncTest.checkNonInternationalizedStrings` to fail.

**Fix:** Use `th:text="#{key}"` in templates and add keys (`waiting`, `waitingLoading`, `completeVisit`) to all `messages*.properties` locale files.

## VisitController unit tests

**Issue:** `@WebMvcTest(VisitController.class)` failed to load after adding `VetRepository` and `WaitingCounterService` dependencies.

**Fix:** Add `@MockitoBean` for both dependencies in `VisitControllerTests`. Guard `checkIn` with `visit.getId() != null` because mocked saves do not assign JPA ids.

## Visit-to-vet mapping

**Learning:** PetClinic visits have no vet field. Minimal integration uses round-robin vet assignment on booking and an in-memory `visitId → vetId` map in `WaitingCounterService` for check-out.
