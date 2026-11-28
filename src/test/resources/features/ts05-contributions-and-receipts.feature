Feature: TS05 Contributions and receipts service
  As a developer
  I want endpoints to register a contribution with its receipt, validate it, review it and register cash
  So that the state of the pot is computed on the server

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" started a group with "Sofia Gonzales" that sends the contributions to Yape

  Scenario: Register a receipt that matches
    When "Sofia Gonzales" registers a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    Then the service responds 201 Created
    And the contribution is in status "VALIDATED" without inconsistencies

  Scenario Outline: Register a receipt that does not match
    When "Sofia Gonzales" registers a receipt of S/ <amount> paid to "<payee>" with the operation "04581273"
    Then the service responds 201 Created
    And the contribution is in status "INCONSISTENT"
    And the field "<field>" did not match

    Examples:
      | amount | payee      | field  |
      | 150    | Anna Weber | AMOUNT |
      | 200    | Carla Vega | PAYEE  |

  Scenario: Register a receipt already used in the group
    Given "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    When "Anna Weber" registers a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    Then the service responds 409 Conflict
    And the error code is "RECEIPT_CONFLICT"

  Scenario: Approve a contribution that did not match
    Given "Sofia Gonzales" registered a receipt of S/ 150 paid to "Anna Weber" with the operation "04581273"
    When "Anna Weber" approves the contribution of "Sofia Gonzales"
    Then the service responds 200 OK
    And the contribution is in status "APPROVED"
    And "Sofia Gonzales" appears as paid in the pot

  Scenario: Reject a contribution that did not match
    Given "Sofia Gonzales" registered a receipt of S/ 150 paid to "Anna Weber" with the operation "04581273"
    When "Anna Weber" rejects the contribution of "Sofia Gonzales"
    Then the service responds 200 OK
    And the contribution is in status "REJECTED"
    And "Sofia Gonzales" can register a receipt of S/ 200 paid to "Anna Weber" with the operation "05230918"

  Scenario: Take back the approval of a contribution
    Given "Sofia Gonzales" registered a receipt of S/ 150 paid to "Anna Weber" with the operation "04581273"
    And "Anna Weber" approved the contribution of "Sofia Gonzales"
    When "Anna Weber" reviews the contribution of "Sofia Gonzales" again
    Then the service responds 200 OK
    And the contribution is in status "INCONSISTENT"
    And "Sofia Gonzales" no longer appears as paid in the pot
    And the organizer sees no contribution reviewed in the period

  Scenario: A receipt corrected by hand goes to review
    When "Sofia Gonzales" registers a receipt of S/ 200 paid to "Anna Weber" that the application read as S/ 150
    Then the service responds 201 Created
    And the contribution is in status "INCONSISTENT"

  Scenario: Register a cash contribution
    When "Anna Weber" registers S/ 200 in cash for "Sofia Gonzales"
    Then the service responds 201 Created
    And the contribution has the method "CASH" and the status "VALIDATED"

  Scenario: A different cash amount needs the organizer to confirm it
    When "Anna Weber" registers S/ 150 in cash for "Sofia Gonzales"
    Then the service responds 422 Unprocessable Entity
    And the error code is "AMOUNT_MISMATCH"

  Scenario: Register a different cash amount the organizer confirmed
    When "Anna Weber" registers S/ 150 in cash for "Sofia Gonzales" confirming the difference
    Then the service responds 201 Created
    And the contribution has the method "CASH" and the status "VALIDATED"
    And the contribution has the amount S/ 150

  Scenario: State of the period
    Given "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    When "Sofia Gonzales" checks the state of the pot
    Then the service responds 200 OK
    And the pot has S/ 200 collected of S/ 400 and S/ 200 missing
    And the pot shows who collects, the days to the cutoff and the state of each member

  Scenario: Share a contribution with a link anyone can verify
    Given "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    When "Sofia Gonzales" shares the contribution
    Then the service responds 200 OK
    And anyone who opens the link sees the contribution of "Sofia Gonzales" of S/ 200 with the operation ending "1273"

  Scenario: A contribution under review cannot be shared
    Given "Sofia Gonzales" registered a receipt of S/ 150 paid to "Anna Weber" with the operation "04581273"
    When "Sofia Gonzales" shares the contribution
    Then the service responds 422 Unprocessable Entity
    And the error code is "CONTRIBUTION_NOT_SETTLED"
