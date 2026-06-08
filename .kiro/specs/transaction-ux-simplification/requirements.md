# Requirements Document

## Introduction

This document specifies the requirements for the Transaction UX Simplification feature in the Casha Android app. The feature aims to improve user experience by reducing visual noise, consolidating fragmented layouts, improving action accessibility, and adding helpful financial summaries to the transaction management flow.

The improvements span three main areas:
1. **Transaction List** - Adding period summaries, removing redundant badges, and enabling quick actions
2. **Transaction Detail** - Consolidating multiple cards into a unified view and improving action accessibility
3. **Transaction Edit** - Removing confusing internal state elements and improving clarity

## Glossary

- **Transaction_System**: The Android transaction management module including list, detail, and edit screens
- **User**: The person using the Casha app to manage their financial transactions
- **Period_Summary**: Aggregated financial data (income, expense, net) for the currently selected time period
- **Context_Menu**: A popup menu that appears on long-press gesture showing available actions
- **Unified_Card**: A single consolidated card component that replaces multiple separate card components
- **Sync_State**: The boolean status indicating whether a transaction has been synchronized with the remote server
- **INCOME**: A transaction type representing money received
- **EXPENSE**: A transaction type representing money spent
- **Type_Badge**: A visual label displaying "INCOME" or "EXPENSE" text on transaction items

## Requirements

### Requirement 1: Period Financial Summary Display

**User Story:** As a user, I want to see aggregated financial totals for the selected period at the top of my transaction list, so that I can quickly understand my income, expenses, and net position without manual calculation.

#### Acceptance Criteria

1. WHEN a user views the transaction list, THE Transaction_System SHALL display a period summary showing total income, total expense, and net amount for all visible transactions
2. WHEN the user changes the time filter (This month, Other month, This year, Custom), THE Transaction_System SHALL recalculate and update the period summary to reflect the new period
3. THE Transaction_System SHALL display income values in green color with an appropriate icon
4. THE Transaction_System SHALL display expense values in red color with an appropriate icon
5. THE Transaction_System SHALL display net amount in green when positive and red when negative
6. WHEN the transaction list is empty, THE Transaction_System SHALL display zero values for all period summary fields

### Requirement 2: Transaction List Item Visual Simplification

**User Story:** As a user, I want transaction list items to show only essential information without redundant visual elements, so that I can scan my transactions more easily and focus on important details.

#### Acceptance Criteria

1. THE Transaction_System SHALL display each transaction item with icon, name, category, amount, and time
2. THE Transaction_System SHALL NOT display type badges (INCOME/EXPENSE text labels) on transaction list items
3. THE Transaction_System SHALL differentiate transaction types using icon background color (green for income, red for expense)
4. THE Transaction_System SHALL differentiate transaction types using amount color (green for income, red for expense)
5. WHEN displaying transaction amounts, THE Transaction_System SHALL use the existing color coding without adding text badges

### Requirement 3: Quick Action Context Menu

**User Story:** As a user, I want to quickly edit or delete transactions directly from the list using a long-press menu, so that I don't have to navigate through multiple screens for simple actions.

#### Acceptance Criteria

1. WHEN a user long-presses a transaction list item, THE Transaction_System SHALL display a context menu with Edit and Delete options
2. WHEN a user selects Edit from the context menu, THE Transaction_System SHALL navigate to the transaction detail screen in edit mode
3. WHEN a user selects Delete from the context menu, THE Transaction_System SHALL display a confirmation dialog
4. WHEN a user confirms deletion from the context menu, THE Transaction_System SHALL delete the transaction and refresh the list
5. IF a transaction has Sync_State equal to false, THEN THE Transaction_System SHALL disable Edit and Delete options in the context menu
6. WHEN context menu actions are disabled, THE Transaction_System SHALL provide visual indication of the disabled state

### Requirement 4: Unified Transaction Detail Card

**User Story:** As a user, I want to see all transaction details in a single consolidated card instead of multiple separate cards, so that I can view all information at once without scrolling on most devices.

#### Acceptance Criteria

1. THE Transaction_System SHALL display transaction details in a single unified card component
2. THE unified card SHALL display a compact icon (40dp) instead of the previous large decorative icon (80dp)
3. THE unified card SHALL display transaction name and category with date inline in the header section
4. THE unified card SHALL display the transaction amount in a prominent size with type-appropriate color
5. THE unified card SHALL display wallet name and creation date in a details section
6. THE Transaction_System SHALL NOT display type badges (INCOME/EXPENSE text) in the detail view
7. THE Transaction_System SHALL NOT display sync status as a separate row in the detail view
8. THE Transaction_System SHALL NOT display "updated at" timestamp in the detail view
9. THE unified card SHALL have a total height not exceeding 200dp for standard transaction data

### Requirement 5: Direct Action Buttons in Detail View

**User Story:** As a user, I want to access Edit and Delete actions directly from prominent buttons in the detail view, so that I can perform actions with fewer taps.

#### Acceptance Criteria

1. THE Transaction_System SHALL display Edit and Delete buttons at the bottom of the unified transaction detail card
2. THE Edit button SHALL use primary color styling with a pencil icon
3. THE Delete button SHALL use destructive color styling (red) with a trash icon
4. WHEN a user taps the Edit button, THE Transaction_System SHALL open the edit bottom sheet
5. WHEN a user taps the Delete button, THE Transaction_System SHALL display a confirmation dialog
6. IF the transaction has Sync_State equal to false, THEN THE Transaction_System SHALL disable both Edit and Delete buttons
7. WHEN buttons are disabled due to sync state, THE Transaction_System SHALL display an inline status message indicating "Syncing with server..."
8. WHEN the transaction sync completes, THE Transaction_System SHALL automatically enable the action buttons

### Requirement 6: Simplified Edit Form

**User Story:** As a user, I want the transaction edit form to show only editable fields without confusing internal technical controls, so that I can make changes easily without risk of corrupting data.

#### Acceptance Criteria

1. THE Transaction_System SHALL display edit fields for Name, Amount, Category, and Date in the edit bottom sheet
2. THE Transaction_System SHALL NOT display a "Confirmed" toggle or any direct sync state control in the edit form
3. IF a transaction has Sync_State equal to false, THEN THE Transaction_System SHALL display a read-only status indicator showing "Syncing..." with a progress indicator
4. WHEN a user attempts to save with invalid input (empty name or zero amount), THE Transaction_System SHALL display inline error messages on the invalid fields
5. WHEN a user attempts to save with invalid input, THE Transaction_System SHALL disable the Save button until all validations pass
6. WHEN a user saves valid changes, THE Transaction_System SHALL update the transaction and close the bottom sheet

### Requirement 7: Delete Operation Safety

**User Story:** As a user, I want to be protected from accidental deletions with confirmation dialogs, so that I don't lose important transaction data due to mistaken taps.

#### Acceptance Criteria

1. WHEN a user initiates a delete action (from detail view or context menu), THE Transaction_System SHALL display a confirmation dialog before executing the deletion
2. THE confirmation dialog SHALL clearly state which transaction will be deleted (show transaction name and amount)
3. WHEN a user confirms deletion, THE Transaction_System SHALL delete the transaction from local database
4. WHEN a user confirms deletion, THE Transaction_System SHALL sync the deletion with the remote server
5. IF the deletion fails, THEN THE Transaction_System SHALL display an error dialog with a descriptive message
6. IF the deletion fails, THEN THE Transaction_System SHALL retain the transaction in the list without modification
7. WHEN deletion succeeds, THE Transaction_System SHALL navigate back to the transaction list and refresh the displayed data

### Requirement 8: Sync State Handling

**User Story:** As a user, I want clear feedback when transactions are syncing with the server, so that I understand why certain actions are temporarily unavailable.

#### Acceptance Criteria

1. WHEN a transaction has Sync_State equal to false, THE Transaction_System SHALL display visual indication that syncing is in progress
2. WHEN a transaction is syncing, THE Transaction_System SHALL disable Edit and Delete actions
3. WHEN displaying disabled actions due to sync state, THE Transaction_System SHALL show explanatory text such as "Syncing with server..." or "Waiting for synchronization..."
4. WHEN a transaction sync completes successfully, THE Transaction_System SHALL automatically update the UI to enable actions
5. THE Transaction_System SHALL listen to sync completion events from the SyncEventBus
6. WHEN sync completion is detected, THE Transaction_System SHALL refresh transaction data and update button states accordingly

### Requirement 9: Period Summary Calculation Correctness

**User Story:** As a user, I want the period summary to accurately reflect my transaction totals, so that I can trust the displayed financial information for decision-making.

#### Acceptance Criteria

1. FOR ALL transactions in the current period, THE Transaction_System SHALL calculate total income as the sum of all amounts where type equals INCOME
2. FOR ALL transactions in the current period, THE Transaction_System SHALL calculate total expense as the sum of all absolute amounts where type equals EXPENSE
3. THE Transaction_System SHALL calculate net amount as total income minus total expense
4. WHEN the transaction list contains no items, THE Transaction_System SHALL display period summary with zero values for all fields
5. WHEN transactions are added or removed, THE Transaction_System SHALL recalculate the period summary to reflect the updated data

### Requirement 10: Visual Space Efficiency

**User Story:** As a user, I want the detail screen to fit on my device without excessive scrolling, so that I can view all information at a glance.

#### Acceptance Criteria

1. THE unified transaction detail card SHALL occupy no more than 200dp in height for standard transaction data
2. THE unified card SHALL reduce vertical space usage by at least 40% compared to the previous multi-card layout
3. WHEN displaying transaction details on standard Android devices, THE Transaction_System SHALL show all essential information without requiring vertical scrolling
4. THE Transaction_System SHALL use a compact icon size (40dp) instead of large decorative icons (80dp)

