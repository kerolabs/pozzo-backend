Feature: TS06 Pot delivery and closing service
  As a developer
  I want endpoints to record the delivery of the pot, the coverages and the closing of the cycle
  So that the progress of the group is recorded consistently

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" started a group with "Sofia Gonzales" that sends the contributions to Yape

  Scenario: Deliver a complete pot
    Given every member paid the current period
    When "Anna Weber" confirms the delivery of the pot
    Then the service responds 200 OK
    And the period was delivered and turn 2 is open

  Scenario: Deliver the pot of the last turn
    Given every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    And every member paid the current period
    When "Anna Weber" confirms the delivery of the pot
    Then the service responds 200 OK
    And the cycle is "CLOSED"

  Scenario: Deliver the pot before everyone paid
    Given "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    When "Anna Weber" confirms the delivery of the pot
    Then the service responds 422 Unprocessable Entity
    And the error code is "POT_NOT_COMPLETE"

  Scenario: Cover the contribution of a member
    When "Anna Weber" covers the contribution of "Sofia Gonzales"
    Then the service responds 201 Created
    And "Sofia Gonzales" appears as covered in the pot
    And the history of "Sofia Gonzales" counts 1 covered contribution
