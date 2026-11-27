Feature: TS02 Savings groups service
  As a developer
  I want endpoints to create, read, update and start savings groups
  So that the mobile application manages the life cycle of each group

  Background:
    Given "Anna Weber" has a Pozzo account

  Scenario: Create a savings group
    When "Anna Weber" creates a monthly group "Junta de la familia Weber" of S/ 200 with 4 seats and Yape as destination
    Then the service responds 201 Created
    And the group is in status "DRAFT"
    And "Anna Weber" is its organizer

  Scenario Outline: The number of seats goes from 2 to 50
    When "Anna Weber" creates a monthly group "Junta de la familia Weber" of S/ 200 with <seats> seats and Yape as destination
    Then the service responds 400 Bad Request
    And the error code is "VALIDATION_ERROR"

    Examples:
      | seats |
      | 1     |
      | 51    |

  Scenario: The free plan allows up to six members
    When "Anna Weber" creates a group on the free plan with 8 seats
    Then the service responds 422 Unprocessable Entity
    And the error code is "FREE_PLAN_SEATS_EXCEEDED"

  Scenario: The free plan covers one active group per organizer
    Given "Anna Weber" has a group on the free plan
    When "Anna Weber" creates a group on the free plan with 4 seats
    Then the service responds 422 Unprocessable Entity
    And the error code is "FREE_PLAN_IN_USE"

  Scenario: Activate the paid plan of a group
    Given "Anna Weber" has a group on the free plan
    When "Anna Weber" activates the paid plan of the group
    Then the service responds 200 OK
    And the group is on the "PAID" plan

  Scenario: List the groups of the member
    Given "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" started a group with "Sofia Gonzales"
    When "Sofia Gonzales" lists the groups
    Then the service responds 200 OK
    And the list has the group with the role "PARTICIPANT" and a turn

  Scenario: Start a group that is ready
    Given "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" has a group of 2 seats where "Sofia Gonzales" joined
    And "Anna Weber" drew the turns
    When "Anna Weber" starts the group
    Then the service responds 200 OK
    And the group is in status "STARTED"
    And the cycle of the group is open on turn 1

  Scenario: Start a group that is not ready
    Given "Anna Weber" has a group of 2 seats
    When "Anna Weber" starts the group
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_NOT_READY"

  Scenario: The rules of a started group cannot change
    Given "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" started a group with "Sofia Gonzales"
    When "Anna Weber" changes the contribution of the group to S/ 300
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_ALREADY_STARTED"
