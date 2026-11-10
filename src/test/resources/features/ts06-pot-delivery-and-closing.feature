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

  Scenario: Open a new cycle with the same group
    Given every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    And every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    When "Anna Weber" opens a new cycle of the group
    Then the service responds 201 Created
    And the group is in status "DRAFT"
    And "Sofia Gonzales" is a member of the group
    And "Sofia Gonzales" has the notice "Nuevo ciclo"

  Scenario: A new cycle needs the current one to be over
    When "Anna Weber" opens a new cycle of the group
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_NOT_CLOSED"

  Scenario: Deliver the pot before everyone paid
    Given "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"
    When "Anna Weber" confirms the delivery of the pot
    Then the service responds 422 Unprocessable Entity
    And the error code is "POT_NOT_COMPLETE"

  Scenario: Attach the transfer of a delivered pot
    Given every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    When "Anna Weber" attaches the proof of the delivery
    Then the service responds 200 OK
    And "Sofia Gonzales" can see the proof of the delivery

  Scenario: Attach the transfer before delivering the pot
    When "Anna Weber" attaches the proof of the delivery
    Then the service responds 422 Unprocessable Entity
    And the error code is "POT_NOT_DELIVERED"

  Scenario: Only the organizer attaches the transfer of the pot
    Given every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    When "Sofia Gonzales" attaches the proof of the delivery
    Then the service responds 403 Forbidden

  Scenario: Cover the contribution of a member
    When "Anna Weber" covers the contribution of "Sofia Gonzales"
    Then the service responds 201 Created
    And "Sofia Gonzales" appears as covered in the pot
    And the history of "Sofia Gonzales" counts 1 covered contribution

  Scenario: Register that a member dropped out
    When "Anna Weber" registers that "Sofia Gonzales" dropped out
    Then the service responds 200 OK
    And the history of "Sofia Gonzales" counts 1 dropout
    And "Sofia Gonzales" does not have the notice "Un integrante dejó la junta"

  Scenario: Replace a member who dropped out with someone without the application
    Given "Anna Weber" registered that "Sofia Gonzales" dropped out
    When "Anna Weber" replaces the member who dropped out with "Rosa Medina"
    Then the service responds 201 Created
    And "Rosa Medina" owes the contribution of the current period

  Scenario: Replace a member who dropped out with someone who uses Pozzo
    Given "Jorge Ramos" has a Pozzo account
    And "Anna Weber" registered that "Sofia Gonzales" dropped out
    When "Anna Weber" replaces the member who dropped out with the number of "Jorge Ramos"
    Then the service responds 201 Created
    And "Jorge Ramos" is a member of kind "APP"
    And "Jorge Ramos" has the notice "Te sumaron a una junta"
