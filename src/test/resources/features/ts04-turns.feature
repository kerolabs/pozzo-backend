Feature: TS04 Turns service
  As a developer
  I want endpoints to assign the turns by draw or by an agreed order
  So that the mobile application supports the ways a group decides who collects first

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account
    And "Jorge Ramos" has a Pozzo account
    And "Anna Weber" has a group of 3 seats where "Sofia Gonzales" and "Jorge Ramos" joined

  Scenario: Assign the turns by draw
    When "Anna Weber" draws the turns
    Then the service responds 200 OK
    And every member has one turn from 1 to 3 with its cutoff date
    And the calendar shows the seed of the draw

  Scenario: Assign the turns in an agreed order
    When "Anna Weber" sets the order "Jorge Ramos", "Anna Weber", "Sofia Gonzales"
    Then the service responds 200 OK
    And the turns follow the order "Jorge Ramos", "Anna Weber", "Sofia Gonzales"

  Scenario: The turns need every seat taken
    Given "Anna Weber" removed "Jorge Ramos" from the group
    When "Anna Weber" draws the turns
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_NOT_FULL"

  Scenario: Swap two turns whose periods have not started
    Given "Anna Weber" sets the order "Anna Weber", "Sofia Gonzales", "Jorge Ramos"
    And "Anna Weber" starts the group
    When "Anna Weber" swaps the turns of "Sofia Gonzales" and "Jorge Ramos"
    Then the service responds 200 OK
    And the calendar of the group follows the order "Anna Weber", "Jorge Ramos", "Sofia Gonzales"
    And "Sofia Gonzales" has the notice "Cambió tu turno"

  Scenario: A turn whose period started cannot be swapped
    Given "Anna Weber" sets the order "Anna Weber", "Sofia Gonzales", "Jorge Ramos"
    And "Anna Weber" starts the group
    When "Anna Weber" swaps the turns of "Anna Weber" and "Jorge Ramos"
    Then the service responds 422 Unprocessable Entity
    And the error code is "TURN_ALREADY_STARTED"

  Scenario: Only the organizer swaps turns
    Given "Anna Weber" sets the order "Anna Weber", "Sofia Gonzales", "Jorge Ramos"
    And "Anna Weber" starts the group
    When "Sofia Gonzales" swaps the turns of "Sofia Gonzales" and "Jorge Ramos"
    Then the service responds 403 Forbidden
