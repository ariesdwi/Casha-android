# Casha Android — Detailed Module Specs & Development Timeline

> Complete reference with every model definition, screen/view breakdown per module, and a step-by-step sprints timeline.

---

## Table of Contents

1. [Module 1: Auth](#module-1-auth)
2. [Module 2: Dashboard](#module-2-dashboard)
3. [Module 3: Transactions](#module-3-transactions)
4. [Module 4: AddTransaction (AI Chat)](#module-4-addtransaction)
5. [Module 5: Budget](#module-5-budget)
6. [Module 6: GoalTracker](#module-6-goaltracker)
7. [Module 7: Portfolio](#module-7-portfolio)
8. [Module 8: Liabilities](#module-8-liabilities)
9. [Module 9: Income](#module-9-income)
10. [Module 10: Report](#module-10-report)
11. [Module 11: Profile](#module-11-profile)
12. [Module 12: Recommendation (AI)](#module-12-recommendation)
13. [Module 13: Subscription](#module-13-subscription)
14. [Module 14: Progress (Overlay)](#module-14-progress)
15. [Shared Components](#shared-components)
16. [Development Timeline](#development-timeline)

---

## Module 1: Auth

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **LoginScreen** | `LoginView.swift` | Email/password login, SSO buttons (Google, Apple) |
| **RegisterScreen** | `RegisterView.swift` | Registration form (name, email, phone, password) |
| **ForgotPasswordScreen** | `ForgotPassView.swift` | Email input for password reset |
| **SetupCurrencyScreen** | `SetupCurrency.swift` | Currency picker on first login |

### State / ViewModel

| State | File | Published Properties |
|:------|:-----|:---------------------|
| `LoginState` | `LoginState.swift` | `email`, `password`, `isLoading`, `isLoggedIn`, `activeAuthMethod`, `errorMessage` |
| `RegisterState` | `RegisterState.swift` | `name`, `email`, `phone`, `password`, `isLoading`, `toastMessage` |

### Domain Models

#### `UserCasha`
```kotlin
data class UserCasha(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val phone: String?,
    val currency: String,
    val createdAt: Date,
    val updatedAt: Date
)
```

#### `LoginResult`
```kotlin
data class LoginResult(
    val token: String,
    val currency: String?
)
```

#### `UpdateProfileRequest`
```kotlin
data class UpdateProfileRequest(
    val name: String?,
    val email: String?,
    val phone: String?,
    val avatar: String?,
    val currency: String?
)
```

### API Endpoints Used
- `POST auth/login` → `LoginResult`
- `POST auth/apple` → `LoginResult`
- `POST auth/google` → `LoginResult`
- `POST auth/signup` → `String` (token)
- `POST auth/forgot-password`

---

## Module 2: Dashboard

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **DashboardScreen** | `DashboardView.swift` | Main home screen |
| ↳ `CardBalanceCard` | `CardBalanceView.swift` | Net cashflow card with income/expense mini stats |
| ↳ `ReportChart` | `ReportChartView.swift` | Week/month toggle bar chart |
| ↳ `GoalSection` | (inline) | Horizontal scroll of top 3 goals |
| ↳ `RecentTransactions` | `RecentTransactionList.swift` | Last 5 cashflow items in card |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `DashboardState` | `DashboardState.swift` | `recentTransactions: [CashflowEntry]`, `cashflowSummary: CashflowSummary?`, `report: SpendingReport`, `unsyncedCount: Int`, `isOnline: Bool`, `isSyncing: Bool`, `selectedPeriod: SpendingPeriod` |

### Domain Models

#### `CashflowEntry`
```kotlin
data class CashflowEntry(
    val id: String,
    val type: CashflowType,        // INCOME, EXPENSE
    val name: String,
    val amount: Double,
    val datetime: Date,
    val category: String,
    val direction: CashflowDirection // IN, OUT
) {
    val signedAmount: Double get() = if (direction == IN) amount else -amount
}

enum class CashflowType { INCOME, EXPENSE }
enum class CashflowDirection { IN, OUT }
```

#### `CashflowSummary`
```kotlin
data class CashflowSummary(
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netCashflow: Double,
    val savingsRate: Double,
    val debtServiceRatio: Double,
    val byCategory: Map<String, Double>
)
```

#### `SpendingReport`
```kotlin
data class SpendingReport(
    val thisWeekTotal: Double,
    val thisMonthTotal: Double,
    val dailyBars: List<SpendingBar>,   // 7 bars (Mon-Sun)
    val weeklyBars: List<SpendingBar>   // 4-5 bars (Week 1-4)
)

data class SpendingBar(
    val label: String,
    val amount: Double
)
```

#### `SpendingPeriod`
```kotlin
sealed class SpendingPeriod {
    object ThisMonth : SpendingPeriod()
    object LastMonth : SpendingPeriod()
    object ThisYear : SpendingPeriod()
    object AllTime : SpendingPeriod()
    data class Custom(val start: Date, val end: Date) : SpendingPeriod()
}
```

#### `CashflowDateSection`
```kotlin
data class CashflowDateSection(
    val date: String,
    val day: String,
    val items: List<CashflowEntry>
) {
    val totalIn: Double get() = items.filter { it.direction == IN }.sumOf { it.amount }
    val totalOut: Double get() = items.filter { it.direction == OUT }.sumOf { it.amount }
    val netAmount: Double get() = totalIn - totalOut
}
```

#### `CashflowHistoryResult`
```kotlin
data class CashflowHistoryResult(
    val entries: List<CashflowEntry>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
```

### API Endpoints Used
- `GET cashflow/history` → paginated `CashflowEntry[]`
- `GET cashflow/summary` → `CashflowSummary`
- Local CoreData for report chart data

---

## Module 3: Transactions

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **TransactionListScreen** | `TransactionListView.swift` | Paginated list grouped by date |
| ↳ `TransactionFilterBar` | `TransactionFilterBar.swift` | Period picker (week/month/year/custom) |
| ↳ `TransactionCard` | `TransactionCardView.swift` | Single transaction row in card |
| **TransactionDetailScreen** | `TransactionDetailView.swift` | Full detail + edit/delete |
| **TransactionEditScreen** | `TransactionEditView.swift` | Edit form for name, amount, category, date |
| ↳ `TenorSelectionSheet` | `TenorSelectionSheet.swift` | Installment tenor picker for credit card transactions |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `TransactionState` | `TransactionState.swift` | `transactions: [TransactionCasha]`, `searchQuery: String`, `selectedPeriod`, `isLoading` |

### Domain Models

#### `TransactionCasha`
```kotlin
data class TransactionCasha(
    val id: String,
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: Date,
    val isSynced: Boolean,
    val liabilityId: String?,     // non-null if credit card transaction
    val createdAt: Date,
    val updatedAt: Date
)
```

#### `AddTransactionRequest`
```kotlin
data class AddTransactionRequest(
    val message: String?,                    // AI chat text
    val imageURL: String?,                   // Receipt image
    val structuredData: TransactionStructuredData?  // Manual form
)

data class TransactionStructuredData(
    val id: String?,
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: String,          // ISO8601
    val currency: String?
)
```

#### `UpdateTransactionRequest`
```kotlin
data class UpdateTransactionRequest(
    val name: String?,
    val category: String?,
    val amount: Double?,
    val datetime: String?          // ISO8601
)
```

### API Endpoints Used
- `POST transactions/create` → `TransactionCasha`
- `GET transactions` → `TransactionCasha[]`
- `PATCH cashflow/{type}/{id}` → update
- `DELETE cashflow/{type}/{id}` → delete

---

## Module 4: AddTransaction

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **AddTransactionCoordinator** | `AddTransactionCoordinator.swift` | Full-screen overlay, routes between AI chat and manual form |
| **AddMessageScreen** | `AddMessageView.swift` | AI chat input (text + image) for natural language transaction |
| **AddTransactionScreen** | `AddTransactionView.swift` | Manual form: name, category, amount (calculator), date picker |
| **CashaIntents** | `CashaIntents.swift` | Siri Shortcuts integration |

### Domain Models
Uses `AddTransactionRequest`, `TransactionStructuredData`, `ChatParseResult`

#### `ChatParseResult`
```kotlin
data class ChatParseResult(
    val transactions: List<ParsedTransaction>,
    val incomes: List<ParsedIncome>
)

data class ParsedTransaction(
    val name: String,
    val category: String,
    val amount: Double,
    val datetime: String,
    val currency: String?
)
```

### API Endpoints Used
- `POST chat/parse` → AI text parsing → `ChatParseResult`
- `POST chat/parse-image` → Receipt image parsing (multipart)
- `POST transactions/create`
- `POST income`

---

## Module 5: Budget

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **BudgetScreen** | `BudgetView.swift` | Main budget page |
| ↳ `BudgetSummaryCard` | `BudgetSummaryView.swift` | Total budget vs spent overview |
| ↳ `BudgetFilterBar` | `BudgetFilterBar.swift` | Filter by period/category |
| ↳ `BudgetList` | `BudgetListView.swift` | List of budget cards |
| ↳ `BudgetCard` | `BudgetCardView.swift` | Progress bar + remaining amount |
| **AddBudgetScreen** | `AddBudgetView.swift` | Create budget form (category, amount, period) |
| **EditBudgetScreen** | `editBudgetView.swift` | Edit existing budget |
| **BudgetAIAdvisorScreen** | `BudgetAIAdvisorView.swift` | AI-recommended budgets |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `BudgetState` | `BudgetState.swift` | `budgets: [BudgetCasha]`, `summary: BudgetSummary?`, `recommendations: [BudgetRecommendation]`, `isLoading` |

### Domain Models

#### `BudgetCasha`
```kotlin
data class BudgetCasha(
    val id: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val period: String,           // "MONTHLY", "WEEKLY"
    val startDate: Date,
    val endDate: Date,
    val category: String,
    val currency: String,
    val isSynced: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)
```

#### `BudgetSummary`
```kotlin
data class BudgetSummary(
    val totalBudget: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
    val budgetCount: Int
)
```

#### `NewBudgetReq`
```kotlin
data class NewBudgetReq(
    val category: String,
    val amount: Double,
    val period: String,
    val currency: String
)
```

#### `BudgetAIRecommendation`
```kotlin
data class BudgetAIRecommendation(
    val id: String,
    val category: String,
    val suggestedAmount: Double,
    val reason: String,
    val averageSpending: Double
)
```

### API Endpoints Used
- `GET budgets/` → `BudgetCasha[]`
- `GET budgets/summary` → `BudgetSummary`
- `POST budgets/` → create
- `PUT budgets/{id}` → update
- `DELETE budgets/{id}` → delete
- `GET budgets/recommendations` → AI recommendations
- `POST budgets/apply-recommendations` → bulk apply

---

## Module 6: GoalTracker

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **GoalTrackerScreen** | `GoalTrackerView.swift` | List of all goals with summary stats |
| **GoalTrackerDetailScreen** | `GoalTrackerDetailView.swift` | Hero card, progress, quick actions, contribution history |
| **AddGoalScreen** | `AddGoalView.swift` | Create goal form (name, target, category, deadline, linked asset) |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `GoalTrackerState` | `GoalTrackerState.swift` | `goals: [Goal]`, `goalCategories: [GoalCategory]`, `summary: GoalSummary?`, `isLoading` |

### Domain Models

#### `Goal`
```kotlin
data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String,
    val category: GoalCategory,
    val icon: String?,
    val color: String?,
    val deadline: Date?,
    val status: GoalStatus,
    val assetId: String?,         // linked portfolio asset
    val assetName: String?,
    val note: String?,
    val progress: GoalProgress,
    val recentContributions: List<GoalContribution>,
    val createdAt: Date,
    val updatedAt: Date
)

enum class GoalStatus { ACTIVE, PAUSED, COMPLETED, CANCELLED }
```

#### `GoalCategory`
```kotlin
data class GoalCategory(
    val id: String,
    val name: String,
    val icon: String,             // SF Symbol name → Material Icon
    val color: String,            // hex color
    val isActive: Boolean,
    val userId: String?
)
```

#### `GoalProgress`
```kotlin
data class GoalProgress(
    val percentage: Double,       // 0.0 - 100.0
    val daysRemaining: Int?,
    val monthlySavingsNeeded: Double?
)
```

#### `GoalContribution`
```kotlin
data class GoalContribution(
    val id: String,
    val goalId: String,
    val amount: Double,
    val note: String?,
    val datetime: Date
)
```

#### `GoalSummary`
```kotlin
data class GoalSummary(
    val totalGoals: Int,
    val activeGoals: Int,
    val completedGoals: Int,
    val totalTarget: Double,
    val totalCurrent: Double,
    val overallProgress: Double,
    val nearestDeadline: NearestDeadline?
)

data class NearestDeadline(
    val name: String,
    val deadline: Date
)
```

### API Endpoints Used
- `POST goals` → create
- `GET goals` → list
- `GET goals/summary` → summary
- `GET goals/{id}` → detail
- `PATCH goals/{id}` → update
- `DELETE goals/{id}` → delete
- `POST goals/{id}/contributions` → add contribution
- `GET goals/{id}/contributions` → contribution list
- `GET/POST/PATCH/DELETE goals/categories` → category CRUD

---

## Module 7: Portfolio

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **AssetsListScreen** | `AssetsListView.swift` | Grid/list of all assets by category |
| ↳ `PortfolioSummaryCard` | `PortfolioSummaryCard.swift` | Total portfolio value, PnL |
| **AssetDetailScreen** | `AssetDetailView.swift` | Asset info, transactions, linked incomes |
| **CreateAssetScreen** | `CreateAssetView.swift` | Form: name, type (28 types), amount/quantity, location |
| **EditAssetScreen** | `EditAssetView.swift` | Edit asset fields |
| **AddAssetTransactionScreen** | `AddAssetTransactionView.swift` | Buy/sell/dividend transaction |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `PortfolioState` | `PortfolioState.swift` | `assets: [Asset]`, `summary: PortfolioSummary?`, `isLoading` |

### Domain Models

#### `Asset`
```kotlin
data class Asset(
    val id: String,
    val name: String,
    val type: AssetType,          // 28 types (see enum below)
    val amount: Double,
    val currency: String,
    val description: String?,
    val userId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val incomeEntries: List<IncomeCasha>?,
    // Quantity-based tracking
    val quantity: Double?,
    val unit: String?,
    val pricePerUnit: Double?,
    // Additional
    val acquisitionDate: Date?,
    val location: String?
)
```

#### `AssetType` (28 types, 9 categories)
```kotlin
enum class AssetType(val rawValue: String, val category: AssetCategory) {
    // Liquid Assets
    CASH("CASH", AssetCategory.LIQUID),
    SAVINGS_ACCOUNT("SAVINGS_ACCOUNT", AssetCategory.LIQUID),
    CHECKING_ACCOUNT("CHECKING_ACCOUNT", AssetCategory.LIQUID),
    DEPOSIT("DEPOSIT", AssetCategory.LIQUID),
    // Equity
    STOCK("STOCK", AssetCategory.EQUITY),
    MUTUAL_FUND("MUTUAL_FUND", AssetCategory.EQUITY),
    ETF("ETF", AssetCategory.EQUITY),
    // Fixed Income
    BOND_GOVERNMENT("BOND_GOVERNMENT", AssetCategory.FIXED_INCOME),
    BOND_CORPORATE("BOND_CORPORATE", AssetCategory.FIXED_INCOME),
    SUKUK("SUKUK", AssetCategory.FIXED_INCOME),
    // Commodities
    GOLD_PHYSICAL("GOLD_PHYSICAL", AssetCategory.COMMODITIES),
    GOLD_DIGITAL("GOLD_DIGITAL", AssetCategory.COMMODITIES),
    SILVER_PHYSICAL("SILVER_PHYSICAL", AssetCategory.COMMODITIES),
    // Crypto
    CRYPTOCURRENCY("CRYPTOCURRENCY", AssetCategory.CRYPTO),
    // Real Estate
    LAND("LAND", AssetCategory.REAL_ESTATE),
    RESIDENTIAL_PROPERTY("RESIDENTIAL_PROPERTY", AssetCategory.REAL_ESTATE),
    COMMERCIAL_PROPERTY("COMMERCIAL_PROPERTY", AssetCategory.REAL_ESTATE),
    // Vehicles
    VEHICLE_CAR("VEHICLE_CAR", AssetCategory.VEHICLES),
    VEHICLE_MOTORCYCLE("VEHICLE_MOTORCYCLE", AssetCategory.VEHICLES),
    VEHICLE_OTHER("VEHICLE_OTHER", AssetCategory.VEHICLES),
    // Business
    BUSINESS_OWNERSHIP("BUSINESS_OWNERSHIP", AssetCategory.BUSINESS),
    // Others
    INSURANCE_CASH_VALUE("INSURANCE_CASH_VALUE", AssetCategory.OTHERS),
    PENSION_FUND("PENSION_FUND", AssetCategory.OTHERS),
    RECEIVABLES("RECEIVABLES", AssetCategory.OTHERS),
    OTHER("OTHER", AssetCategory.OTHERS);

    val isQuantityBased: Boolean get() = this in listOf(
        STOCK, MUTUAL_FUND, ETF, BOND_GOVERNMENT, BOND_CORPORATE,
        SUKUK, GOLD_PHYSICAL, GOLD_DIGITAL, SILVER_PHYSICAL,
        CRYPTOCURRENCY, LAND
    )
}

enum class AssetCategory {
    LIQUID, EQUITY, FIXED_INCOME, COMMODITIES, CRYPTO,
    REAL_ESTATE, VEHICLES, BUSINESS, OTHERS
}
```

#### `PortfolioSummary`
```kotlin
data class PortfolioSummary(
    val totalValue: Double,
    val totalCostBasis: Double,
    val unrealizedPnL: Double,
    val assetCount: Int,
    val byCategory: Map<String, Double>
)
```

#### `AssetTransaction`
```kotlin
data class AssetTransaction(
    val id: String,
    val assetId: String,
    val type: String,            // "BUY", "SELL", "DIVIDEND"
    val amount: Double,
    val quantity: Double?,
    val pricePerUnit: Double?,
    val datetime: Date,
    val note: String?
)
```

#### Request Models
```kotlin
data class CreateAssetRequest(
    val name: String,
    val type: AssetType,
    val amount: Double?,
    val currency: String?,
    val description: String?,
    val quantity: Double?,
    val unit: String?,
    val pricePerUnit: Double?,
    val acquisitionDate: Date?,
    val location: String?
)

data class UpdateAssetRequest(
    val amount: Double?,
    val name: String?,
    val quantity: Double?,
    val unit: String?,
    val pricePerUnit: Double?,
    val acquisitionDate: Date?,
    val location: String?
)
```

### API Endpoints Used
- `POST assets` → create
- `GET assets` → list
- `GET assets/portfolio-summary` → summary
- `PATCH assets/{id}` → update
- `DELETE assets/{id}` → delete
- `POST assets/{id}/transactions` → add transaction
- `GET assets/{id}/transactions` → list transactions

---

## Module 8: Liabilities

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **LiabilitiesListScreen** | `LiabilitiesListView.swift` | List of all debts/credit cards |
| ↳ `LiabilitySummaryCard` | `LiabilitySummaryCard.swift` | Total debt, monthly payment, interest |
| **LiabilityDetailScreen** | `LiabilityDetailView.swift` | Full detail: balance, payments, statements, insights |
| **CreateLiabilityScreen** | `CreateLiabilityView.swift` | Form: type, amount, rate, billing cycle |
| **StatementDetailScreen** | `StatementDetailView.swift` | Credit card statement detail |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `LiabilityState` | `LiabilityState.swift` | `liabilities: [Liability]`, `summary: LiabilitySummary?`, `selectedLiability: Liability?`, `statements`, `insights` |

### Domain Models

#### `Liability`
```kotlin
data class Liability(
    val id: String,
    val name: String,
    val principal: Double,
    val currentBalance: Double,
    val interestRate: Double,          // e.g. 8.5 for 8.5%
    val startDate: Date,
    val endDate: Date,
    val currency: String?,
    val monthlyPayment: Double?,
    val payments: List<LiabilityPayment>?,
    val transactions: List<LiabilityTransaction>?,
    // Credit Card Specific
    val bankName: String?,
    val category: LiabilityCategory,
    val creditLimit: Double?,
    val billingDay: Int?,
    val dueDay: Int?,
    val minPaymentPercentage: Double?,
    val lateFee: Double?,
    val interestType: InterestType?,   // MONTHLY, FLAT
    val status: String?,
    val isPaid: Boolean?,
    val description: String?,
    // Insight Fields
    val availableCredit: Double?,
    val monthlyInterestAmount: Double?,
    val daysRemaining: Int?,
    val isOverdue: Boolean?,
    val totalInterestPaid: Double?,
    val totalPrincipalPaid: Double?
)

enum class LiabilityCategory {
    MORTGAGE, PERSONAL_LOAN, AUTO_LOAN, STUDENT_LOAN,
    BUSINESS_LOAN, CREDIT_CARD, PAY_LATER, OTHER
}

enum class InterestType { MONTHLY, FLAT }
```

#### `LiabilityPayment`
```kotlin
data class LiabilityPayment(
    val id: String,
    val liabilityId: String,
    val loanName: String?,
    val amount: Double,
    val principalPaid: Double?,
    val interestPaid: Double?,
    val datetime: Date,
    val paymentDate: String?,              // "yyyy-MM-dd"
    val paymentType: PaymentType?,         // FULL, PARTIAL, MINIMUM, EXTRA
    val balanceAfterPayment: Double?,
    val status: String?,
    val createdAt: Date?
)

enum class PaymentType { FULL, PARTIAL, MINIMUM, EXTRA }
```

#### `LiabilityStatement`
```kotlin
data class LiabilityStatement(
    val id: String,
    val liabilityId: String,
    val startDate: Date,
    val endDate: Date,
    val dueDate: Date,
    val statementBalance: Double,
    val minimumPayment: Double,
    val status: StatementStatus,           // OPEN, PAID, PARTIAL, LATE
    val paymentsMade: Double,
    val previousBalance: Double,
    val purchasesMade: Double,
    val interestCharged: Double,
    val lateFeesCharged: Double,
    val transactions: List<LiabilityTransaction>,
    val reminders: List<String>,
    val warnings: List<String>
)

enum class StatementStatus { OPEN, PAID, PARTIAL, LATE }
```

#### `LiabilityInsight`
```kotlin
data class LiabilityInsight(
    val totalInterestPaid: Double,
    val totalLateFeesPaid: Double,
    val projectedInterestIfMinimum: Double,
    val savingsIfPaidInFull: Double,
    val predictionMessages: List<PredictionMessage>
)

data class PredictionMessage(
    val type: String,           // "MINIMUM_PAYMENT", "FULL_PAYMENT"
    val message: String,
    val amount: Double
)
```

#### `LiabilitySummary`
```kotlin
data class LiabilitySummary(
    val totalDebt: Double,
    val totalPrincipal: Double,
    val totalCurrentBalance: Double,
    val totalMonthlyPayment: Double,
    val totalMonthlyInterest: Double,
    val totalYearlyInterest: Double,
    val averageInterestRate: Double,
    val activeLoansCount: Int,
    val overdueLoansCount: Int,
    val nextPaymentDue: String?,
    val totalInterestPaid: Double,
    val categoryBreakdown: List<CategoryBreakdown>,
    val currency: String?
)
```

### API Endpoints Used
- Full CRUD + statements + payments + insights (14 endpoints)

---

## Module 9: Income

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **IncomeListScreen** | `IncomeListView.swift` | List of all income entries |
| **AddIncomeScreen** | `AddIncomeView.swift` | Create income form (name, amount, type, source, recurring) |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `IncomeState` | `IncomeState.swift` | `incomes: [IncomeCasha]`, `summary: IncomeSummary?`, `isLoading` |

### Domain Models

#### `IncomeCasha`
```kotlin
data class IncomeCasha(
    val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String?,
    val assetId: String?,             // linked to portfolio asset
    val isRecurring: Boolean,
    val frequency: IncomeFrequency?,
    val note: String?,
    val createdAt: Date,
    val updatedAt: Date
)

enum class IncomeType {
    SALARY, FREELANCE, BUSINESS, INVESTMENT, GIFT, REFUND, OTHER
}

enum class IncomeFrequency {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY
}
```

#### `IncomeSummary`
```kotlin
data class IncomeSummary(
    val totalIncome: Double,
    val count: Int,
    val byType: List<IncomeTypeBreakdown>
)

data class IncomeTypeBreakdown(
    val type: IncomeType,
    val total: Double,
    val count: Int
)
```

### API Endpoints Used
- `POST income` → create
- `GET income` → list
- `GET income/summary` → summary

---

## Module 10: Report

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **ReportScreen** | `ReportView.swift` | Spending analytics overview |
| ↳ `CategoryPieChart` | `ReportCategoryPieChart.swift` | Donut chart by category |
| ↳ `CategoryList` | `ReportCategoryList.swift` | Category breakdown list |
| **TransactionByCategoryScreen** | `TransactionListByCategoryView.swift` | Drill-down into category |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `ReportState` | `ReportState.swift` | `categorySpending: [ChartCategorySpending]`, `selectedCategory` |

### Domain Models

#### `ChartCategorySpending`
```kotlin
data class ChartCategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val color: Color
)
```

#### `TransactionDateSection`
```kotlin
data class TransactionDateSection(
    val date: String,
    val transactions: List<TransactionCasha>
)
```

---

## Module 11: Profile

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **ProfileScreen** | `ProfileView.swift` | Settings: avatar, name, currency, logout, delete |
| **ProfileEditScreen** | `ProfileEditView.swift` | Edit profile form |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `ProfileState` | `ProfileState.swift` | `profile: UserCasha?`, `isLoading` |

### API Endpoints Used
- `GET auth/profile`
- `PATCH auth/profile/`
- `DELETE auth/profile/`

---

## Module 12: Recommendation

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **BudgetRecommendationsScreen** | `BudgetRecommendationsView.swift` | AI-analyzed spending → suggested budgets |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `RecommendationState` | `RecommendationState.swift` | `recommendations`, `isLoading` |

### Domain Models

#### `BudgetRecommendation`
```kotlin
data class BudgetRecommendation(
    val id: String,
    val category: String,
    val suggestedAmount: Double,
    val reason: String,
    val currentSpending: Double
)
```

---

## Module 13: Subscription

### Screens & Views

| Screen | File | Description |
|:-------|:-----|:------------|
| **PaywallScreen** | `PaywallView.swift` | Paywall with monthly/lifetime options |

### State / ViewModel

| State | File | Key Properties |
|:------|:-----|:---------------|
| `SubscriptionManager` | `SubscriptionManager.swift` | `hasPremiumAccess: Bool`, `subscriptionType: SubscriptionType`, `products` |

### Domain Models
```kotlin
enum class SubscriptionType { NONE, MONTHLY, LIFETIME }

data class SubscriptionStatus(
    val isActive: Boolean,
    val type: SubscriptionType,
    val expiresAt: Date?
)
```

---

## Module 14: Progress

### Files

| File | Description |
|:-----|:------------|
| `ProgressState.swift` | Global processing state singleton |
| `ProgressOverlay.swift` | Full-screen loading overlay |
| `ProgressModifier.swift` | `.withProgressOverlay()` modifier |
| `ProcessingStatus.swift` | Status enum |

---

## Shared Components

### Domain Models Used Everywhere

#### `CategoryCasha`
```kotlin
data class CategoryCasha(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val userId: String?           // null = system category
) {
    val isSystem: Boolean get() = userId == null
}
```

### API Endpoints (Categories)
- `GET categories` → list
- `POST categories` → create
- `PATCH categories/{id}` → update
- `DELETE categories/{id}` → delete

---

## Development Timeline

### Phase 0: Project Setup (Week 1)
- [ ] Create Android project with Kotlin + Jetpack Compose
- [ ] Setup Hilt dependency injection
- [ ] Setup Retrofit + OkHttp + interceptors (auth, language)
- [ ] Setup Room database + entities
- [ ] Setup DataStore for preferences
- [ ] Setup Navigation (NavHost + routes)
- [ ] Implement `CashaTheme` (colors, typography, shapes)
- [ ] Configure Firebase (FCM, Crashlytics)
- [ ] Setup build variants (dev, staging, prod)

### Phase 1: Core Infrastructure (Week 2)
- [ ] Implement `AuthManager` (token storage, DataStore)
- [ ] Implement `NetworkMonitor` (ConnectivityManager)
- [ ] Implement `CurrencyFormatter` utility
- [ ] Implement `DateHelper` utility
- [ ] Implement base Retrofit API service with `BaseResponse<T>`
- [ ] Implement all Retrofit endpoint interfaces
- [ ] Implement Room entities (Transaction, Budget, Category, Income)
- [ ] Implement Room DAOs

### Phase 2: Auth Module (Week 3)
- [ ] Domain: `UserCasha`, `LoginResult` models
- [ ] Domain: Auth repository interface
- [ ] Data: Auth DTOs + repository impl
- [ ] Domain: `LoginUseCase`, `RegisterUseCase`, `GoogleLoginUseCase`
- [ ] UI: `LoginScreen` (email + Google SSO)
- [ ] UI: `RegisterScreen`
- [ ] UI: `ForgotPasswordScreen`
- [ ] UI: `SetupCurrencyScreen`
- [ ] ViewModel: `LoginViewModel`, `RegisterViewModel`
- [ ] Wire up auth flow with token + currency saving

### Phase 3: Dashboard Module (Week 4)
- [ ] Domain: `CashflowEntry`, `CashflowSummary`, `SpendingReport` models
- [ ] Domain: Cashflow repository interface
- [ ] Data: Cashflow DTOs + repository impl
- [ ] Domain: Use cases (GetRecentTransactions, GetTotalSpending, GetSpendingReport, CashflowSync)
- [ ] UI: `DashboardScreen` with `BalanceCard`, `ReportChart`, `GoalSection`, `RecentTransactions`
- [ ] ViewModel: `DashboardViewModel`
- [ ] Implement parallel data loading on app launch (`AppLoadingScreen`)

### Phase 4: Transaction Module (Week 5)
- [ ] Domain: `TransactionCasha` model + local repo interface
- [ ] Data: Transaction DTOs + Room entity + DAO
- [ ] Data: Local + Remote repository impl
- [ ] Domain: CRUD use cases + sync use case
- [ ] UI: `TransactionListScreen` + filter bar + grouped by date
- [ ] UI: `TransactionDetailScreen` + `TransactionEditScreen`
- [ ] ViewModel: `TransactionViewModel`
- [ ] Implement offline-first sync

### Phase 5: AddTransaction + AI Chat (Week 6)
- [ ] Domain: `AddTransactionRequest`, `ChatParseResult` models
- [ ] Data: Chat DTOs + repository impl
- [ ] UI: `AddTransactionCoordinator` (bottom sheet/full screen)
- [ ] UI: `AddMessageScreen` (AI chat input)
- [ ] UI: `AddTransactionScreen` (manual form + calculator)
- [ ] UI: Calculator numpad component

### Phase 6: Budget Module (Week 7)
- [ ] Domain: `BudgetCasha`, `BudgetSummary` models
- [ ] Data: Budget DTOs + Room entity + repository impl
- [ ] Domain: Budget CRUD use cases
- [ ] UI: `BudgetScreen` + summary card + card list
- [ ] UI: `AddBudgetScreen` + `EditBudgetScreen`
- [ ] UI: `BudgetAIAdvisorScreen`
- [ ] ViewModel: `BudgetViewModel`

### Phase 7: Category Module (Week 7, parallel)
- [ ] Domain: `CategoryCasha` model
- [ ] Data: Category Room entity + DAO + sync logic
- [ ] Domain: Category CRUD + sync use cases
- [ ] UI: Category picker component (used in Transaction + Budget)

### Phase 8: Income Module (Week 8)
- [ ] Domain: `IncomeCasha`, `IncomeSummary` models
- [ ] Data: Income DTOs + Room entity + repository impl
- [ ] Domain: Income CRUD use cases
- [ ] UI: `IncomeListScreen` + `AddIncomeScreen`
- [ ] ViewModel: `IncomeViewModel`

### Phase 9: GoalTracker Module (Week 9)
- [ ] Domain: `Goal`, `GoalCategory`, `GoalProgress`, `GoalContribution`, `GoalSummary` models
- [ ] Data: GoalTracker DTOs + repository impl
- [ ] Domain: Goals CRUD + contributions use cases
- [ ] UI: `GoalTrackerScreen` + `GoalTrackerDetailScreen` + `AddGoalScreen`
- [ ] UI: `GoalCard` component
- [ ] ViewModel: `GoalTrackerViewModel`

### Phase 10: Report Module (Week 10)
- [ ] Domain: `ChartCategorySpending` model
- [ ] Domain: Category spending use cases
- [ ] UI: `ReportScreen` + pie chart + category list
- [ ] UI: `TransactionByCategoryScreen`
- [ ] ViewModel: `ReportViewModel`

### Phase 11: Portfolio Module (Week 11-12)
- [ ] Domain: `Asset` (28 types), `AssetTransaction`, `PortfolioSummary` models
- [ ] Data: Portfolio DTOs + repository impl
- [ ] Domain: CRUD use cases
- [ ] UI: `AssetsListScreen` + `PortfolioSummaryCard`
- [ ] UI: `AssetDetailScreen` + `CreateAssetScreen` + `EditAssetScreen`
- [ ] UI: `AddAssetTransactionScreen`
- [ ] ViewModel: `PortfolioViewModel`

### Phase 12: Liabilities Module (Week 13-14)
- [ ] Domain: `Liability`, `LiabilityPayment`, `LiabilityStatement`, `LiabilityInsight` models
- [ ] Data: Liability DTOs + repository impl (most complex)
- [ ] Domain: CRUD use cases + statements + payments + insights
- [ ] UI: `LiabilitiesListScreen` + `LiabilitySummaryCard`
- [ ] UI: `LiabilityDetailScreen` + `CreateLiabilityScreen` + `StatementDetailScreen`
- [ ] ViewModel: `LiabilityViewModel`

### Phase 13: Profile + Settings (Week 15)
- [ ] UI: `ProfileScreen` + `ProfileEditScreen`
- [ ] ViewModel: `ProfileViewModel`
- [ ] Implement avatar upload (multipart)
- [ ] Implement logout + clear data
- [ ] Implement delete account

### Phase 14: Subscription (Week 15, parallel)
- [ ] Integrate Google Play Billing Library
- [ ] Domain: `SubscriptionType`, `SubscriptionStatus` models
- [ ] UI: `PaywallScreen`
- [ ] ViewModel: `SubscriptionViewModel`

### Phase 15: Push Notifications (Week 16)
- [ ] Setup FCM token registration
- [ ] Implement notification handling + deep linking
- [ ] In-app notification banner component

### Phase 16: Recommendation (AI) (Week 16, parallel)
- [ ] Domain: `BudgetRecommendation` model
- [ ] Data: Recommendation DTOs + repository impl
- [ ] UI: `BudgetRecommendationsScreen`

### Phase 17: Localization (Week 17)
- [ ] Extract all strings to `strings.xml`
- [ ] Translate to 11 languages (en, id, ar, de, es, fr, hi, ja, ko, pt-BR, zh-Hans)
- [ ] Implement language-aware API headers

### Phase 18: Polish & QA (Week 18-19)
- [ ] Implement dark mode support
- [ ] Add micro-animations and transitions
- [ ] Performance optimization (lazy loading, caching)
- [ ] End-to-end testing
- [ ] Fix edge cases (offline, empty states, error handling)
- [ ] Crashlytics integration

### Phase 19: Release (Week 20)
- [ ] Play Store listing (screenshots, descriptions)
- [ ] Beta testing
- [ ] Production release

---

### Summary: 20-Week Plan

| Phase | Weeks | Modules | Complexity |
|:------|:------|:--------|:-----------|
| Setup + Core | 1-2 | Infrastructure | 🟡 Medium |
| Auth + Dashboard | 3-4 | Auth, Dashboard | 🟡 Medium |
| Transaction + AI | 5-6 | Transaction, AddTransaction | 🟡 Medium |
| Budget + Category | 7 | Budget, Category | 🟢 Low |
| Income + Goals | 8-9 | Income, GoalTracker | 🟡 Medium |
| Report | 10 | Report | 🟢 Low |
| Portfolio | 11-12 | Portfolio (28 types) | 🔴 High |
| Liabilities | 13-14 | Liabilities (complex) | 🔴 High |
| Profile + Subs | 15 | Profile, Subscription | 🟢 Low |
| Notif + AI + L10n | 16-17 | Push, Recommendation, Localization | 🟡 Medium |
| Polish + Release | 18-20 | QA, Performance, Release | 🟡 Medium |

---

*Generated from Casha iOS codebase analysis — February 20, 2026*
