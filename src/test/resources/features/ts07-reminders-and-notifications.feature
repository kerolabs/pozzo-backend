Feature: TS07 Reminders and notifications service
  As a developer
  I want the registration of devices and a reminder plan on the server
  So that the notifications are sent even when the application is closed

  Background:
    Given "Anna Weber" has a Pozzo account
    And "Sofia Gonzales" has a Pozzo account

  Scenario: Register a device
    When "Anna Weber" registers a phone with the push token "fcm-token-of-the-phone"
    Then the service responds 201 Created
    And the device is active

  Scenario: A push token moves to the account that registers it
    Given "Anna Weber" registered a phone with the push token "fcm-shared-phone"
    When "Sofia Gonzales" registers a phone with the push token "fcm-shared-phone"
    Then the service responds 201 Created
    And the device is active

  Scenario: Default reminder plan of a group
    Given "Anna Weber" started a group with "Sofia Gonzales"
    When "Sofia Gonzales" checks the reminders of the group
    Then the service responds 200 OK
    And the reminders are sent 3, 1 and 0 days before the cutoff at 9:00

  Scenario Outline: Notices by event
    Given "Anna Weber" started a group with "Sofia Gonzales" that sends the contributions to Yape
    When <event>
    Then "<recipient>" has the notice "<title>"

    Examples:
      | event                                                                                             | recipient      | title                   |
      | "Sofia Gonzales" registers a receipt of S/ 200 paid to "Anna Weber" with the operation "04581273" | Sofia Gonzales | Aporte registrado       |
      | "Sofia Gonzales" registers a receipt of S/ 150 paid to "Anna Weber" with the operation "04581273" | Anna Weber     | Comprobante por revisar |
      | "Anna Weber" covers the contribution of "Sofia Gonzales"                                          | Sofia Gonzales | Aporte cubierto         |

  Scenario: Notice when the group starts
    When "Anna Weber" started a group with "Sofia Gonzales"
    Then "Sofia Gonzales" has the notice "La junta inició"
