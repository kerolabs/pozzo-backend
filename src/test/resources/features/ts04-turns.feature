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

  Scenario: Auction the turns
    When "Anna Weber" auctions the turns with a minimum bid of S/ 20 and a draw when nobody bids
    Then the service responds 200 OK
    And the group is in status "READY"

  Scenario: The highest bid collects the turn
    Given "Anna Weber" auctions the turns with a minimum bid of S/ 20 and a draw when nobody bids
    And "Anna Weber" starts the group
    And "Sofia Gonzales" bids S/ 50 for the turn
    And "Jorge Ramos" bids S/ 30 for the turn
    When "Anna Weber" closes the auction of the turn
    Then the service responds 200 OK
    And "Sofia Gonzales" wins the turn by "BID" and receives S/ 550
    And "Jorge Ramos" has the notice "Subasta cerrada"

  Scenario: The members still to collect owe less in the next period
    Given "Anna Weber" auctions the turns with a minimum bid of S/ 20 and a draw when nobody bids
    And "Anna Weber" starts the group
    And "Sofia Gonzales" bids S/ 50 for the turn
    And "Anna Weber" closes the auction of the turn
    And every member paid the current period
    And "Anna Weber" confirmed the delivery of the pot
    Then "Jorge Ramos" owes S/ 175 in the current period
    And "Anna Weber" owes S/ 175 in the current period
    And "Sofia Gonzales" owes S/ 200 in the current period

  Scenario: A bid below the minimum
    Given "Anna Weber" auctions the turns with a minimum bid of S/ 20 and a draw when nobody bids
    And "Anna Weber" starts the group
    When "Sofia Gonzales" bids S/ 10 for the turn
    Then the service responds 422 Unprocessable Entity
    And the error code is "BID_BELOW_MINIMUM"

  Scenario: Without bids the turn goes by draw without discount
    Given "Anna Weber" auctions the turns with a minimum bid of S/ 20 and a draw when nobody bids
    And "Anna Weber" starts the group
    When "Anna Weber" closes the auction of the turn
    Then the service responds 200 OK
    And the turn went by "DRAW" and its member receives S/ 600
