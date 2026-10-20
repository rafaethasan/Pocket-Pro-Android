# Pocket Pro AdMob configuration

Pocket Pro's paid feature gates and Play Billing integration have been removed. All app features are free.

Debug builds use Google's official test banner unit. Release builds do not request ads until these Gradle properties are supplied locally or by CI:

```properties
POCKET_ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
POCKET_ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
```

Before a production rollout:

1. Register `com.softx.app` in AdMob and complete app readiness review.
2. Add the production app and banner IDs as secure Gradle/CI properties.
3. Configure Google-certified consent messaging for the EEA, UK, and Switzerland before loading ads there.
4. Update Google Play Data safety and the privacy policy for advertising data collection/sharing.
5. Publish and verify `app-ads.txt` on the developer website linked from the Play listing.
6. Test only with Google's test ad unit or registered test devices before release.

Current placement is one adaptive banner on Home, after Quick actions. No ads are placed in transaction entry, app lock, backup/restore, authentication, or other sensitive flows.
