Feature: TS01 SMS authentication service
  As a developer
  I want endpoints to request and verify SMS codes and to issue session tokens
  So that the mobile application signs members in without a password

  Scenario: Request a verification code
    Given a phone number without an account
    When the client requests a verification code for the number
    Then the service responds 202 Accepted
    And an SMS with a six-digit code reaches the number
    And the code expires 10 minutes after the request
    And another code can be requested 30 seconds after the request

  Scenario: Verify the code of a number without an account
    Given a phone number without an account received a verification code
    When the client verifies the number with the code it received
    Then the service responds 200 OK
    And the response asks to complete the registration with a registration token

  Scenario: Complete the registration
    Given a phone number without an account was verified
    When the client completes the registration as "Anna Weber" accepting the terms
    Then the service responds 201 Created
    And the response contains a session token for "Anna Weber"

  Scenario: Verify the code of a number with an account
    Given "Anna Weber" has a Pozzo account
    And 30 seconds have passed
    And "Anna Weber" received a new verification code
    When the client verifies the number of "Anna Weber" with the code it received
    Then the service responds 200 OK
    And the response contains a session token

  Scenario: A wrong code
    Given a phone number without an account received a verification code
    When the client verifies the number with the code "000000"
    Then the service responds 401 Unauthorized
    And the error code is "INVALID_VERIFICATION_CODE"
    And the error says "2 attempts left"

  Scenario: The third wrong code blocks the code
    Given a phone number without an account received a verification code
    And the client verified the number with the code "000000" 2 times
    When the client verifies the number with the code "000000"
    Then the service responds 401 Unauthorized
    And the error code is "BLOCKED_VERIFICATION_CODE"

  Scenario: An expired code
    Given a phone number without an account received a verification code
    And 10 minutes have passed
    When the client verifies the number with the code it received
    Then the service responds 401 Unauthorized
    And the error code is "EXPIRED_VERIFICATION_CODE"

  Scenario: Another code requested too soon
    Given a phone number without an account received a verification code
    When the client requests a verification code for the number
    Then the service responds 429 Too Many Requests
    And the error code is "VERIFICATION_CODE_RESEND_TOO_SOON"
