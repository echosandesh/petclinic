
# Should Have Features 


1. Write unit tests for service and endpoint
- Add `WaitingCounterServiceTest` unit tests covering concurrency, increment/decrement bounds, and `getAllCounts()`.
- Add `WaitingCounterControllerTest` using MockMvc to verify JSON response and status.

2. Add integration test for UI update path
- Add a lightweight integration test that mocks service data and verifies controller + fragment endpoint produce expected JSON.

3. Run tests and fix issues
- Execute existing test suite; fix any failures caused by changes. Keep modifications minimal.