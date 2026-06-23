# Profile to "More" Tab Refactor — Task Breakdown

## 📋 Document Info
- **Related Spec**: PROFILE_TO_MORE_TAB_REFACTOR_SPEC.md
- **Total Estimated Effort**: 3-5 days
- **Total Tasks**: 32 tasks across 5 phases
- **Status**: Ready for Implementation

---

## 🎯 Task Overview

| Phase | Tasks | Estimated Time | Priority |
|-------|-------|----------------|----------|
| Phase 1: Foundation | 8 tasks | 1 day | HIGH |
| Phase 2: Menu Sections | 7 tasks | 1 day | HIGH |
| Phase 3: Polish & Integration | 7 tasks | 1 day | MEDIUM |
| Phase 4: Testing & Refinement | 6 tasks | 1 day | HIGH |
| Phase 5: Cleanup & Documentation | 4 tasks | 1 day | MEDIUM |

---

## 📦 PHASE 1: Foundation (Day 1)

### Task 1.1: Add Localization Keys
**Priority**: HIGH  
**Estimated Time**: 30 minutes  
**Dependencies**: None

**Description**: Add all "More" tab localization keys to Common.xcstrings

**Files to Modify**:
- `App/UI/Component/Resources/Common.xcstrings`

**Acceptance Criteria**:
- [ ] Add `tab.more` with all 10 language translations
- [ ] Add `tab.more.subtitle` with all 10 language translations
- [ ] Add `more.section.account_settings` with all translations
- [ ] Add `more.section.financial_tools` with all translations
- [ ] Add `more.section.app_settings` with all translations
- [ ] Add `more.section.subscription_account` with all translations
- [ ] Verify all translations follow existing patterns
- [ ] Test String Catalog builds correctly

**Implementation Notes**:
```json
"tab.more": {
  "en": "More",
  "id": "Lainnya",
  // ... other languages
}
```

---

### Task 1.2: Create MoreMenuRow Component
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: None

**Description**: Create reusable menu row component for consistent styling

**Files to Create**:
- `App/Module/More/Sources/components/MoreMenuRow.swift`

**Acceptance Criteria**:
- [ ] Create MoreMenuRow struct with SwiftUI View
- [ ] Props: icon (String), title (String), subtitle (String?), badge (String?/Int?)
- [ ] Props: isLocked (Bool), showChevron (Bool), isDestructive (Bool)
- [ ] Minimum tap target 44pt height
- [ ] Icon frame width: 24pt for alignment
- [ ] Support for red badge indicator (notifications)
- [ ] Support for count badge (wallets)
- [ ] Lock icon appears on right when isLocked = true
- [ ] Destructive styling (red text/icon) when isDestructive = true
- [ ] Chevron appears on right when showChevron = true

**Implementation Notes**:
```swift
struct MoreMenuRow: View {
    let icon: String
    let title: String
    var subtitle: String? = nil
    var badge: String? = nil
    var isLocked: Bool = false
    var showChevron: Bool = true
    var isDestructive: Bool = false
    
    var body: some View {
        HStack(spacing: 12) {
            // Icon + Title + Accessories
        }
        .frame(minHeight: 44)
    }
}
```

---

### Task 1.3: Create MoreHeaderView Component
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: None

**Description**: Create compact horizontal profile header component

**Files to Create**:
- `App/Module/More/Sources/components/MoreHeaderView.swift`

**Acceptance Criteria**:
- [ ] Create MoreHeaderView struct with SwiftUI View
- [ ] Horizontal layout: Avatar (left) | Name + Email + Badge (right)
- [ ] Avatar size: 60x60 (smaller than current 70x70)
- [ ] Avatar uses SF Symbol "person.crop.circle.fill"
- [ ] Name: .headline font, cashaTextPrimary color
- [ ] Email: .subheadline font, cashaTextSecondary color
- [ ] Premium badge: capsule shape, same as current design
- [ ] Entire header tappable (onTap closure prop)
- [ ] Add subtle separator line at bottom
- [ ] Padding: 16pt horizontal, 16pt vertical

**Implementation Notes**:
```swift
struct MoreHeaderView: View {
    let profile: UserCasha
    let onTap: () -> Void
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 16) {
                // Avatar | VStack(Name, Email, Badge)
            }
        }
    }
}
```

---

### Task 1.4: Create MoreMenuContent Component
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Task 1.2 (MoreMenuRow)

**Description**: Create menu content with all sections using List

**Files to Create**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create MoreMenuContent struct with SwiftUI View
- [ ] Use List with .insetGrouped style
- [ ] Create 6 sections (see below)
- [ ] All binding props for sheet presentation states
- [ ] All binding props for navigation states
- [ ] Use MoreMenuRow for all menu items
- [ ] Section headers: .caption2.bold() + .uppercase()
- [ ] List row background: Color(.systemGray6)
- [ ] Hide scroll content background if iOS 16+

**Sections to Create**:
1. Account Settings (3 items)
2. Financial Tools (4 items)
3. App Settings (placeholder - can be empty for now)
4. Subscription & Account (2 items)
5. Developer Tools (DEBUG only - 5 items)
6. Logout (1 item, no header)

---

### Task 1.5: Create MoreView Main Container
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Task 1.3, 1.4

**Description**: Create main MoreView container that replaces ProfileView

**Files to Create**:
- `App/Module/More/Sources/main/MoreView.swift`

**Acceptance Criteria**:
- [ ] Create MoreView struct with SwiftUI View
- [ ] All EnvironmentObjects from ProfileView reused
- [ ] @State variables for sheet presentations
- [ ] @State variable for activeNavigation
- [ ] ZStack with cashaBackground color
- [ ] VStack containing: MoreHeaderView + MoreMenuContent
- [ ] Error banner at top if state.lastError exists
- [ ] Empty state if profile is nil
- [ ] .navigationTitle("More") - localized
- [ ] .navigationBarTitleDisplayMode(.large)
- [ ] .toolbarBackground(.hidden)
- [ ] Loading indicator in toolbar if state.isLoading
- [ ] .task { await state.refreshProfile() }

**Implementation Notes**:
- Copy structure from ProfileView.swift
- Reuse ProfileSheetsModifier (or create MoreSheetsModifier)
- Reuse all existing sheets and navigation destinations

---

### Task 1.6: Add More Tab to MainTabView
**Priority**: HIGH  
**Estimated Time**: 30 minutes  
**Dependencies**: Task 1.5

**Description**: Replace Profile tab with More tab in tab bar

**Files to Modify**:
- `App/Tabbar/MainTabView.swift`

**Acceptance Criteria**:
- [ ] Add @StateObject private var moreRouter = NavigationRouter()
- [ ] Add NavigationStack for More tab at position .tag(4)
- [ ] NavigationStack contains MoreView with all EnvironmentObjects
- [ ] Update customTabBar tabs array (replace Profile with More)
- [ ] Use "ellipsis.circle" or "line.3.horizontal" icon for More tab
- [ ] Tab title uses String(localized: "tab.more", table: "Common")
- [ ] Add moreRouter to onChange ResetTab handler (case 4)
- [ ] Verify all notification navigation paths still work
- [ ] Test tab selection and navigation

**Implementation Notes**:
```swift
NavigationStack(path: $moreRouter.path) {
    MoreView()
        .environmentObject(profileState)
        .environmentObject(goalTrackerState)
        .environmentObject(portfolioState)
        .environmentObject(moreRouter)
        .environmentObject(emailSyncState)
        .environmentObject(walletState)
}
.tag(4)
```

---

### Task 1.7: Create More Module Folder Structure
**Priority**: HIGH  
**Estimated Time**: 15 minutes  
**Dependencies**: None

**Description**: Create proper folder structure for More module

**Folders to Create**:
- `App/Module/More/`
- `App/Module/More/Sources/`
- `App/Module/More/Sources/main/`
- `App/Module/More/Sources/components/`
- `App/Module/More/Sources/subView/` (if needed later)

**Acceptance Criteria**:
- [ ] Create folder structure matching existing modules
- [ ] Verify Xcode recognizes new folders
- [ ] Add folders to version control
- [ ] Update .gitignore if needed (shouldn't be)

---

### Task 1.8: Create MoreRoute Enum
**Priority**: MEDIUM  
**Estimated Time**: 15 minutes  
**Dependencies**: None

**Description**: Create MoreRoute enum for navigation (rename from ProfileRoute)

**Files to Modify**:
- `App/Tabbar/MainTabView.swift` (at bottom where routes are defined)

**Acceptance Criteria**:
- [ ] Rename ProfileRoute enum to MoreRoute
- [ ] Keep all existing cases (portfolio, liabilities, goalTracker, categoryList, paywall, emailSync)
- [ ] Update ProfileNavigation enum to MoreNavigation (or keep as ProfileNavigation for now)
- [ ] Update all NavigationStack destination handlers to use MoreRoute
- [ ] Verify navigation still works correctly

**Implementation Notes**:
```swift
public enum MoreRoute: Hashable {
    case portfolio
    case liabilities
    case goalTracker
    case categoryList
    case paywall
    case emailSync
}
```

---

## 📦 PHASE 2: Menu Sections (Day 2)

### Task 2.1: Implement Account Settings Section
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 1 complete

**Description**: Implement Account Settings section with Edit Profile, Notifications, Wallets

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create Section with header "more.section.account_settings"
- [ ] Row 1: Edit Profile (icon: "pencil") → sets showingEditProfile = true
- [ ] Row 2: Notifications (icon: "bell.badge") → sets showingNotificationHistory = true
- [ ] Row 2: Show red badge dot if notificationHandler.hasPendingNotification
- [ ] Row 3: My Wallets (icon: "wallet.pass.fill") → sets showingWalletList = true
- [ ] Row 3: Show count badge with walletState.wallets.count
- [ ] All rows use MoreMenuRow component
- [ ] Test all three navigation paths work

---

### Task 2.2: Implement Financial Tools Section
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Phase 1 complete

**Description**: Implement Financial Tools section with premium-locked features

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create Section with header "more.section.financial_tools"
- [ ] Row 1: Portfolio (icon: "briefcase") → handleFeatureTap(.portfolio)
- [ ] Row 1: isLocked = !subscriptionManager.hasPremiumAccess
- [ ] Row 2: Liabilities (icon: "creditcard") → handleFeatureTap(.liabilities)
- [ ] Row 2: isLocked = !subscriptionManager.hasPremiumAccess
- [ ] Row 3: Goal Tracker (icon: "target") → handleFeatureTap(.goalTracker)
- [ ] Row 3: isLocked = !subscriptionManager.hasPremiumAccess
- [ ] Row 4: Manage Categories (icon: "tag") → NavigationLink(value: MoreRoute.categoryList)
- [ ] Row 4: Always unlocked (no lock icon)
- [ ] Copy handleFeatureTap logic from ProfileView
- [ ] Test premium and free user states
- [ ] Test paywall shows for locked features when tapped

**Implementation Notes**:
```swift
private func handleFeatureTap(feature: PremiumFeature) {
    if subscriptionManager.hasPremiumAccess {
        // Navigate to feature
        switch feature {
        case .portfolio: activeNavigation = .portfolio
        case .liabilities: activeNavigation = .liabilities
        case .goalTracker: activeNavigation = .goalTracker
        default: break
        }
    } else {
        // Show paywall
        activeNavigation = .paywall
    }
}
```

---

### Task 2.3: Implement App Settings Section (Placeholder)
**Priority**: LOW  
**Estimated Time**: 15 minutes  
**Dependencies**: Phase 1 complete

**Description**: Create placeholder App Settings section for future expansion

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create Section with header "more.section.app_settings"
- [ ] OPTION A: Leave section empty for now
- [ ] OPTION B: Add "Coming Soon" placeholder rows (Language, Currency, Theme)
- [ ] If OPTION B: Rows are disabled/non-interactive with .opacity(0.5)
- [ ] Comment indicating this is placeholder for future features

**Implementation Notes** (if using OPTION B):
```swift
Section(header: Text("more.section.app_settings", tableName: "Common")) {
    // Placeholder for future settings
    MoreMenuRow(icon: "globe", title: "Language")
        .disabled(true)
        .opacity(0.5)
    // ... other placeholder items
}
```

---

### Task 2.4: Implement Subscription & Account Section
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 1 complete

**Description**: Implement subscription status and account deletion

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create Section with header "more.section.subscription_account"
- [ ] Row 1: Premium Status (icon: "crown")
- [ ] Row 1: Show Active (green) or Inactive (red) badge on right
- [ ] Row 1: Badge text from subscriptionManager.hasPremiumAccess
- [ ] Row 1: Non-interactive (no tap, no chevron)
- [ ] Row 2: Delete Account (icon: "trash", red styling)
- [ ] Row 2: isDestructive = true
- [ ] Row 2: Sets showingDeleteConfirmation = true
- [ ] DEBUG: Row 3: Toggle Premium (icon: "switch.2") with PremiumDebugToggle
- [ ] Test delete confirmation dialog appears
- [ ] Test premium badge updates when subscription changes

**Implementation Notes**:
```swift
HStack {
    MoreMenuRow(icon: "crown", title: "Premium Status", showChevron: false)
    Spacer()
    Text(subscriptionManager.hasPremiumAccess ? "Active" : "Inactive")
        .font(.caption).fontWeight(.bold).foregroundColor(.white)
        .padding(.horizontal, 8).padding(.vertical, 4)
        .background(Capsule().fill(subscriptionManager.hasPremiumAccess ? Color.green : Color.red))
}
```

---

### Task 2.5: Implement Developer Tools Section
**Priority**: MEDIUM  
**Estimated Time**: 30 minutes  
**Dependencies**: Phase 1 complete

**Description**: Implement debug tools section (DEBUG builds only)

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Wrap entire section in #if DEBUG ... #endif
- [ ] Create Section with header "Developer Tools" (can be English only)
- [ ] Row 1: Test Welcome Notification → simulateNotification(.welcome)
- [ ] Row 2: Test Budget Alert → simulateNotification(.budgetAlert)
- [ ] Row 3: Crashlytics Test Button (reuse from ProfileView)
- [ ] Row 4: Crashlytics Debug View (reuse from ProfileView)
- [ ] Row 5: Toggle Premium (with PremiumDebugToggle component)
- [ ] Section only visible in DEBUG builds
- [ ] Test all debug actions work correctly

**Implementation Notes**:
- Copy simulateNotification() function from ProfileView
- Copy CrashlyticsTestButton and CrashlyticsDebugView usage
- Wrap in conditional compilation

---

### Task 2.6: Implement Logout Section
**Priority**: HIGH  
**Estimated Time**: 20 minutes  
**Dependencies**: Phase 1 complete

**Description**: Implement logout button at bottom

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Create Section with NO header (standalone at bottom)
- [ ] Row: Logout (icon: "rectangle.portrait.and.arrow.right")
- [ ] isDestructive = true (red text and icon)
- [ ] Button role: .destructive
- [ ] Taps calls: Task { await loginState.logout() }
- [ ] No chevron on logout row
- [ ] Test logout works and returns to login screen
- [ ] Test logout clears all user data

**Implementation Notes**:
```swift
Section {
    Button(role: .destructive) {
        Task { await loginState.logout() }
    } label: {
        MoreMenuRow(
            icon: "rectangle.portrait.and.arrow.right",
            title: "Logout",
            showChevron: false,
            isDestructive: true
        )
    }
}
```

---

### Task 2.7: Wire Up All Sheet Presentations
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: Task 2.1-2.6

**Description**: Connect all sheets to MoreView using modifier

**Files to Modify**:
- `App/Module/More/Sources/main/MoreView.swift`

**Acceptance Criteria**:
- [ ] Create MoreSheetsModifier (copy from ProfileSheetsModifier)
- [ ] Wire showingEditProfile → ProfileEditView sheet
- [ ] Wire showingNotificationHistory → NotificationHistoryView sheet
- [ ] Wire showingWalletList → WalletListView sheet
- [ ] Wire showingDeleteConfirmation → confirmation dialog
- [ ] Wire activeNavigation == .paywall → PaywallView sheet
- [ ] Wire .navigationDestination for MoreRoute cases
- [ ] Pass all required EnvironmentObjects to sheets
- [ ] Test all sheets open and close correctly
- [ ] Test navigation destinations work correctly

---

## 📦 PHASE 3: Polish & Integration (Day 3)

### Task 3.1: Implement Notification Badge Logic
**Priority**: HIGH  
**Estimated Time**: 30 minutes  
**Dependencies**: Task 2.1

**Description**: Show red badge on Notifications row when unread exist

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Check notificationHandler.hasPendingNotification
- [ ] Show red Circle (6x6) on Notifications row if true
- [ ] Badge positioned on right side before chevron
- [ ] Badge disappears when notifications are marked as read
- [ ] Test badge appears/disappears correctly

**Implementation Notes**:
```swift
HStack {
    MoreMenuRow(icon: "bell.badge", title: "Notifications")
    if notificationHandler.hasPendingNotification {
        Circle().fill(Color.red).frame(width: 6, height: 6)
    }
}
```

---

### Task 3.2: Implement Wallet Count Badge Logic
**Priority**: HIGH  
**Estimated Time**: 20 minutes  
**Dependencies**: Task 2.1

**Description**: Show wallet count badge on My Wallets row

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuContent.swift`

**Acceptance Criteria**:
- [ ] Get count from walletState.wallets.count
- [ ] Show count as text badge on right side
- [ ] Only show if count > 0
- [ ] Badge styling: .caption.bold(), cashaTextSecondary color
- [ ] Test badge updates when wallets are added/removed

**Implementation Notes**:
```swift
HStack {
    MoreMenuRow(icon: "wallet.pass.fill", title: "My Wallets")
    if !walletState.wallets.isEmpty {
        Text("\(walletState.wallets.count)")
            .font(.caption.bold())
            .foregroundStyle(Color.cashaTextSecondary)
    }
}
```

---

### Task 3.3: Implement Premium Lock Icons
**Priority**: HIGH  
**Estimated Time**: 30 minutes  
**Dependencies**: Task 2.2

**Description**: Show lock icons on premium-locked features

**Files to Modify**:
- `App/Module/More/Sources/components/MoreMenuRow.swift`

**Acceptance Criteria**:
- [ ] When isLocked = true, show lock icon on right
- [ ] Lock icon: "lock.fill" SF Symbol
- [ ] Lock styling: .caption2 size, cashaPrimary color opacity 0.1 background
- [ ] Lock icon in small circle
- [ ] Position before chevron (if chevron exists)
- [ ] Test lock appears/disappears based on subscription state

**Implementation Notes**:
```swift
if isLocked {
    Image(systemName: "lock.fill")
        .font(.caption2)
        .padding(4)
        .background(Color.cashaPrimary.opacity(0.1))
        .foregroundColor(.cashaPrimary)
        .clipShape(Circle())
}
```

---

### Task 3.4: Add Loading States
**Priority**: MEDIUM  
**Estimated Time**: 30 minutes  
**Dependencies**: Task 1.5

**Description**: Add loading indicators for async operations

**Files to Modify**:
- `App/Module/More/Sources/main/MoreView.swift`

**Acceptance Criteria**:
- [ ] Show ProgressView in toolbar when state.isLoading
- [ ] Show loading overlay during logout (optional)
- [ ] Show skeleton/placeholder when profile is loading
- [ ] Disable interactions during critical operations
- [ ] Test loading states appear correctly

---

### Task 3.5: Add Error States
**Priority**: MEDIUM  
**Estimated Time**: 30 minutes  
**Dependencies**: Task 1.5

**Description**: Handle and display error states gracefully

**Files to Modify**:
- `App/Module/More/Sources/main/MoreView.swift`

**Acceptance Criteria**:
- [ ] Show error banner at top if state.lastError exists
- [ ] Error banner: red background, white text, full width
- [ ] Auto-dismiss error after 3 seconds (existing logic)
- [ ] Show empty state if profile is nil (existing logic)
- [ ] Test error states display correctly
- [ ] Test delete account failure shows error

---

### Task 3.6: Implement Empty State
**Priority**: LOW  
**Estimated Time**: 20 minutes  
**Dependencies**: Task 1.5

**Description**: Show proper empty state when profile fails to load

**Files to Modify**:
- `App/Module/More/Sources/main/MoreView.swift`

**Acceptance Criteria**:
- [ ] Show when state.profile is nil
- [ ] Icon: "person.crop.circle.badge.exclamationmark" (50pt size)
- [ ] Message: "No profile found" or similar (localized)
- [ ] Gray color scheme
- [ ] Centered on screen
- [ ] Test empty state appears when profile is nil

---

### Task 3.7: Test All Navigation Paths
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: All Phase 2 and 3 tasks

**Description**: Comprehensive testing of all navigation flows

**Test Cases**:
- [ ] Tap More tab → More view loads with profile
- [ ] Tap profile header → Edit profile sheet opens
- [ ] Tap Edit Profile → Sheet opens, edit works, dismiss works
- [ ] Tap Notifications → Sheet opens with notification list
- [ ] Tap My Wallets → Sheet opens with wallet list
- [ ] Tap Portfolio (free user) → Paywall shows
- [ ] Tap Portfolio (premium user) → Portfolio view navigates
- [ ] Tap Liabilities (premium) → Liabilities view navigates
- [ ] Tap Goal Tracker (premium) → Goal tracker view navigates
- [ ] Tap Manage Categories → Categories view navigates
- [ ] Tap Delete Account → Confirmation dialog shows
- [ ] Confirm delete → Account deletes, logout occurs
- [ ] Tap Logout → Logout executes, login screen shows
- [ ] DEBUG: All debug actions work correctly
- [ ] Navigation back button works from all destinations
- [ ] Tab switching preserves/resets state appropriately

---

## 📦 PHASE 4: Testing & Refinement (Day 4)

### Task 4.1: Test All Localizations
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Phase 3 complete

**Description**: Test UI with all 10 supported languages

**Test Cases**:
- [ ] English (en) - Base language
- [ ] Indonesian (id)
- [ ] Arabic (ar) - Test RTL layout
- [ ] German (de)
- [ ] Spanish (es)
- [ ] French (fr)
- [ ] Hindi (hi)
- [ ] Japanese (ja)
- [ ] Korean (ko)
- [ ] Portuguese Brazil (pt-BR)
- [ ] Chinese Simplified (zh-Hans)

**Acceptance Criteria**:
- [ ] All tab labels translated correctly
- [ ] All section headers translated correctly
- [ ] All menu items translated correctly (reused keys)
- [ ] No missing translation warnings in Xcode
- [ ] RTL languages (Arabic) display correctly
- [ ] Long translations don't break layout
- [ ] Text truncation works properly

**Testing Method**:
1. Change device language in Settings
2. Force quit and relaunch app
3. Navigate to More tab
4. Verify all text is translated
5. Check for layout issues

---

### Task 4.2: Test Premium vs Free User States
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 3 complete

**Description**: Verify premium feature gating works correctly

**Test Cases - Free User**:
- [ ] Portfolio row shows lock icon
- [ ] Liabilities row shows lock icon
- [ ] Goal Tracker row shows lock icon
- [ ] Categories row does NOT show lock icon
- [ ] Premium Status badge shows "Inactive" (red)
- [ ] Tapping Portfolio shows paywall
- [ ] Tapping Liabilities shows paywall
- [ ] Tapping Goal Tracker shows paywall
- [ ] Tapping Categories navigates directly (unlocked)

**Test Cases - Premium User**:
- [ ] Portfolio row does NOT show lock icon
- [ ] Liabilities row does NOT show lock icon
- [ ] Goal Tracker row does NOT show lock icon
- [ ] Premium Status badge shows "Active" (green)
- [ ] Tapping Portfolio navigates to portfolio view
- [ ] Tapping Liabilities navigates to liabilities view
- [ ] Tapping Goal Tracker navigates to goal tracker view

**Test Cases - Premium State Change**:
- [ ] DEBUG: Toggle premium → Lock icons appear/disappear immediately
- [ ] DEBUG: Toggle premium → Status badge updates immediately
- [ ] Real purchase → All premium features unlock immediately

---

### Task 4.3: Test Accessibility (VoiceOver & Dynamic Type)
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Phase 3 complete

**Description**: Verify accessibility compliance

**VoiceOver Test Cases**:
- [ ] Enable VoiceOver on device/simulator
- [ ] Navigate to More tab using VoiceOver
- [ ] Verify tab label is read correctly
- [ ] Verify profile header is read correctly (name, email, status)
- [ ] Verify all menu items are read correctly
- [ ] Verify lock status is announced for locked items
- [ ] Verify badge counts are announced
- [ ] Verify buttons are identified as buttons
- [ ] Verify navigation links are identified correctly
- [ ] Test navigation with VoiceOver gestures

**Dynamic Type Test Cases**:
- [ ] Set text size to smallest (Accessibility > Display > Text Size)
- [ ] Verify all text is readable and not cut off
- [ ] Set text size to largest
- [ ] Verify layout doesn't break with large text
- [ ] Verify minimum tap targets maintained (44pt)
- [ ] Verify badges scale appropriately
- [ ] Verify icons don't overlap text

**Color Contrast Test**:
- [ ] Verify text meets WCAG AA standards (4.5:1 for normal text)
- [ ] Verify red text (destructive actions) is readable
- [ ] Verify badges are visible and readable
- [ ] Test in Light mode
- [ ] Test in Dark mode

---

### Task 4.4: Test Error Scenarios
**Priority**: MEDIUM  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 3 complete

**Description**: Test error handling and edge cases

**Test Cases**:
- [ ] No internet connection → Profile load fails → Error message shows
- [ ] Profile API returns error → Error banner shows
- [ ] Delete account API fails → Error alert shows
- [ ] Logout fails → Error handled gracefully
- [ ] Wallet count is 0 → Badge doesn't show
- [ ] No notifications → Badge doesn't show
- [ ] Profile is nil → Empty state shows
- [ ] Network timeout → Error shows with retry option
- [ ] Rapid tab switching → No crashes or UI glitches
- [ ] Memory warning → App doesn't crash
- [ ] Background/foreground transitions → State preserved

---

### Task 4.5: Test Deep Links and Notifications
**Priority**: HIGH  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 3 complete

**Description**: Verify notification navigation paths still work

**Test Cases**:
- [ ] Push notification → App opens to correct tab
- [ ] In-app notification banner → Tap navigates correctly
- [ ] NavigateToProfile notification → Opens More tab
- [ ] NotificationCenter "NavigateToProfile" → More tab selected
- [ ] Deep link to profile section → Opens More tab
- [ ] Widget quick action → App opens correctly
- [ ] URL scheme → Navigation works
- [ ] Universal link → Navigation works
- [ ] Background app launch → Notification handling works
- [ ] Foreground app → Banner shows and navigation works

**Files to Check**:
- `App/Tabbar/MainTabView.swift` - Notification observers
- `App/AppDelegate.swift` - Notification handling
- Any URL scheme handlers

**Potential Updates Needed**:
- Update "NavigateToProfile" to "NavigateToMore" (or keep as is)
- Verify tag 4 still maps to More tab correctly

---

### Task 4.6: Performance Testing
**Priority**: MEDIUM  
**Estimated Time**: 30 minutes  
**Dependencies**: Phase 3 complete

**Description**: Verify performance benchmarks are met

**Test Cases**:
- [ ] List scroll performance: 60fps on target devices
- [ ] Profile load time: < 2 seconds on good network
- [ ] Tab switch time: Instant (< 100ms)
- [ ] Sheet presentation: Smooth animation (< 300ms)
- [ ] Navigation push: Smooth animation (< 300ms)
- [ ] Memory usage: No significant leaks
- [ ] CPU usage: Normal during idle
- [ ] Battery impact: Similar to before refactor

**Testing Method**:
- Use Xcode Instruments (Time Profiler, Leaks, Energy Log)
- Test on older devices (iPhone SE 2nd gen minimum)
- Monitor frame rate during interactions
- Compare metrics before/after refactor

---

## 📦 PHASE 5: Cleanup & Documentation (Day 5)

### Task 5.1: Code Review and Refinement
**Priority**: HIGH  
**Estimated Time**: 1 hour  
**Dependencies**: Phase 4 complete

**Description**: Self-review and cleanup code

**Checklist**:
- [ ] Remove all console logs and debug prints
- [ ] Remove commented-out code
- [ ] Check for unused imports
- [ ] Verify consistent code formatting
- [ ] Verify naming conventions followed
- [ ] Check for force unwraps (avoid if possible)
- [ ] Add TODO comments for future improvements
- [ ] Verify all EnvironmentObjects are needed
- [ ] Check for retain cycles in closures
- [ ] Review error handling completeness
- [ ] Verify all strings are localized
- [ ] Check for hardcoded values that should be constants

**Code Quality Checks**:
- [ ] SwiftLint passes (if configured)
- [ ] No compiler warnings
- [ ] Build succeeds in Release mode
- [ ] All deprecation warnings addressed

---

### Task 5.2: Update Documentation
**Priority**: MEDIUM  
**Estimated Time**: 45 minutes  
**Dependencies**: Phase 4 complete

**Description**: Update project documentation

**Files to Update/Create**:
- [ ] Update `MOBILE_APP_FLOW.md` with More tab info
- [ ] Update `README.md` if it references Profile tab
- [ ] Create `MORE_TAB_MIGRATION.md` with change summary
- [ ] Update any architecture diagrams
- [ ] Update navigation flow diagrams
- [ ] Add inline code comments for complex logic
- [ ] Document public APIs if any

**Documentation Content**:
- Overview of More tab structure
- List of all menu sections and items
- Premium feature gating logic
- Navigation patterns used
- State management approach
- Known limitations or edge cases

---

### Task 5.3: Remove Old Profile Tab References
**Priority**: LOW  
**Estimated Time**: 30 minutes  
**Dependencies**: Phase 4 complete

**Description**: Clean up any old Profile tab code if needed

**Tasks**:
- [ ] Search codebase for "ProfileView" references
- [ ] Verify all references are intentional (reused views are OK)
- [ ] Check for "Profile" tab string references
- [ ] Update notification names if needed (NavigateToProfile)
- [ ] Check URL schemes for profile references
- [ ] Update deep link handlers if needed
- [ ] Verify no dead code left behind
- [ ] Remove unused assets if any

**Note**: ProfileView.swift and related components should be KEPT as they're reused by MoreView. Only remove true dead code.

---

### Task 5.4: Final QA Testing
**Priority**: HIGH  
**Estimated Time**: 1.5 hours  
**Dependencies**: All previous tasks

**Description**: Comprehensive end-to-end testing before release

**Full User Journey Tests**:

**Journey 1: New User**
1. [ ] Launch app → Login → See More tab in tab bar
2. [ ] Tap More tab → See profile loaded
3. [ ] Tap profile header → Edit profile opens
4. [ ] Edit name → Save → See name updated in header
5. [ ] Tap Notifications → See empty state or notifications
6. [ ] Tap My Wallets → See wallet list
7. [ ] Tap Portfolio (free user) → See paywall
8. [ ] Tap Logout → Return to login

**Journey 2: Premium User**
1. [ ] Login as premium user → Navigate to More tab
2. [ ] Verify Premium Status shows "Active" (green)
3. [ ] Verify no lock icons on premium features
4. [ ] Tap Portfolio → Navigate to portfolio view successfully
5. [ ] Go back → Tap Liabilities → Navigate successfully
6. [ ] Go back → Tap Goal Tracker → Navigate successfully
7. [ ] Go back → Tap Manage Categories → Navigate successfully
8. [ ] All navigation smooth and functional

**Journey 3: Notification Flow**
1. [ ] Trigger test notification (DEBUG)
2. [ ] See in-app banner
3. [ ] Tap banner → Navigate correctly
4. [ ] Go to More tab → See red badge on Notifications
5. [ ] Open Notifications → Badge disappears
6. [ ] Verify notification marked as read

**Journey 4: Account Management**
1. [ ] Navigate to More tab
2. [ ] Scroll to Subscription & Account
3. [ ] Note current premium status
4. [ ] Tap Delete Account
5. [ ] See confirmation dialog
6. [ ] Cancel → Nothing happens
7. [ ] Tap Delete Account again → Confirm
8. [ ] Account deleted → Logged out → At login screen

**Regression Tests**:
- [ ] All other tabs still work correctly
- [ ] Add transaction flow works
- [ ] Dashboard loads correctly
- [ ] Transactions tab works
- [ ] Budget tab works
- [ ] Report tab works
- [ ] Tab switching is smooth
- [ ] App doesn't crash during normal usage

**Edge Case Tests**:
- [ ] Airplane mode → Proper error handling
- [ ] Poor network → Loading states work
- [ ] Kill app → State preserved correctly
- [ ] Background/foreground → No issues
- [ ] Memory pressure → No crashes
- [ ] Rapid navigation → No race conditions

---

## 📊 Progress Tracking

### Phase Completion Checklist

- [ ] **Phase 1: Foundation** (8/8 tasks)
  - [ ] Task 1.1: Add Localization Keys
  - [ ] Task 1.2: Create MoreMenuRow Component
  - [ ] Task 1.3: Create MoreHeaderView Component
  - [ ] Task 1.4: Create MoreMenuContent Component
  - [ ] Task 1.5: Create MoreView Main Container
  - [ ] Task 1.6: Add More Tab to MainTabView
  - [ ] Task 1.7: Create More Module Folder Structure
  - [ ] Task 1.8: Create MoreRoute Enum

- [ ] **Phase 2: Menu Sections** (7/7 tasks)
  - [ ] Task 2.1: Implement Account Settings Section
  - [ ] Task 2.2: Implement Financial Tools Section
  - [ ] Task 2.3: Implement App Settings Section (Placeholder)
  - [ ] Task 2.4: Implement Subscription & Account Section
  - [ ] Task 2.5: Implement Developer Tools Section
  - [ ] Task 2.6: Implement Logout Section
  - [ ] Task 2.7: Wire Up All Sheet Presentations

- [ ] **Phase 3: Polish & Integration** (7/7 tasks)
  - [ ] Task 3.1: Implement Notification Badge Logic
  - [ ] Task 3.2: Implement Wallet Count Badge Logic
  - [ ] Task 3.3: Implement Premium Lock Icons
  - [ ] Task 3.4: Add Loading States
  - [ ] Task 3.5: Add Error States
  - [ ] Task 3.6: Implement Empty State
  - [ ] Task 3.7: Test All Navigation Paths

- [ ] **Phase 4: Testing & Refinement** (6/6 tasks)
  - [ ] Task 4.1: Test All Localizations
  - [ ] Task 4.2: Test Premium vs Free User States
  - [ ] Task 4.3: Test Accessibility (VoiceOver & Dynamic Type)
  - [ ] Task 4.4: Test Error Scenarios
  - [ ] Task 4.5: Test Deep Links and Notifications
  - [ ] Task 4.6: Performance Testing

- [ ] **Phase 5: Cleanup & Documentation** (4/4 tasks)
  - [ ] Task 5.1: Code Review and Refinement
  - [ ] Task 5.2: Update Documentation
  - [ ] Task 5.3: Remove Old Profile Tab References
  - [ ] Task 5.4: Final QA Testing

---

## 🎯 Definition of Done

A task is considered "done" when:

1. **Code is written** and follows project conventions
2. **Code compiles** without errors or warnings
3. **Functionality works** as described in acceptance criteria
4. **Edge cases handled** (nil values, errors, empty states)
5. **UI looks correct** on all device sizes (SE, standard, Plus/Max)
6. **Accessibility works** (VoiceOver, Dynamic Type)
7. **Localization complete** (all strings translated)
8. **Testing passed** (manual testing of all paths)
9. **Code reviewed** (self-review or peer review)
10. **Documented** (inline comments for complex logic)

---

## 🚨 Blockers and Dependencies

### External Dependencies
- None - All dependencies are internal to the codebase

### Potential Blockers
1. **Design Approval** - Need confirmation on reference design details
2. **Localization Review** - Translations may need review by native speakers
3. **Testing Devices** - Need access to test devices for various iOS versions
4. **Backend Changes** - None required (purely frontend refactor)

### Risk Mitigation
- Start with Phase 1 immediately (no blockers)
- Clarify design questions during Phase 1
- Use placeholder text if translations delayed
- Test on simulators if devices unavailable

---

## 📝 Notes and Considerations

### Design Decisions

**1. Why "More" instead of "Settings" or "Profile"?**
- "More" is more flexible (can include non-settings items)
- Common pattern in popular apps (Instagram, Twitter)
- Allows future expansion without renaming

**2. Why keep ProfileView components?**
- Maximizes code reuse
- Reduces testing surface
- Faster implementation
- No breaking changes to existing sheets

**3. Why horizontal header instead of vertical?**
- Saves screen space
- More modern design pattern
- Better information density
- Easier to scan

**4. Why include App Settings placeholder?**
- Future-proofs the design
- Provides natural expansion point
- Avoids major refactor later
- Can be hidden if not needed

### Technical Decisions

**1. Component Architecture**
- MoreView (container) → MoreHeaderView + MoreMenuContent
- MoreMenuContent → Multiple section views
- MoreMenuRow (reusable atomic component)
- Follows existing app patterns

**2. State Management**
- Reuse existing ProfileState (no changes needed)
- Reuse existing LoginState (no changes needed)
- @State variables for UI state (sheets, navigation)
- EnvironmentObjects for shared state

**3. Navigation Pattern**
- NavigationStack with NavigationPath (iOS 16+)
- Sheet presentation for modal views
- NavigationLink for hierarchical navigation
- Matches existing tab navigation patterns

**4. Premium Feature Gating**
- Leverage existing SubscriptionManager
- No changes to premium logic
- UI-only lock indicators
- Paywall for free users
- Direct navigation for premium users

### Future Enhancements (Not in Scope)

These can be added later without major refactor:

1. **App Settings Section**
   - Language preference selector
   - Currency preference selector
   - Theme selector (light/dark/auto)
   - Notification preferences
   - Privacy settings

2. **Profile Customization**
   - Profile photo upload (currently system icon)
   - Bio or tagline
   - Display preferences

3. **Quick Actions**
   - Swipe actions on menu items
   - Long-press context menus
   - Shortcuts integration

4. **Analytics**
   - Track which features are used most
   - Track premium conversion from locked features
   - User behavior insights

5. **Social Features**
   - Share profile
   - Referral system
   - Social media integration

---

## 🔗 Related Documents

- `PROFILE_TO_MORE_TAB_REFACTOR_SPEC.md` - Full requirements specification
- `MOBILE_APP_FLOW.md` - Current app flow documentation
- `App/Module/Profile/Sources/ProfileView.swift` - Current implementation
- `App/Tabbar/MainTabView.swift` - Tab bar implementation
- `App/UI/Component/Resources/Common.xcstrings` - Localization strings

---

## ✅ Sign-off

### Pre-Implementation Checklist
- [ ] Requirements document reviewed and approved
- [ ] Design questions answered
- [ ] Team has capacity for 3-5 day effort
- [ ] No blocking dependencies identified
- [ ] Development environment ready

### Ready to Start?
Once all items above are checked, proceed with **Phase 1: Foundation**

---

## 📞 Questions or Issues?

If you encounter issues during implementation:

1. **Design Questions** → Refer to PROFILE_TO_MORE_TAB_REFACTOR_SPEC.md
2. **Technical Questions** → Review existing ProfileView.swift patterns
3. **Localization Issues** → Check Common.xcstrings for patterns
4. **Navigation Issues** → Review MainTabView.swift routing patterns
5. **State Management Issues** → Check existing state implementations

---

**Document Complete** ✅  
**Total Tasks**: 32 tasks across 5 phases  
**Estimated Effort**: 3-5 days  
**Status**: Ready for implementation

---

## 📈 Quick Reference Summary

### Files to Create (New)
1. `App/Module/More/Sources/main/MoreView.swift`
2. `App/Module/More/Sources/components/MoreHeaderView.swift`
3. `App/Module/More/Sources/components/MoreMenuContent.swift`
4. `App/Module/More/Sources/components/MoreMenuRow.swift`

### Files to Modify
1. `App/Tabbar/MainTabView.swift` - Add More tab, MoreRoute enum
2. `App/UI/Component/Resources/Common.xcstrings` - Add localization keys

### Files to Reuse (No Changes)
- All ProfileView subviews and sheets
- ProfileState.swift
- LoginState.swift
- SubscriptionManager.swift
- All EnvironmentObjects

### Key Components
- **MoreView** - Main container (like ProfileView)
- **MoreHeaderView** - Compact profile header
- **MoreMenuContent** - List with 6 sections
- **MoreMenuRow** - Reusable row component

### Key Features
- ✅ Horizontal compact header
- ✅ 6 organized menu sections
- ✅ Premium feature locks
- ✅ Notification badges
- ✅ Wallet count badges
- ✅ All existing functionality preserved
- ✅ Full localization (10 languages)
- ✅ Accessibility compliant
- ✅ No breaking changes

---

**Happy Coding!** 🚀
