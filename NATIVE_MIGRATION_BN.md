# Pocket Pro Native Migration

এই folder-এ `Pocket Pro`-র pure Kotlin native Android app step-by-step build হবে।

## Current safe status

- Existing web app untouched
- Existing `android-pocket-pro` wrapper untouched for live users
- New native project lives in `android-pocket-native`
- Current phase creates the native app foundation plus live email login, persistent session cookies, dashboard API integration, native records screen, native reports screen, native profile screen, native settings screen with default English plus top language/currency preferences, native accounts/budget/goals screens, and a native add transaction flow

## Phase roadmap

1. Kotlin + Compose foundation
2. Shared auth/API integration with existing SoftX accounts
3. Native dashboard, records, reports, budget, goals, accounts
4. Native settings, notifications, profile, subscription, export
5. Native add transaction, social login, production hardening

## Important rule

কোনো phase-এ existing live web app break করা যাবে না। Native app build হবে parallel path-এ।

## Resume note

Current completed step:

- Kotlin + Compose project scaffold
- Signed release bundle generation for native project
- Live Pocket Pro email login API
- Native dashboard API
- Native records API
- Native Compose login flow connected to backend
- Native home screen loading live dashboard data
- Native records screen loading live month-wise data
- Native reports screen loading live analytics data
- Native profile screen loading live account summary and logout action
- Persistent cookie-based session restore across app relaunch
- Native settings screen with default English, top language list, and preferred currency selection
- Native accounts screen with live filters, balances, and recent activity
- Native budget screen with live summary and category budget cards
- Native goals screen with live progress plus quick-save actions
- Native add transaction screen for income, expense, and lent entries
- Native transaction form/save APIs connected to existing Pocket Pro backend

Next coding step should focus on:

- Social login integration
- More native settings sections, export, and production polish
