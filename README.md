# Jokino Jewels

A fully offline Android game in Kotlin + Jetpack Compose with two modes — a themed **Quiz** and **Matching Pairs** — set on a royal sapphire stage where a harlequin jester juggles gold-set rubies and sapphires.

- **applicationId / namespace:** `com.jokinojewels.game`
- **minSdk 24 · compileSdk 36 · targetSdk 36 · JDK 17**
- Portrait only, edge-to-edge, no permissions, no network.

---

## Features

**Quiz — "Round Wonders"** — circles, spheres and balls in **3 difficulty levels × 10 questions** (Easy · Medium · Hard), 4 options each, exactly one correct.
One question at a time, shown inside the gold-and-sapphire frame, with a progress bar and `n/10`. Tapping an answer turns the correct option green with **✓** and a wrong pick red with **✗**, then auto-advances after ~0.75 s. Best score per difficulty is saved. The quiz screen never scrolls: the frame and answer plates are sized from the real screen height, and any space left under the answers is filled by two jesters who jump for joy on a correct pick and droop on a wrong one. Pause overlay: Continue / Restart / Menu. Results panel: `Score: X/10`, Continue / Restart / Menu.

**Matching Pairs** — 9 levels of rising difficulty:

| Level | Pairs | Columns | Seconds |
|---|---|---|---|
| 1 | 2 | 2 | 60 |
| 2 | 3 | 2 | 70 |
| 3 | 4 | 2 | 70 |
| 4 | 6 | 3 | 80 |
| 5 | 6 | 3 | 80 |
| 6 | 8 | 4 | 100 |
| 7 | 8 | 4 | 100 |
| 8 | 10 | 4 | 110 |
| 9 | 12 | 4 | 120 |

The deck takes `pairs` icons out of 12, duplicates and shuffles them. Matches stay open; mismatches flip back after 0.7 s. The whole board always fits on screen without scrolling (card size is computed from the free width and height — 4 columns × 6 rows on level 9). Card backs are sapphire playing cards with a gold rim and the Jokino Jewels logo. A per-level countdown stops while paused. All pairs before time → **Well Done**; time out → **Out of Time**. Best matched pairs per level are saved; a level unlocks once the previous one is cleared (level 1 is always open).

**Screens:** Main · Grand Menu · Quiz difficulty select · Quiz · Quiz results · Levels (9 bubbles + Game Rules) · Pairs game · Pairs results · Results summary · Settings · Game Rules.

## Offline only

- The manifest declares **no `INTERNET` permission and no runtime permissions**; no services, WorkManager, alarms or notifications.
- No networking, ads, analytics, payments, maps, Firebase, attribution or tracking SDKs. No WebView.
- "Rate the Game" and the optional privacy URL hand a link to the Play Store / browser app via an `ACTION_VIEW` intent — the game itself never connects.
- Merged-manifest note: AndroidX Core adds its own signature-level `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` and ProfileInstaller declares a receiver guarded by `DUMP`. Neither is a runtime permission nor network access.

## Local storage & DataStore

One `GameRepository` (created once in `JokinoApp`) stores a single JSON string in **DataStore Preferences** under `game_data_json`:

```kotlin
@Serializable data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),   // categoryId -> best score
    val pairsBest: Map<String, Int> = emptyMap(),  // "1".."9"   -> best matched pairs
    val settings: Settings = Settings()
)
@Serializable data class Settings(val soundEnabled: Boolean = true)
```

`GameSerializer` (context-free, `ignoreUnknownKeys = true`, `coerceInputValues = true`) never throws: empty storage, missing keys, empty / corrupted / wrongly-typed JSON all decode to defaults; impossible values are filtered. A corrupted DataStore file is replaced by `ReplaceFileCorruptionHandler`, and IO errors on read emit defaults.

## Privacy

Nothing leaves the device. Backups and device transfers are excluded (`allowBackup="false"` + `data_extraction_rules.xml`). The in-app Privacy panel (Settings) shows the policy text from `AppConfig.PRIVACY_POLICY_TEXT`; set `AppConfig.PRIVACY_POLICY_URL` if Google Play needs a public URL — a button to open it appears automatically.

## Missing integrations (intentionally not included)

Internet access · accounts / sign-in · cloud save · leaderboards · ads · in-app purchases · analytics / crash reporting · push notifications · attribution / tracking SDKs (AppsFlyer, OneSignal, …) · Firebase · WebView · remote config · localisation (English only).

## Visual concept & unique layout

**"Sapphire Jester Theatre"** — a royal stage behind deep-blue velvet curtains; a harlequin jester in ruby-and-sapphire diamonds juggles gold-set jewels. The palette is sampled from the delivered art.

| Token | HEX | Usage |
|---|---|---|
| Midnight | `#020B2B` | window / splash background, deepest panel stop |
| Navy | `#0A1D5C` | surfaces, HUD pills, card-back bottom stop |
| SapphireDeep | `#1636A3` | panel top stop, card-back middle stop |
| Sapphire | `#2A57D8` | card face, secondary accent |
| SapphireBright | `#5B8CFF` | timer pill and bar |
| JesterGold | `#F6C344` | primary accent: borders, titles, progress, pips |
| GoldDeep | `#B8862B` | secondary borders, dividers |
| Ruby | `#D9263D` | urgency accent (always with the text "Hurry!") |
| RubyDeep | `#A3162A` | wrong answer fill (white text ≈ 7.6:1) |
| Emerald | `#2FBF71` | success accent |
| EmeraldDeep | `#1B7A45` | correct answer fill (white text ≈ 5.4:1) |
| Cream | `#FFF6DE` | body text on dark surfaces (≈ 15:1 on Navy) |

**Home screen — "the Jester's stage"** (not the usual mascot → title → stats card → button stack):
a crown-shaped arc of seven twinkling treasures hangs from the curtains; the logo hangs under it like a theatre sign and gently sways; two jesters enter from the left and right wings, partly off-stage, bobbing in turn; a "string of jewels" (9 diamond pips = Pairs levels cleared) leads into the ornate START plate.
The Grand Menu staggers its crested plates left/right like harlequin diamonds; the Levels screen is a zig-zag "jewel path" of sapphire level buttons; each screen features a different jester pose or treasure.

**Typography:** Material 3 with system fonts only — Serif bold for headings (gold, with a soft shadow in the top bar), default sans for body. No bundled fonts.

**Buttons:** one image plate with the label drawn on top in white bold — `btn_plate` (sapphire bar with gold frame and ruby side gems) everywhere, `btn_menu` (the same plate with the jester-cap crest) on Main and the Grand Menu, at 0.7 of screen width. Plates keep their aspect ratio (`Modifier.aspectRatio`); the label is centred on the blue bar (below the crest on `btn_menu`). Menus scroll when space runs out; game screens never scroll.
**Modal panels** (pause / results / rules / confirmations) are drawn in Compose — rounded rectangle, sapphire gradient, gold border — and grow with their content. The fixed-shape gold frame image is used only for the quiz question, where its aspect ratio is kept and the text is fitted inside.

**Accessibility:** ≥ 48 dp touch targets; content descriptions on image buttons, cards, pip rows and jesters on the home stage; quiz feedback uses ✓ / ✗ marks and "Correct / Wrong answer" state descriptions (colour is never the only cue); locked levels show a padlock and a "Locked" label; matched cards show a ✓ badge; the timer shows "Hurry!" in the last 10 s; text follows the system font scale (capped where it must fit a plate or frame); headings are marked for screen readers.

## App icon

Adaptive icon: the blue velvet stage from `background.jpg` as the background layer and the Jokino Jewels logo (jester + lettering) as the foreground, inside the safe zone. Includes a monochrome logo silhouette for Android 13 themed icons, legacy icons for API 24–25 made from the delivered `icon.jpg` in every density, and `store/ic_launcher_512.png` for the Play listing.

## Splash

`core-splashscreen` theme: Midnight `#020B2B` background with the Jokino Jewels logo (`splash_icon.png`, one ~150 KB PNG sized to sit inside the splash's circular icon area). It hands over to the Compose app instantly.

## Tech stack

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.12.01) · Material 3 · Navigation Compose 2.8.5 (type-safe `@Serializable` routes) · Lifecycle / ViewModel 2.8.7 · Coroutines 1.9.0 · Flow · DataStore Preferences 1.1.1 · Kotlinx Serialization 1.7.3 · AGP 8.7.3 · Gradle 8.9 · version catalog `gradle/libs.versions.toml`. Every imported package is a **direct** catalog dependency (including `compose-foundation`, `compose-animation`, `runtime-saveable`).

## Architecture

```
JokinoApp (Application)
 ├─ GameRepository  (DataStore, one JSON string)  ← GameSerializer (pure)
 └─ SoundManager    (SoundPool, synced to settings.soundEnabled at startup + live)

MainActivity → JokinoTheme → AppContainer (CompositionLocal) → AppNavHost
 Screens  ── collectAsStateWithLifecycle ──▶ ViewModels (immutable UiState via StateFlow)
   ProgressViewModel  (menus, levels, summary, settings)
   QuizViewModel      (one quiz run, SavedStateHandle)
   PairsViewModel     (one pairs run, SavedStateHandle: deck, matches, time)
 Pure logic: GameLogic (levels, deck building, unlock, completed count), QuizContent
```

Simple MVVM, no DI framework: repository and sound manager are created once in `JokinoApp` and passed to small `ViewModelProvider.Factory` classes.
**Safe navigation:** routes have defaulted args and are decoded with `safeRoute()` (never throws); every screen validates its args and shows a friendly fallback with Back for invalid / unknown / locked values; navigation only fires from a RESUMED entry (no double-tap crashes); results are saved before navigating; game state survives rotation and process recreation (restored paused).

## Placeholder assets (replaceable)

All images live in `app/src/main/res/drawable-nodpi` and are referenced only through `ui/Assets.kt`. They are produced from the artist's folder by:

```bash
pip install pillow imagequant
python3 tools/import_assets.py "/path/to/Jokino Jewels art"
```

The script trims margins, resizes, palette-compresses (libimagequant) and builds the plates, launcher layers, splash and store icon. Source → app mapping:

| App file | Source art | Used for |
|---|---|---|
| `bg_main.jpg` | background.jpg | every screen; adaptive-icon background |
| `logo.png` | logo.png | home stage, card backs, launcher foreground, splash |
| `btn_menu.png` | Сапфировая титульная табличка (full, with crest) | START + Grand Menu plates |
| `btn_plate.png` | same plate, crest removed | all other buttons, quiz answers |
| `frame_question.png` | Золотая рамка с сапфировой вставкой | quiz question frame |
| `plate_round.png` | Пустая сапфировая кнопка | level bubbles |
| `icon_back.png` / `icon_pause.png` | Сапфировая кнопка назад / Золотая кнопка паузы | top bar |
| `mascot_a` … `mascot_f` | the six jester poses | home stage, quiz cheering, menu, rules, levels, settings, results, fallback |
| `item_01` … `item_12` | crown, mask, heart box, chest, gift, goblet, key, hourglass, bell, harp, scepter, lyre | Pairs cards, home arc, timer pill |
| `deco_pendant` / `deco_fan` / `deco_cap` | pendant, fan, jester cap | Easy / Medium / Hard quiz icons (cap also on Settings) |
| `deco_medal` | medal | results + summary |
| `deco_feather` | feather | rules |
| `deco_ticket` | theatre ticket | Grand Menu |
| launcher, `store/ic_launcher_512.png` | icon.jpg (+ logo, background) | app icon |

Drop new art in with the same names — no code change. If proportions change, update the `*_ASPECT` constants in `Assets.kt`.
**Quiz questions** live in `data/QuizContent.kt` ("Round Wonders", 3 × 10); keep the `id`s (`round_easy`, `round_medium`, `round_hard`) stable — best scores are stored under them.
**Sound effects** are synthesized tones in `res/raw` (`click`, `correct`, `wrong`, `match`, `win`, `lose`) from `python3 tools/generate_sfx.py`; replace with your own short `.wav`/`.ogg` of the same names.

## Open in Android Studio

1. Android Studio Ladybug (2024.2) or newer → **Open** the project folder.
2. Settings → Build Tools → Gradle → **Gradle JDK = 17**.
3. SDK Manager: install **Android 16 (API 36)** platform and **Build-Tools 36.0.0**.
4. Sync. Studio downloads Gradle 8.9 from `gradle/wrapper/gradle-wrapper.properties`. To use `./gradlew` on the command line, generate the wrapper scripts once with a local Gradle: `gradle wrapper --gradle-version 8.9`.

### JDK 17 / API 36

The build targets JVM 17 (Java + Kotlin). AGP 8.7.x predates official API 36 support, so `gradle.properties` contains `android.suppressUnsupportedCompileSdk=36`; `buildToolsVersion` is pinned to 36.0.0.

### 16 KB memory page size

Pure Kotlin/Compose, **no native libraries** (`.so`), so the app is 16 KB page-size compatible. CI still runs `zipalign -c -P 16` on the APK; also check the AAB in Play Console → App bundle explorer.

## Builds

**Debug** (no signing needed):

```bash
gradle assembleDebug              # or ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
gradle testDebugUnitTest          # unit tests
```

**Non-minified release** (first release ships like this): `isMinifyEnabled = false`, `isShrinkResources = false`.

```bash
gradle assembleRelease bundleRelease
# → app/build/outputs/apk/release/app-release.apk
# → app/build/outputs/bundle/release/app-release.aab
```

A release task **fails immediately** with a clear message if any of the four signing env vars is missing — it never falls back to the debug key.

## Signing

### Generate a PKCS12 keystore

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore jokinojewels-release.p12 -alias jokinojewels \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Jokino Jewels, OU=Mobile, O=YourName, C=BY"
```

For PKCS12 the key password equals the keystore password. **Back up the `.p12` and passwords** — Play App Signing protects the app key, but losing the upload key means a reset request. Never commit it (`.gitignore` blocks `*.p12`, `*.jks`, `*.keystore`).

### Local signing

```bash
export ANDROID_KEYSTORE_PATH="/absolute/path/jokinojewels-release.p12"
export ANDROID_KEYSTORE_PASSWORD='…'
export ANDROID_KEY_ALIAS='jokinojewels'
export ANDROID_KEY_PASSWORD='…'
gradle assembleRelease bundleRelease
```

### GitHub Secrets

Repository → Settings → Secrets and variables → Actions → *New repository secret*:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 jokinojewels-release.p12` (macOS: `base64 -i jokinojewels-release.p12`) |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `jokinojewels` |
| `ANDROID_KEY_PASSWORD` | key password (same as keystore password) |

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and on manual dispatch:

1. JDK 17 (Temurin), Android SDK Platform 36 + Build-Tools 36.0.0.
2. Gradle **8.9** via `gradle/actions/setup-gradle` (fixed version — no committed wrapper JAR needed).
3. Checks all four secrets exist, decodes `ANDROID_KEYSTORE_BASE64` into `$RUNNER_TEMP` (never printed).
4. `testDebugUnitTest`, then signed `assembleRelease` + `bundleRelease`.
5. `apksigner verify --print-certs` on the APK — **fails** if verification fails or the cert contains `CN=Android Debug`; `jarsigner` + `keytool -printcert` do the same for the AAB.
6. `zipalign -c -P 16` 16 KB alignment check.
7. Uploads **`jokino-jewels-release-apk`** (test artifact, sideload only) and **`jokino-jewels-release-aab`** (the Google Play artifact).

No emulator smoke test is required. **A green CI build is not proof the app launches** — always verify on a device.

### apksigner

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --verbose --print-certs app-release.apk
```

Expect `Verified using v2 scheme: true` and your own `CN=` — never `CN=Android Debug`.

### APK vs AAB

- **AAB** (`app-release.aab`) — the **only** file you upload to Google Play.
- **APK** (`app-release.apk`) — for local testing / sideloading only.

## Staged R8

1. Ship and verify the non-minified release first (current config).
2. Then set in `app/build.gradle.kts`:
   ```kotlin
   isMinifyEnabled = true
   isShrinkResources = true
   ```
   `proguard-rules.pro` already keeps Kotlinx-Serialization serializers, companions and `@Serializable` classes (GameData, Settings and the type-safe navigation routes).
3. Rebuild and repeat the full local verification below before uploading.

## Local release verification (checklist)

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat --pid=$(adb shell pidof -s com.jokinojewels.game)
```

- [ ] Splash shows the midnight background + logo, then the Jester's stage.
- [ ] Start → Grand Menu; system Back returns correctly everywhere; no blank screens.
- [ ] Quiz (Easy / Medium / Hard): no scrolling on a small phone, jesters under the answers, green ✓ / red ✗ highlight, auto-advance, Pause → Continue / Restart / Menu, results `Score: X/10`, best saved after app restart.
- [ ] Pairs level 9 (24 cards) fits the screen without scrolling; level 1 win → "Well Done", level 2 unlocks; let a timer run out → "Out of Time".
- [ ] Timer stops while paused and when the app goes to background.
- [ ] Rotate / dark-mode toggle / "Don't keep activities" — game restores without crashing.
- [ ] Settings: sound toggle persists across restarts and mutes instantly; Reset asks for confirmation and locks levels 2–9 again.
- [ ] Airplane mode: everything works.
- [ ] `adb shell dumpsys package com.jokinojewels.game | grep permission` — no INTERNET.
- [ ] No `FATAL EXCEPTION` in logcat. Repeat the whole list after enabling R8.

## Data reset

In-app: Settings → **Reset Progress** (confirmation dialog; sound setting is kept). Or: `adb shell pm clear com.jokinojewels.game`, Android Settings → Apps → Jokino Jewels → Storage → Clear data, or uninstall.

## Known limitations

- English only.
- Portrait is locked; on Android 16+ large screens (≥ 600 dp) the system may ignore the orientation lock — the layouts are responsive and still work in landscape there.
- Pairs restore after process death resumes from the saved board (paused); the half-second mismatch animation in progress at that moment is skipped.
- Best Pairs result stores matched pairs only (not best time).
- AGP 8.7.x with compileSdk 36 relies on `android.suppressUnsupportedCompileSdk`; upgrade AGP when convenient.
