# CampusCue

CampusCue is a fresh Android study-planning app for the RevenueCat Shipaton 2026 **Next Gen Award**. It turns a student's scattered campus tasks into a calm, prioritized focus plan.

## What is new

This project is event-specific and independent from the user's earlier hackathon projects. It has its own package, UI, repository plan, product story, and RevenueCat purchase flow.

## RevenueCat integration

CampusCue uses RevenueCat Android SDK `10.15.1` for:

- loading the current offering;
- purchasing the `CampusCue Plus` package;
- checking the `CampusCue Plus` entitlement;
- restoring purchases.

The app expects an offering with entitlement identifier `CampusCue Plus`. Configure a monthly package such as `campuscue_plus_monthly` in RevenueCat's Test Store before recording the demo.

The public SDK key is supplied at build time and is not stored in the repository:

```powershell
./gradlew assembleDebug -PREVENUECAT_PUBLIC_SDK_KEY="your_public_sdk_key"
```

Or put the property in a local `~/.gradle/gradle.properties` file. Never commit private RevenueCat secret keys.

## Build

Open the project in Android Studio with Android SDK 35 installed, or run:

```powershell
./gradlew assembleDebug -PREVENUECAT_PUBLIC_SDK_KEY="your_public_sdk_key"
```

The resulting debug APK is under `app/build/outputs/apk/debug/`.

## Demo flow

1. Launch CampusCue.
2. Show the Today tab and start a focus cue.
3. Show the Plan tab and mark a cue done.
4. Open Premium.
5. Load the RevenueCat Test Store offering.
6. Complete the sandbox purchase.
7. Show CampusCue Plus becoming active.
8. Show Restore purchases.

## Next Gen Award checklist

- [x] Android project with source code and instructions.
- [x] RevenueCat SDK integrated into a purchase and entitlement flow.
- [x] Open-source license included.
- [ ] Add the final public repository URL to Devpost.
- [ ] Record a public device demo under two minutes.
- [ ] Configure the RevenueCat Test Store product and public SDK key.
- [ ] Submit the new project through the RevenueCat Shipaton Devpost form.

The Next Gen Award does not require a paid Google Play listing, but the project still needs a working device demonstration and a public repository.
