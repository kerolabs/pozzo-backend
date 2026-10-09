Feature: TS03 Members and invitations service
  As a developer
  I want endpoints to resolve an invitation code, add a member to a group and manage its members
  So that joining works by code, by link and by manual registration

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account

  Scenario: Resolve a valid invitation
    Given "Anna Weber" has a group of 3 seats with an invitation
    When "Sofia Gonzales" opens the invitation
    Then the service responds 200 OK
    And the preview shows the name, the rules and 2 free seats
    And the preview does not show the members or the destination

  Scenario: Resolve an invitation that does not exist
    When "Sofia Gonzales" opens the invitation "ZZ-2222"
    Then the service responds 404 Not Found

  Scenario: Resolve the invitation of a group that already started
    Given "Anna Weber" started a group with "Sofia Gonzales"
    When "Sofia Gonzales" opens the invitation
    Then the service responds 404 Not Found

  Scenario: Join a group with free seats
    Given "Anna Weber" has a group of 3 seats with an invitation
    When "Sofia Gonzales" joins with the invitation
    Then the service responds 200 OK
    And "Sofia Gonzales" is a member of the group

  Scenario: Join a full group
    Given "Jorge Ramos" has a Pozzo account
    And "Anna Weber" has a group of 2 seats where "Sofia Gonzales" joined
    When "Jorge Ramos" joins with the invitation
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_FULL"

  Scenario: Join a group twice
    Given "Anna Weber" has a group of 3 seats where "Sofia Gonzales" joined
    When "Sofia Gonzales" joins with the invitation
    Then the service responds 422 Unprocessable Entity
    And the error code is "ALREADY_A_MEMBER"

  Scenario: Register a member without the application
    Given "Anna Weber" has a group of 3 seats with an invitation
    When "Anna Weber" registers "Marta Quispe" without the application with the number 923456789
    Then the service responds 201 Created
    And "Marta Quispe" is a member of kind "MANUAL"

  Scenario: Remove a member before the group starts
    Given "Anna Weber" has a group of 3 seats where "Sofia Gonzales" joined
    When "Anna Weber" removes "Sofia Gonzales" from the group
    Then the service responds 204 No Content
    And "Sofia Gonzales" is no longer a member of the group

  Scenario: Remove a member after the group started
    Given "Anna Weber" started a group with "Sofia Gonzales"
    When "Anna Weber" removes "Sofia Gonzales" from the group
    Then the service responds 422 Unprocessable Entity
    And the error code is "SAVINGS_GROUP_ALREADY_STARTED"
