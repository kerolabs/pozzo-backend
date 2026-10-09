Feature: TS08 Compliance history service
  As a developer
  I want an endpoint that computes the compliance history of a member from their contributions
  So that the application shows it and shares it in a verifiable way

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account
    And "Anna Weber" started a group with "Sofia Gonzales" that sends the contributions to Yape
    And "Sofia Gonzales" registered a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273"

  Scenario: Own history
    When "Sofia Gonzales" checks the compliance history
    Then the service responds 200 OK
    And the history has the level "GOOD", 100 % compliance and 1 contribution on time
    And the history has the detail of the group

  Scenario: History of a member of my group
    When "Anna Weber" checks the summary of "Sofia Gonzales"
    Then the service responds 200 OK
    And the summary has 1 contribution on time

  Scenario: Compliance of every member of my group
    When "Anna Weber" checks the compliance of the group
    Then the service responds 200 OK
    And the compliance lists "Anna Weber" and "Sofia Gonzales"

  Scenario: History of someone outside my groups
    Given "Carla Vega" has a Pozzo account
    When "Carla Vega" checks the summary of "Sofia Gonzales"
    Then the service responds 404 Not Found

  Scenario: Open a shared history without signing in
    Given "Sofia Gonzales" shared the history
    When anyone opens the shared link without signing in
    Then the service responds 200 OK
    And the shared history shows "Sofia Gonzales" and the summary without amounts or group names

  Scenario: Open a revoked link
    Given "Sofia Gonzales" shared the history
    And "Sofia Gonzales" revoked the link
    When anyone opens the shared link without signing in
    Then the service responds 404 Not Found
