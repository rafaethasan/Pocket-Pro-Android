# Pocket Pro — A-to-Z Product, UX, Engineering & Growth Audit

Audit date: 20 October 2026  
Scope: Android app source, emulator flow, release configuration, monetization, API/backend code, Play readiness, product roadmap.

## 1. Executive verdict

Pocket Pro-এর feature breadth শক্তিশালী: income/expense, loan, budget, installment, reminder, goal, report, export, backup, app lock—একটি Bangladesh-first money manager-এর জন্য ভালো foundation আছে। কিন্তু বর্তমান product এখনও production-scale quality-তে পৌঁছায়নি। প্রধান কারণ:

- automated test একটিও নেই;
- Android codebase-এর কয়েকটি file অত্যন্ত বড় এবং tightly coupled;
- bottom navigation content ঢাকে এবং একাধিক responsive layout defect দৃশ্যমান;
- backend-এর কয়েকটি native mutation endpoint-এ explicit authentication guard দেখা যায়নি;
- session cookie plaintext SharedPreferences-এ persist হয়;
- release build-এর আগে Billing 8 migration থেকে launch crash হয়েছিল;
- ads যোগ হলে Data Safety, consent ও privacy disclosures পুনরায় করতে হবে।

“Top 1” ranking guarantee করা যায় না। সঠিক লক্ষ্য হলো: reliable daily habit, high retention, strong reviews, low crash/ANR, fast entry, trusted privacy, এবং measurable store conversion।

## 2. Evidence: audited product flow

| # | Step | Evidence | Health | Key finding |
|---|---|---|---|---|
| 1 | Launch/Home | `01-launch.png`, `10-free-home.png` | Needs work | Information-rich, কিন্তু hero card ও floating nav অতিরিক্ত বড়; above-the-fold efficiency কম। |
| 2 | Quick Add | `02-add-entry.png` | Needs work | Six actions আছে; “Give Loan/Get Back/Take Loan/Pay Back” directional ambiguity সৃষ্টি করে। |
| 3 | Expense form | `03-expense-form.png` | Blocked | “Description” দুই লাইনে ভেঙেছে; zero amount-এ Save active; keyboard form context ঢাকে। |
| 4 | Records | `04-records.png` | Needs work | Empty state clear, কিন্তু huge month header ও permanent bottom nav content space কমায়। |
| 5 | Reports | `05-reports.png` | Blocked | `Pro` badge `Pr/o` হয়ে wrap করেছে; bottom nav report cards ঢাকে। Free conversion-এর পর gate removed। |
| 6 | More | `06-more.png` | Needs work | Menu comprehensive, কিন্তু long list, weak grouping, repeated icons, এবং bottom overlap আছে। |
| 7 | Settings/language | `07-settings.png` | Needs work | Bangla option আছে, কিন্তু app strings এখনও mostly hardcoded English; nav content ঢাকে। |
| 8 | Relaunch/billing | `08-premium.png` | Blocked (fixed locally) | Billing 8 mixed product query launch crash ঘটিয়েছে। Billing এখন সম্পূর্ণ removed। |
| 9 | Free + ads build | `11-free-ad.png` | Verified locally | All gates free; official AdMob test banner renders; no launch crash observed। |

Evidence folder: `audit/screens/`.

## 3. What was changed during this audit

### Completed

- সব Premium/Pro purchase path ও Google Play Billing dependency removed.
- সব feature gate default-free; pricing/upgrade row More screen থেকে removed.
- profile-এ paid plan label-এর বদলে `Free access`.
- Home-এ Quick actions-এর পরে একটি non-blocking adaptive banner placement.
- Debug build official Google test App ID ও test banner ID ব্যবহার করে.
- Release build real AdMob banner ID না পাওয়া পর্যন্ত ad request করে না.
- Billing 8 launch crash-এর root cause identified; পরে billing layer removed.
- Debug build, lint pass; emulator relaunch ও test banner verified.
- Signed release AAB `1.2.0 (2026102001)` built successfully; release lint passed. Production banner ID is intentionally blank, so this bundle will not request revenue ads yet.

### Still required before production ad revenue

- real `POCKET_ADMOB_APP_ID` and `POCKET_ADMOB_BANNER_ID`;
- AdMob app registration/readiness review;
- Google-certified consent message for EEA/UK/Switzerland;
- Play Data Safety + privacy policy update;
- developer website-এ verified `app-ads.txt`;
- production ads only on registered test devices during QA.

Setup details: `ADMOB_SETUP.md`.

## 4. Stop-ship findings (P0)

### P0.1 Backend authorization must be enforced centrally

`goals/save`, `goals/<id>/saved`, categories save/delete/move, transaction save, এবং record delete-এর মতো কয়েকটি mutation route explicit `pocket_native_current_auth()` check করে না। `pocket_native_db()` unauthenticated অবস্থায় default database path ব্যবহার করতে পারে। Code inspection অনুযায়ী এটি cross-tenant/default-database mutation risk; live exploit test করা হয়নি।

Fix:

1. Every `/api/pocket/native/*` route-এর জন্য central `@require_pocket_native_auth` decorator.
2. Guest mode-কে dedicated guest tenant/database-এ isolate করা.
3. Every update/delete query-তে tenant/user ownership condition.
4. Unauthorized request-এ deterministic `401`; forbidden ownership-এ `403`.
5. Contract tests: unauthenticated, wrong tenant, inactive user, deleted session.

### P0.2 No automated tests

`app/src/test` বা `app/src/androidTest`-এ test source পাওয়া যায়নি; Gradle result `NO-SOURCE`. Money calculations, debt settlement, reminder recurrence, backup restore, auth isolation—কোনোটির regression protection নেই।

Build verification is green, but `testDebugUnitTest NO-SOURCE` means “no test failed,” not “the logic is tested.”

Minimum release gate:

- domain unit tests;
- repository/API contract tests;
- Compose UI tests for critical flows;
- backend pytest suite with isolated temporary DB;
- one end-to-end smoke suite against staging.

### P0.3 Uploaded Play draft is obsolete

আগে upload করা version `2026101901 (1.1.0)`-এ paid/billing model ছিল। বর্তমান free/ad code সেই AAB-তে নেই। Draft release submit করা যাবে না; নতুন version code সহ নতুন AAB দরকার।

### P0.4 Ads change policy declarations

Google Mobile Ads SDK device/ad identifiers ও diagnostics ব্যবহার করতে পারে। তাই আগের “no data collected/shared” ধরনের declaration থাকলে তা পুনরায় evaluate না করে release করা যাবে না। Consent flow-এর আগে production ad initialization করা যাবে না যেখানে আইন/Google policy প্রযোজ্য।

## 5. UI/UX deep audit

### Navigation and layout

- Floating bottom bar visually dominant এবং content-এর ওপর বসে; several screens-এর last rows/cards partially hidden.
- Screen-level bottom padding inconsistent: Home `264dp`, others `176dp`, shell আবার bottom inset ignore করে.
- Back pill + bottom navigation একই hierarchy-তে redundant navigation তৈরি করে.
- Fix: standard `NavigationBar`, real Scaffold inner padding, only central Add FAB, consistent 80–96dp bottom safe area.

### Home/dashboard

- Good: balance, due, today metrics, quick actions clear.
- Risk: all cards oversized; important “Add entry” one thumb action হলেও screen নিচে.
- Change: compact balance card; three primary actions only (`Add`, `Records`, `Plans`); secondary tools in More; user-customizable dashboard.
- Empty/zero data-তে guidance: “Add first expense”, sample walkthrough, no fake analytics.

### Quick Add

- Rename with direction:
  - `Give Loan` → `I lent money`
  - `Get Back` → `Money returned to me`
  - `Take Loan` → `I borrowed money`
  - `Pay Back` → `I repaid a loan`
- Add icon + one-line helper; remember last-used action; offer recent amount/category chips.
- On low-height devices use bottom sheet with scroll, not oversized modal.

### Transaction form

- Visible wrap bug: `Description` breaks into two lines.
- Save must be disabled until amount > 0 and required fields valid.
- `Note` vs `Description` overlaps conceptually; merge into one optional note unless distinct backend semantics exist.
- Amount should be first, large, currency-aware; numeric keypad action should move/submit safely.
- Inline validation; preserve draft on back; idempotency key prevents duplicate submission.
- Date display local-friendly (`20 Oct 2026`) while API keeps ISO.

### Records

- Reduce month title height; sticky filter/search row.
- Add transaction-type chips, account/category filters, clear active-filter count.
- Empty state should include `Add entry` CTA.
- Swipe actions should require confirmation/undo, especially delete.

### Reports

- Remove paywall language (done locally).
- Use one report hierarchy: Overview → Category → Cash flow → Trends.
- Charts require labeled axes, accessible summaries, empty states, and exact totals.
- PDF export should show period/filter before export and verify generated document.

### More/settings

- Group into: Money tools, Data & security, Preferences, Help & legal.
- Search, Reports, Accounts already exist in bottom/home; avoid duplicate primary destinations.
- Language UI says Bangla/Hindi/Urdu/Arabic, but hardcoded English strings mean selection may not fully localize. Move all copy to `strings.xml` and ship complete locale packs.

### Accessibility

- Many decorative icons have `contentDescription = null`; acceptable only when adjacent text fully labels the action. Icon-only buttons need semantic labels.
- Verify TalkBack order, 200% font, bold text, RTL, switch labels, color contrast, keyboard focus, and 48dp targets.
- Do not encode income/expense/status only by green/pink/orange; pair with text/icons.

## 6. Logo, icon and visual identity

Current launcher asset is one legacy vector drawable: a blue/dark wallet with a small white dot. The same drawable is used for normal and round icon.

Issues:

- no Android adaptive icon foreground/background layers;
- no Android 13 themed monochrome icon;
- wallet silhouette is generic and hard to distinguish at 48dp;
- in-app avatar `PG` does not reinforce the wallet mark;
- visual system mixes bright blue, pink, orange, green, purple with equal emphasis.

Recommended identity:

- Core mark: a simple “P” made from a pocket/wallet fold, one distinctive notch, no coin/detail smaller than 6–8% of icon width.
- Primary palette: deep navy + electric blue; green only for positive money, red/pink only for expense/risk, orange for due/reminder.
- Deliverables: master SVG, adaptive foreground/background, monochrome drawable, 512×512 Play icon, notification icon, wordmark, dark/light variants.
- Validate at 16, 24, 32, 48, 72, 192 and 512px; run one Play Store icon experiment at a time.

## 7. Frontend/Android engineering audit

Codebase: ~24,939 Kotlin lines.

Largest hotspots:

- `BudgetNativeScreen.kt`: 3,696 lines
- `PocketNativeRepository.kt`: 2,475 lines
- `PocketProNativeApp.kt`: 1,955 lines
- `AddTransactionNativeScreen.kt`: 1,765 lines
- `HomeNativeScreen.kt`: 1,400 lines
- `GoalsNativeScreen.kt`: 1,157 lines
- `PocketNativePreferencesStore.kt`: 1,056 lines

Risks:

- UI, navigation, state, business rules and side effects are mixed.
- Repository uses many `runCatching` paths and string errors; error semantics are inconsistent.
- SharedPreferences/Gson acts as a broad local database/cache/queue.
- no dependency injection or clear domain layer.
- local fallback and remote synchronization are ad hoc rather than a tested state machine.
- production minification/resource shrinking are disabled.
- broad `MultiDexApplication` usage should be re-evaluated for minSdk 24.

Target architecture:

```text
Compose screen
  → ViewModel + immutable UiState
    → Use case (transaction, budget, loan, reminder)
      → Repository interface
        → RemoteDataSource (Retrofit)
        → LocalDataSource (Room/DataStore)
        → SyncEngine (WorkManager + idempotency)
```

Rules:

- one screen file ideally <500–700 lines;
- one source of truth (Room) for finance data;
- typed `Result/Error` model;
- no Context inside domain/data models;
- unidirectional data flow;
- route arguments typed and restorable;
- feature modules after behavior is covered by tests.

Google’s current architecture guidance also recommends clear UI/data layers, repositories, coroutines/flows, UDF, testing, R8 and performance profiles: <https://developer.android.com/topic/architecture/recommendations>.

## 8. Data, security and backend audit

Backend hotspot: one `app.py` file is ~19,802 lines with 134 routes and no test suite found.

High-priority changes:

- split auth, pocket API, admin, billing/legacy, jobs, and DB migrations into blueprints/modules;
- central auth middleware and tenant-bound DB access;
- schema migrations with versioned tool, not runtime `ALTER/ensure` calls during requests;
- request validation with typed schemas;
- consistent error codes and correlation IDs;
- per-account and per-IP login/mutation rate limits;
- audit log for login, export, delete, restore, and security settings;
- secure cookie must be on in production; current env default appears false;
- encrypt Android session/token at rest; current CookieJar serializes cookie values into plaintext SharedPreferences;
- rotate sessions after login and privilege change;
- protect backup with encryption and integrity signature;
- database backup/restore drills and point-in-time recovery.

## 9. Core logic test matrix

### Transactions

- reject zero/negative/NaN/overflow amounts;
- currency precision uses decimal/minor units, not floating-point for ledger truth;
- save retry cannot duplicate a record;
- edit preserves audit history;
- delete is tenant-owned, reversible/soft-delete where appropriate;
- totals equal sum of visible records under every filter.

### Loans

- `0 ≤ settled ≤ original`;
- partial receive/repay updates remaining exactly once;
- overpayment blocked or explicitly recorded;
- direction and cash-flow signs consistent;
- closed loan cannot be settled again.

### Budgets/installments/reminders

- month boundary, leap year, day 29–31 policy;
- Asia/Dhaka timezone and device-time changes;
- notification permission denied/revoked;
- reboot rescheduling;
- “mark paid” advances one cycle only;
- reminder edit/delete cancels old scheduled work.

### Goals

- target > 0; saved never silently exceeds target;
- concurrent quick-add is serialized/idempotent;
- progress uses exact decimal arithmetic;
- archived/completed goal is immutable unless reopened.

### Offline/sync

- airplane-mode create/edit/delete;
- process death mid-sync;
- duplicate retry;
- server conflict resolution;
- logout clears tenant-scoped cache;
- two accounts on one device never see each other’s data.

### Backup/restore

- schema version + checksum;
- corrupted/partial/oversized file rejection;
- restore preview and confirmation;
- no session cookies/secrets in export;
- rollback on partial restore failure.

## 10. Features to add—ordered, not all at once

### P1: makes the daily product better

- recurring transaction creation/editing with preview;
- CSV import with mapping and duplicate detection;
- account reconciliation and opening balance correction;
- subscription/bill detector from user-entered recurrence;
- biometric app lock backed by Android Keystore;
- widgets: balance, due today, quick add;
- fully localized Bangla + BDT formatting;
- undo after delete/mark-paid.

### P2: retention and insight

- weekly digest and monthly close;
- cash-flow forecast based on recurring items;
- smart category rules;
- unusual-spend alert with explainable logic;
- customizable dashboard;
- family/shared wallet only after permissions and audit model exist.

### P3: growth

- referral/deep-link onboarding without exposing finance data;
- shareable, privacy-safe monthly summary image;
- contextual Play review prompt after a successful milestone, never after an error;
- localized Play listings and screenshot experiments.

Avoid bank sync/OCR/AI advice until privacy, consent, data accuracy, and support capacity are ready.

## 11. Free + ads monetization design

Principle: feature access stays free; ads must not interrupt money entry or damage trust.

Recommended placements:

- one inline adaptive banner after Home quick actions (implemented with test ID);
- optional inline banner after a Reports section or long Records list, only if scroll depth supports it;
- no ad on login/register, transaction form, app lock, backup/restore, account deletion, or error dialog;
- no interstitial on app open, Save, Back, or delete;
- no deceptive ad that looks like an app action;
- frequency caps and remote kill switch.

Revenue should be optimized by retention and healthy session depth, not ad density. Google requires test ads during development and a real AdMob App ID in production: <https://developers.google.com/admob/android/quick-start>. Official banner guidance: <https://developers.google.com/admob/android/banner>. App-ads.txt guidance: <https://support.google.com/admob/answer/9787936>. Consent requirement: <https://support.google.com/admob/answer/13554020>.

## 12. Roadmap with release gates

### Phase 0 — Stop-ship stabilization (Week 1)

- Add backend auth/tenant guard to every native route.
- Add tests for auth isolation and all mutation routes.
- Remove all remaining dead premium/billing code names and stored entitlement migration.
- Fix zero amount Save, Description wrap, and bottom-nav overlap.
- Add real release version `1.2.0` with new version code only after tests.
- Reconcile Play Data Safety for ads.

Exit gate: zero known P0, all critical smoke tests green, no release submission yet.

### Phase 1 — Ledger correctness (Weeks 1–3)

- Introduce Money/Decimal domain type.
- Define transaction/loan/budget/goal invariants.
- Add idempotency keys and audit trail.
- Unit + backend contract tests for all calculations.
- Property-based tests for totals, partial settlement and recurrence.

Exit gate: business rules have deterministic tests and cannot silently duplicate or cross tenant.

### Phase 2 — Architecture and offline foundation (Weeks 3–6)

- Screen ViewModels + immutable states.
- Split repository by domain.
- Room as local source of truth; DataStore for preferences.
- WorkManager sync queue, retry/backoff, conflict policy.
- Encrypt session and sensitive local values.

Exit gate: offline create/edit/delete survives process death and resyncs exactly once.

### Phase 3 — UX and brand system (Weeks 2–6, parallel)

- Standard navigation/safe insets.
- Compact dashboard and simplified IA.
- Transaction form redesign and validation.
- Design tokens/component library.
- Adaptive/themed icon and consistent in-app brand.
- Bangla localization, dynamic text, TalkBack, RTL.

Exit gate: all critical screens pass phone-size, font-scale and accessibility QA.

### Phase 4 — Ads, privacy and observability (Weeks 4–7)

- Connect production AdMob IDs through secure CI values.
- Consent flow before production ad load.
- app-ads.txt and developer website verification.
- Privacy policy/Data Safety update.
- Privacy-safe crash monitoring and release health dashboard.
- Remote ad kill switch; placement/frequency analytics.

Exit gate: ads are policy-compliant, remotely disableable and never block core tasks.

### Phase 5 — Quality automation and performance (Weeks 5–9)

- CI: format, lint, unit, backend, Compose UI, signed bundle verification.
- screenshot regression tests for 320/360/412dp and large font.
- macrobenchmarks and Baseline/Startup Profiles.
- enable R8/minification after keep-rule verification.
- Play pre-launch report, internal → closed → 5% staged rollout.

Google notes Baseline Profiles can improve first-run code execution by about 30% for covered paths: <https://developer.android.com/topic/performance/baselineprofiles/overview>.

### Phase 6 — Retention and store growth (Weeks 8–14+)

- instrument funnel: install → setup → first entry → day-2 return → week-1 report;
- improve onboarding based on drop-off;
- weekly/monthly digest;
- review prompt after value moment;
- localized store assets;
- Play Store listing experiments, one asset at a time: <https://support.google.com/googleplay/android-developer/answer/12053285>.

Exit gate: each release has a hypothesis, metric, rollout guard and rollback rule.

## 13. Success metrics

Reliability:

- crash-free users ≥ 99.9%;
- ANR-free users ≥ 99.9%;
- zero confirmed cross-tenant access;
- sync duplicate rate = 0;
- backup restore success ≥ 99.5% in controlled tests.

Product:

- first transaction completion rate;
- median time to add expense < 20 seconds;
- D1/D7/D30 retention;
- users creating at least one reminder/goal;
- report views per active user;
- support tickets per 1,000 active users.

Ads:

- banner match/fill/impression rate;
- revenue per daily active user;
- retention before/after placement;
- accidental-click signals;
- no increase in form abandonment or app-store complaints.

Growth:

- store listing visitor → install;
- install → first successful entry;
- review rating and review themes;
- organic install share;
- localized listing performance.

## 14. Release checklist for the next version

- [ ] Backend P0 auth fixed and tested.
- [ ] Zero-amount Save and layout blockers fixed.
- [ ] Real AdMob IDs supplied; test IDs absent from release requests.
- [ ] Consent and privacy disclosures implemented.
- [ ] Data Safety updated.
- [ ] app-ads.txt verified.
- [x] Version code bumped; clean signed AAB built.
- [ ] Unit/backend/UI smoke suites green.
- [ ] Play pre-launch report reviewed.
- [ ] Internal test completed on at least low-, mid-, and high-end phones.
- [ ] 5% staged rollout with crash/ANR/ad kill-switch monitoring.
- [ ] Rollback plan documented.

Current local candidate: `app/build/outputs/bundle/release/app-release.aab`  
SHA-256: `8fb5a4377a809184319c22745d40759b37062acf40907ccdcd07839aaed2ce30`  
Verified manifest: package `com.softx.app`, version `1.2.0`, code `2026102001`, target SDK 36. Do not upload/submit it as the revenue build until production AdMob IDs and the privacy/policy work are complete.

## 15. Audit limitations

- Emulator evidence used one Pixel 6 Pro API 33 profile in dark mode.
- No physical-device, foldable/tablet, TalkBack, 200% font, RTL, poor-network, battery optimization or OEM-specific testing was completed.
- Backend findings are static code inspection; no destructive live penetration testing was performed.
- Production AdMob account, app IDs, consent configuration and live revenue were not available.
- Current Play draft was not submitted or rolled out.

