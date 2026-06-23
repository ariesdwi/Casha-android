# Profile to "More" Tab Refactor — Requirements & Design Spec

## 📋 Document Metadata
- **Feature**: Profile to More Tab Migration
- **Status**: Requirements Phase
- **Effort Estimate**: Medium (3-5 days)
- **Priority**: Medium
- **Created**: 2026-06-07
- **Last Updated**: 2026-06-07

---

## 🎯 Executive Summary

Refactor the existing Profile tab into a new "More" tab in the main tab bar, modernizing the UX and making secondary features more discoverable. This change aligns with common mobile app patterns (like Instagram, Twitter, etc.) where "More" or "Menu" tabs consolidate settings, profile, and utility features.

### Key Changes
- Replace "Profile" tab with "More" tab in main navigation
- Redesign layout based on reference design
- Maintain all existing functionality
- Improve discoverability of premium features
- Add localization for new "More" tab

---

## 📊 Current State Analysis

### Current Tab Bar Structure
```
[Report] [Home] [+Add] [Transactions] [Budget]
         Profile accessed via: ??? (needs verification)
```

**Note**: Profile appears to be accessible but tab position unclear from MainTabView.swift analysis.

### Current Profile Features (ProfileView.swift)

#### 1. **Profile Header**
- Avatar (system icon)
- User name
- Email
- Premium/Free badge

#### 2. **Account Settings Section**
- Edit Profile (sheet)
- Notifications (sheet with badge indicator)

#### 3. **Wallets Section**
- My Wallets (sheet) with wallet count badge

#### 4. **Financial Liberty Section** (Premium Features)
- Portfolio (locked for free users)
- Liabilities (locked for free users)
- Goal Tracker (locked for free users)
- Manage Categories (unlocked)

#### 5. **Subscription Section**
- Delete Account (destructive action)
- Premium Status (Active/Inactive badge)
- Toggle Premium (DEBUG only)

#### 6. **Developer Tools Section** (DEBUG only)
- Test Welcome Notification
- Test Budget Alert
- Crashlytics Test Button
- Crashlytics Debug View

#### 7. **Logout Section**
- Logout button (destructive)

---

## 🎨 Design Requirements (Based on Reference Image Analysis)

### Reference Design Overview
> **Note**: User provided design image showing desired "More" tab layout

### Expected Design Patterns
Based on common "More" tab patterns and existing code structure:

1. **Compact Profile Header**
   - Smaller, horizontal layout (vs. current vertical)
   - Avatar on left, name/email on right
   - Premium badge inline
   - Possibly tappable to view/edit full profile

2. **Grouped Menu Sections**
   - Clear visual separation between sections
   - Section headers with icons
   - Consistent row height and padding
   - Chevron indicators for navigation items

3. **Visual Hierarchy**
   - Primary actions more prominent
   - Premium features clearly marked with lock icons
   - Destructive actions (logout, delete) visually distinct
   - Status indicators (badges, counts) right-aligned

4. **Action Items**
   - Icon + Label + Accessory (badge/chevron/toggle)
   - Consistent tap targets (44pt minimum)
   - Loading states for async actions
   - Confirmation dialogs for destructive actions

---

## 📝 Functional Requirements

### FR-1: Tab Bar Integration
**Priority**: HIGH  
**Description**: Add "More" as a new tab in the main tab bar

**Acceptance Criteria**:
- [ ] "More" tab appears in tab bar at position 4 (replacing current Profile position)
- [ ] Tab uses appropriate SF Symbol icon (e.g., `ellipsis.circle`, `line.3.horizontal`, or `person.circle`)
- [ ] Tab label is "More" in English and localized in all supported languages
- [ ] Tab selection state works correctly
- [ ] Tapping tab when already selected scrolls to top (standard iOS behavior)

**Technical Notes**:
- Update `MainTabView.swift` tab items array
- Add `tab.more` and `tab.more.subtitle` to `Common.xcstrings`

---

### FR-2: Profile Header Redesign
**Priority**: HIGH  
**Description**: Create a compact, horizontal profile header

**Acceptance Criteria**:
- [ ] Header shows avatar (left), name, email, and premium badge
- [ ] Entire header is tappable to edit profile
- [ ] Header has clear visual separation from menu items below
- [ ] Avatar maintains 1:1 aspect ratio
- [ ] Premium badge uses consistent styling with rest of app
- [ ] Loading state during profile fetch shows skeleton or placeholder

**Current Behavior**:
- Vertical layout with large centered avatar (70x70)
- Separate "Edit Profile" button in menu

**New Behavior**:
- Horizontal layout with smaller avatar (50x50 or 60x60)
- Entire header acts as navigation to edit profile
- More compact to save screen space

---

### FR-3: Account Settings Section
**Priority**: HIGH  
**Description**: Group account-related settings

**Acceptance Criteria**:
- [ ] Section header: "Account Settings" (localized)
- [ ] Contains: Edit Profile, Notifications, Wallets
- [ ] Each item shows appropriate icon
- [ ] Notifications shows red badge if unread notifications exist
- [ ] Wallets shows count badge (e.g., "3")
- [ ] All items navigate to existing sheets/views

**Items**:
1. **Edit Profile** → ProfileEditView sheet
2. **Notifications** → NotificationHistoryView sheet (with unread badge)
3. **My Wallets** → WalletListView sheet (with count badge)

---

### FR-4: Financial Features Section
**Priority**: HIGH  
**Description**: Group financial tools (renamed from "Financial Liberty")

**Acceptance Criteria**:
- [ ] Section header: "Financial Tools" or "Wealth Management" (localized)
- [ ] Contains: Portfolio, Liabilities, Goal Tracker, Categories
- [ ] Premium-locked items show lock icon
- [ ] Tapping locked item shows paywall
- [ ] Tapping unlocked item navigates to feature
- [ ] Lock state reflects real-time subscription status

**Items**:
1. **Portfolio** → AssetsListView (premium locked)
2. **Liabilities** → LiabilitiesListView (premium locked)
3. **Goal Tracker** → GoalTrackerView (premium locked)
4. **Manage Categories** → CategoryListView (unlocked)

**Current Behavior**: All premium checks working correctly via `SubscriptionManager`

---

### FR-5: App Settings Section
**Priority**: MEDIUM  
**Description**: New section for app-level settings (future expansion)

**Acceptance Criteria**:
- [ ] Section header: "App Settings" (localized)
- [ ] Contains placeholder for future settings:
  - Language preferences
  - Currency settings
  - Theme selection (light/dark)
  - Notification preferences
- [ ] **Phase 1**: Can be empty or show "Coming Soon" items

**Notes**: This section provides expansion space for future settings without requiring major refactor.

---

### FR-6: Subscription & Account Section
**Priority**: HIGH  
**Description**: Group subscription status and account management

**Acceptance Criteria**:
- [ ] Section header: "Subscription & Account" (localized)
- [ ] Shows premium status with active/inactive badge
- [ ] Includes "Delete Account" option (destructive)
- [ ] Delete account shows confirmation dialog
- [ ] Premium status badge updates in real-time

**Items**:
1. **Premium Status** → Shows Active (green) or Inactive (red) badge
2. **Delete Account** → Shows confirmation dialog → Calls deleteAccount()

---

### FR-7: Developer Tools Section
**Priority**: LOW (DEBUG only)  
**Description**: Maintain debug tools for development

**Acceptance Criteria**:
- [ ] Only visible in DEBUG builds
- [ ] Contains all current debug actions
- [ ] Visually distinct (e.g., red section header)
- [ ] Does not appear in production builds

**Items** (DEBUG only):
1. Test Welcome Notification
2. Test Budget Alert  
3. Crashlytics Test Button
4. Crashlytics Debug View
5. Toggle Premium

---

### FR-8: Logout Section
**Priority**: HIGH  
**Description**: Prominent logout action

**Acceptance Criteria**:
- [ ] Separate section at bottom of list
- [ ] Red text and icon (destructive styling)
- [ ] Tapping triggers logout immediately (or with confirmation)
- [ ] Shows loading state during logout
- [ ] Clears all user data and returns to login

**Current Behavior**: Logout calls `loginState.logout()` which clears tokens and navigates to login.

---

## 🌍 Localization Requirements

### LR-1: New Localization Keys
**Priority**: HIGH

Add the following keys to `Common.xcstrings`:

```json
{
  "tab.more": {
    "en": "More",
    "id": "Lainnya",
    "ar": "المزيد",
    "de": "Mehr",
    "es": "Más",
    "fr": "Plus",
    "hi": "अधिक",
    "ja": "その他",
    "ko": "더보기",
    "pt-BR": "Mais",
    "zh-Hans": "更多"
  },
  "tab.more.subtitle": {
    "en": "Settings and more",
    "id": "Pengaturan dan lainnya",
    "ar": "الإعدادات والمزيد",
    "de": "Einstellungen und mehr",
    "es": "Ajustes y más",
    "fr": "Paramètres et plus",
    "hi": "सेटिंग्स और अधिक",
    "ja": "設定など",
    "ko": "설정 및 기타",
    "pt-BR": "Configurações e mais",
    "zh-Hans": "设置和更多"
  },
  "more.section.account_settings": {
    "en": "Account Settings",
    "id": "Pengaturan Akun",
    "ar": "إعدادات الحساب",
    "de": "Kontoeinstellungen",
    "es": "Configuración de Cuenta",
    "fr": "Paramètres du Compte",
    "hi": "खाता सेटिंग्स",
    "ja": "アカウント設定",
    "ko": "계정 설정",
    "pt-BR": "Configurações da Conta",
    "zh-Hans": "账户设置"
  },
  "more.section.financial_tools": {
    "en": "Financial Tools",
    "id": "Alat Keuangan",
    "ar": "الأدوات المالية",
    "de": "Finanztools",
    "es": "Herramientas Financieras",
    "fr": "Outils Financiers",
    "hi": "वित्तीय उपकरण",
    "ja": "財務ツール",
    "ko": "금융 도구",
    "pt-BR": "Ferramentas Financeiras",
    "zh-Hans": "财务工具"
  },
  "more.section.app_settings": {
    "en": "App Settings",
    "id": "Pengaturan Aplikasi",
    "ar": "إعدادات التطبيق",
    "de": "App-Einstellungen",
    "es": "Configuración de la App",
    "fr": "Paramètres de l'Application",
    "hi": "ऐप सेटिंग्स",
    "ja": "アプリ設定",
    "ko": "앱 설정",
    "pt-BR": "Configurações do App",
    "zh-Hans": "应用设置"
  },
  "more.section.subscription_account": {
    "en": "Subscription & Account",
    "id": "Langganan & Akun",
    "ar": "الاشتراك والحساب",
    "de": "Abonnement & Konto",
    "es": "Suscripción y Cuenta",
    "fr": "Abonnement et Compte",
    "hi": "सदस्यता और खाता",
    "ja": "サブスクリプションとアカウント",
    "ko": "구독 및 계정",
    "pt-BR": "Assinatura e Conta",
    "zh-Hans": "订阅和账户"
  }
}
```

### LR-2: Reuse Existing Keys
Reuse existing `Profile.xcstrings` keys where applicable:
- `profile.action.edit_profile`
- `profile.action.notifications`
- `profile.action.logout`
- `profile.action.delete_account`
- `profile.menu.portfolio`
- `profile.menu.liabilities`
- `profile.menu.goal_tracker`
- `profile.menu.manage_categories`

---

## 🔒 Non-Functional Requirements

### NFR-1: Performance
- [ ] List scrolling remains smooth (60fps minimum)
- [ ] Profile data loads within 2 seconds
- [ ] Navigation transitions are smooth and instant
- [ ] No jank when showing/hiding sheets

### NFR-2: Accessibility
- [ ] All interactive elements have minimum 44pt tap targets
- [ ] VoiceOver reads all labels correctly
- [ ] Dynamic Type support for all text elements
- [ ] Color contrast meets WCAG AA standards
- [ ] All icons have accessibility labels

### NFR-3: Error Handling
- [ ] Profile load failure shows error message with retry button
- [ ] Network errors during logout are handled gracefully
- [ ] Delete account failure shows error alert
- [ ] Premium status check failures don't crash app

### NFR-4: State Management
- [ ] Premium status updates reflect immediately after purchase
- [ ] Notification badge updates in real-time
- [ ] Wallet count updates when wallets change
- [ ] Profile changes reflect immediately after edit

### NFR-5: Backward Compatibility
- [ ] All existing deep links continue to work
- [ ] All existing navigation paths continue to work
- [ ] All existing sheets/views reused without breaking changes
- [ ] No breaking changes to ProfileState or LoginState

---

## 🔄 User Stories

### US-1: As a user, I want to access app settings and my profile from a "More" tab
**Scenario**: Opening More tab  
**Given**: I am logged in and on any tab  
**When**: I tap the "More" tab  
**Then**: I see my profile header and organized menu sections  

**Scenario**: Editing my profile  
**Given**: I am on the More tab  
**When**: I tap on the profile header or "Edit Profile"  
**Then**: I see the profile edit sheet  

---

### US-2: As a premium user, I want to access premium features easily
**Scenario**: Accessing portfolio  
**Given**: I am a premium user on the More tab  
**When**: I tap "Portfolio"  
**Then**: I navigate to the portfolio view  

**Scenario**: Premium features are unlocked  
**Given**: I am a premium user  
**When**: I view the Financial Tools section  
**Then**: I see no lock icons on premium features  

---

### US-3: As a free user, I want to understand which features require premium
**Scenario**: Viewing locked features  
**Given**: I am a free user on the More tab  
**When**: I view the Financial Tools section  
**Then**: I see lock icons on Portfolio, Liabilities, and Goal Tracker  

**Scenario**: Attempting to access locked feature  
**Given**: I am a free user  
**When**: I tap on a locked feature (e.g., Portfolio)  
**Then**: I see the paywall view  

---

### US-4: As a user, I want to manage my account and subscription
**Scenario**: Viewing subscription status  
**Given**: I am on the More tab  
**When**: I scroll to Subscription & Account section  
**Then**: I see my premium status (Active or Inactive)  

**Scenario**: Deleting my account  
**Given**: I am on the More tab  
**When**: I tap "Delete Account" and confirm  
**Then**: My account is deleted and I'm logged out  

---

### US-5: As a user, I want to see unread notifications
**Scenario**: Notification badge  
**Given**: I have unread notifications  
**When**: I view the More tab  
**Then**: I see a red badge on the Notifications menu item  

**Scenario**: Clearing badge  
**Given**: I have unread notifications  
**When**: I open Notifications and view them  
**Then**: The badge disappears  

---

## 🧩 Component Breakdown

### New Components to Create

#### 1. **MoreView.swift**
- Main container view for More tab
- Replaces ProfileView as tab content
- Contains: MoreHeaderView + MoreMenuContent

#### 2. **MoreHeaderView.swift**
- Compact horizontal profile header
- Tappable to edit profile
- Shows: Avatar (left) | Name + Email + Badge (right)

#### 3. **MoreMenuContent.swift**
- List-based menu with sections:
  - Account Settings
  - Financial Tools
  - App Settings (placeholder)
  - Subscription & Account
  - Developer Tools (DEBUG)
  - Logout

#### 4. **MoreMenuRow.swift**
- Reusable menu row component
- Props: icon, title, subtitle, badge, locked, chevron, destructive
- Consistent styling across all rows

### Components to Modify

#### 1. **MainTabView.swift**
- Add "More" tab at position 4
- Update tab items array
- Add MoreView with proper EnvironmentObjects
- Update NavigationStack for MoreRouter

#### 2. **Common.xcstrings**
- Add new localization keys for "More" tab
- Add new section header keys

### Components to Reuse (No Changes)

1. **ProfileEditView.swift** - Edit profile sheet
2. **NotificationHistoryView.swift** - Notification list sheet
3. **WalletListView.swift** - Wallet list sheet
4. **AssetsListView.swift** - Portfolio view
5. **LiabilitiesListView.swift** - Liabilities view
6. **GoalTrackerView.swift** - Goal tracker view
7. **CategoryListView.swift** - Category management view
8. **PaywallView.swift** - Premium paywall
9. **ProfileState.swift** - Profile state management
10. **LoginState.swift** - Login/logout state management
11. **SubscriptionManager.swift** - Premium status checks

---

## 🎯 Success Metrics

### Quantitative Metrics
- [ ] Zero crashes related to More tab functionality
- [ ] List scroll performance maintains 60fps
- [ ] Profile loads in < 2 seconds on average
- [ ] Zero accessibility violations in automated tests

### Qualitative Metrics
- [ ] UI matches reference design
- [ ] All existing functionality works correctly
- [ ] Navigation feels smooth and intuitive
- [ ] Premium features are clearly distinguished
- [ ] Code review approved by team

---

## ⚠️ Risks & Mitigation

### Risk 1: Breaking Existing Navigation
**Impact**: HIGH  
**Probability**: MEDIUM  
**Mitigation**:
- Thoroughly test all deep links
- Test all notification navigation paths
- Verify all NavigationPath routing
- Test tab switching from all states

### Risk 2: Missing Reference Design Details
**Impact**: MEDIUM  
**Probability**: HIGH  
**Mitigation**:
- Clarify design requirements with user/designer
- Use existing Casha design patterns as fallback
- Maintain visual consistency with rest of app
- Get design approval before implementation

### Risk 3: Localization Errors
**Impact**: LOW  
**Probability**: MEDIUM  
**Mitigation**:
- Reuse existing localization keys where possible
- Follow established localization patterns
- Test with all supported languages
- Use String Catalogs (xcstrings) correctly

### Risk 4: Premium State Synchronization
**Impact**: MEDIUM  
**Probability**: LOW  
**Mitigation**:
- Leverage existing SubscriptionManager
- No changes to premium logic
- Test subscription state changes
- Verify real-time updates work

---

## 📦 Dependencies

### Internal Dependencies
- `ProfileState` - Profile data management
- `LoginState` - Logout functionality
- `SubscriptionManager` - Premium status checks
- `NotificationHandler` - Notification badge logic
- `WalletState` - Wallet count
- Existing navigation routers (ProfileRouter → MoreRouter)

### External Dependencies
- None (all UI is native SwiftUI)

### Breaking Changes
- Navigation routes may need updating if external deeplinks reference Profile tab
- Document any URL scheme changes

---

## 📅 Implementation Phases

### Phase 1: Foundation (Day 1)
- [ ] Create `MoreView.swift` skeleton
- [ ] Create `MoreHeaderView.swift`
- [ ] Create `MoreMenuContent.swift`
- [ ] Create `MoreMenuRow.swift` reusable component
- [ ] Add localization keys to `Common.xcstrings`
- [ ] Update `MainTabView.swift` to include More tab

### Phase 2: Menu Sections (Day 2)
- [ ] Implement Account Settings section
- [ ] Implement Financial Tools section
- [ ] Implement Subscription & Account section
- [ ] Implement Logout section
- [ ] Wire up all navigation and sheets

### Phase 3: Polish & Integration (Day 3)
- [ ] Implement notification badge logic
- [ ] Implement wallet count badge logic
- [ ] Implement premium lock icons and logic
- [ ] Add Developer Tools section (DEBUG)
- [ ] Test all navigation paths

### Phase 4: Testing & Refinement (Day 4)
- [ ] Test all user flows end-to-end
- [ ] Test with all localizations
- [ ] Test premium/free user states
- [ ] Test accessibility (VoiceOver, Dynamic Type)
- [ ] Test error states and edge cases

### Phase 5: Cleanup & Documentation (Day 5)
- [ ] Remove old Profile tab references (if any)
- [ ] Update documentation
- [ ] Code review and refinement
- [ ] Final QA testing
- [ ] Prepare for deployment

---

## 🔍 Open Questions

### Q1: Reference Design Details
**Question**: What specific layout/styling is shown in the reference image?  
**Status**: ⏳ PENDING  
**Blocker**: No - Can use existing Casha patterns as fallback  

### Q2: App Settings Section Content
**Question**: Should we implement specific settings now or leave as placeholder?  
**Status**: ⏳ PENDING  
**Recommendation**: Leave as "Coming Soon" for Phase 1, add settings in Phase 2  

### Q3: Current Profile Tab Position
**Question**: Where is Profile currently accessible in the tab bar?  
**Status**: ⏳ PENDING (appears to be tag 4 based on MainTabView code)  
**Impact**: LOW - Will replace current position with More tab  

### Q4: Should Profile Header Be Tappable?
**Question**: Should entire header navigate to Edit Profile, or keep separate button?  
**Status**: ⏳ PENDING  
**Recommendation**: Make entire header tappable (more intuitive, saves space)  

### Q5: Logout Confirmation?
**Question**: Should logout show confirmation dialog or execute immediately?  
**Status**: ⏳ PENDING  
**Current**: Immediate logout  
**Recommendation**: Keep immediate (less friction, easy to re-login)  

---

## 📚 References

### Code References
- `App/Module/Profile/Sources/ProfileView.swift` - Current implementation
- `App/Tabbar/MainTabView.swift` - Tab bar structure
- `App/UI/Component/Resources/Common.xcstrings` - Localization patterns
- `Domain/Model/UserCasha.swift` - User profile model

### Design References
- Reference image provided by user (to be analyzed in detail)
- Existing Casha design system (colors, typography, spacing)
- iOS Human Interface Guidelines - Tab Bars

### Similar Implementations
- Instagram "More" tab
- Twitter "More" tab  
- Settings app structure

---

## ✅ Next Steps

1. **Review this requirements document with stakeholders**
2. **Get confirmation on open questions**
3. **Analyze reference image in detail** (if not already done)
4. **Proceed to Design Phase**: Create detailed technical design document
5. **Create task breakdown**: Convert phases into specific development tasks

---

## 📝 Change Log

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2026-06-07 | 1.0 | Initial requirements document created | Kiro |

---

**Document Status**: ✅ Requirements Complete - Ready for Review  
**Next Phase**: Design & Technical Specification
