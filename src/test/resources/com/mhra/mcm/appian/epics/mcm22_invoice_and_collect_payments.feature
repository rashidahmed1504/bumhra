Feature: Each notification will have a fee associated with it. No processing of the notification will take place unless the payment has been completed.

  @mcm-22 @mcm-36 @e2e @mcm-38 @mcm-109 @mcm-97 @mcm-68 @mcm-101
  Scenario Outline: Create an invoice processing of different types of notification
    Given I am logged into appian as "<user>" user
    And I create new notification with following data
      | type       | <type>       |
      | ingredient | <ingredient> |
    And I attach a toxicology report for "<ingredient>"
    When I login as "fin1" and generate a standard invoice
    Then I should receive an invoice email with heading "Uninvoiced Notifications" from appian in next 2 min with correct price "<price>" for the stored notification
    When I send paid email response back to appian
    Then The notification status should update to "<status>"
    Examples:
      | user | type | price | status | ingredient |
      | rdt1 | 1    | 150   | Paid   | SUPPA1     |
      | rdt1 | 2    | 80    | Paid   | SUPPA80    |
      | rdt1 | 3    | 150   | Paid   | SUPPA3     |
#      | rdt1 | 4    | 150   | Paid   |
#      | rdt1 | 5    | 150   | Paid   |
#      | rdt1 | 6    | 150   | Paid   |
#      | rdt1 | 7    | 150   | Paid   |


  @mcm-22 @mcm-36 @e2e @mcm-38 @mcm-109 @mcm-68 @mcm-101
  Scenario Outline: Verify invoice processing of different types of notification with TCA number
    Given I am logged into appian as "rdt1" user
    And I create new notification with following data
      | type | <type> |
    Then I should see the stored notification with status set to "<statusWithTCANumber>"
    When I login as "fin1" and generate a standard invoice
    Then I should see the stored notification with status set to "<statusWhenInvoiced>"
    Then I should receive an invoice email with heading "Uninvoiced Notifications" from appian in next 2 min with correct price "<amountToInvoice>" for the stored notification
    When I send paid email response back to appian
    Then The notification status should update to "<statusInvoicePaid>"
    Examples:
      | type | statusWithTCANumber | statusWhenInvoiced | statusInvoicePaid | amountToInvoice |
      | 1    | Ready for Invoicing | Unpaid             | Paid              | 150             |
      | 2    | Ready for Invoicing | Unpaid             | Paid              | 80              |
      | 3    | Ready for Invoicing | Unpaid             | Paid              | 150             |
#      | 4    | Ready for Invoicing | Unpaid             | Paid              | 150             |


  @mcm-22 @mcm-36 @e2e @mcm-38 @mcm-109 @mcm-68 @mcm-101
  Scenario Outline: Verify invoice processing of different types of notification without a TCA number
    Given I am logged into appian as "rdt1" user
    And I create new notification with following data
      | type          | <type>                    |
      | tcaNumber     |                           |
      | submitterName | <submitterNameGeneration> |
    Then I should see the stored notification with status set to "<initialStatus>"
    And I should see new task generated for the stored notification
    When I set a new TCA number for the notification
    Then I should see the stored notification with status set to "<statusWithTCANumber>"
    When I login as "fin1" and generate a standard invoice
    Then I should see the stored notification with status set to "<statusWhenInvoiced>"
    Then I should receive an invoice email with heading "Uninvoiced Notifications" from appian in next 2 min with correct price "<amountToInvoice>" for the stored notification
    When I send paid email response back to appian
    Then The notification status should update to "<statusInvoicePaid>"
    Examples:
      | type | statusWithTCANumber | statusWhenInvoiced | statusInvoicePaid | amountToInvoice | initialStatus | submitterNameGeneration |
      | 1    | Ready for Invoicing | Unpaid             | Paid              | 150             | Uploaded      | random                  |
      | 2    | Ready for Invoicing | Unpaid             | Paid              | 80              | Uploaded      | random                  |
      | 3    | Ready for Invoicing | Unpaid             | Paid              | 150             | Uploaded      | random                  |
#      | 4    | Ready for Invoicing | Unpaid             | Paid              | 150             | Uploaded      |random                  |


  @mcm-36 @wip @ignore
  Scenario Outline: Invoices should not be created for submission type 4 type 5 and type 7
    Given I am logged into appian as "<user>" user
    And I select a previous notification with ecid "<previousECID>"
    When I create new notification with following data
      | type | <type> |
    Then I should see the stored notification with status set to "<status>"
    When I login as "fin1" and generate a standard invoice
    Then I should receive no invoice for stored notification from appian in next 2 min for "" notifications
    Examples:
      | user | type | price | status              | ingredient | previousECID |
      | rdt1 | 5    | 150   | Ready for Invoicing | SUPPA1     |              |
      #| rdt1 | 4    | 150   | Paid   | SUPPA1     | random |
      #| rdt1 | 7    | 150   | Paid   | SUPPA1     | random |