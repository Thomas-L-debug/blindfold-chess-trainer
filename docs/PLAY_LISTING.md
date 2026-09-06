# Google Play listing copy

Paste into Play Console when the developer account is verified.  
UI language of the app is **English**; add **French** as a translation.

Play limits: title **30**, short description **80**, full description **4000**.

Privacy policy URL:

`https://github.com/Thomas-L-debug/blindfold-chess-trainer/blob/main/docs/PRIVACY.md`

Suggested category: **Games → Board**. Alternative: **Education**.  
Ads: **No**. Price: **Free**.

---

## English (default)

**App name (30):** Blindfold Chess Trainer  
(23 characters)

**Short description (80):** Train chess blindfold: squares, paths, classics, and an offline bot.  
(68 characters)

**Full description:**

```
Blindfold Chess Trainer is a calm Android app for mental chess visualization. No streaks, no ads, no accounts, no pressure.

You train at your own pace. Hide the board when you want to work from memory, or show it when you need a check.

Drills
• Find the Square — a coordinate is shown; tap that square.
• Name the Square — a square lights up; enter its name with the pad or your voice.
• Square Colors — is this square light or dark?
• Piece Path — move a bishop, knight, rook, or queen to the target with legal moves.
• Famous Games — replay a classic, one announced move at a time.
• Free Board — play any legal move; pad, tap, or speak.
• Play the Bot — a full game against Stockfish on your phone, at a rating you choose (about 1350–2500 Elo). Offline.

Voice (optional)
Speak moves or square names in French or English. Microphone permission is used only when you tap Speak. Recognition uses Android’s speech service; the app does not keep recordings.

The engine and chess rules run on the device. Free software under the GNU GPLv3 (Stockfish is included). Source, license, and privacy policy: github.com/Thomas-L-debug/blindfold-chess-trainer
```

---

## French (translation)

**Nom (30):** Blindfold Chess Trainer  
(23 characters — keep the English product name)

**Description courte (80):** Entraînement échecs à l'aveugle : cases, chemins, classiques, bot hors ligne.  
(77 characters)

**Description longue:**

```
Blindfold Chess Trainer est une application Android calme pour la visualisation mentale aux échecs. Pas de séries, pas de pubs, pas de comptes, pas de pression.

Tu t’entraînes à ton rythme. Cache le plateau pour travailler de mémoire, ou affiche-le pour vérifier.

Exercices
• Find the Square — une coordonnée s’affiche ; tape la case.
• Name the Square — une case s’allume ; entre son nom au pavé ou à la voix.
• Square Colors — cette case est-elle claire ou foncée ?
• Piece Path — amène un fou, cavalier, tour ou dame sur la cible avec des coups légaux.
• Famous Games — rejoue une partie classique, un coup annoncé à la fois.
• Free Board — joue n’importe quel coup légal ; pavé, tap ou voix.
• Play the Bot — une partie complète contre Stockfish sur le téléphone, au niveau que tu choisis (environ 1350–2500 Elo). Hors ligne.

Voix (facultatif)
Dicte des coups ou des cases en français ou en anglais. Le micro n’est utilisé que si tu appuies sur Speak. La reconnaissance passe par le service vocal d’Android ; l’app ne garde pas d’enregistrements.

Le moteur et les règles tournent sur l’appareil. Logiciel libre GNU GPLv3 (Stockfish inclus). Sources, licence et confidentialité : github.com/Thomas-L-debug/blindfold-chess-trainer
```

---

## Sideload testing (without Play)

This is for friends **now**, while Play identity is pending. It does **not** count toward Play’s 12 testers × 14 days. Those people must later install from a Play **closed testing** invite.

### You (Windows)

```powershell
cd D:\CodingProject\blindfold-chess-trainer
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew :app:assembleRelease
```

APK: `app\build\outputs\apk\release\app-release.apk`  
(signed with the upload keystore; do not send `keystore.properties`)

Send that file (Drive, USB, Signal, email). Not the `.aab`.

### Testers (phone)

1. Open the APK from the download.
2. Android will ask to allow installs from that app (Files / Drive / Chrome) → allow once.
3. Install. First launch: board hidden; Home → a drill.
4. Speak: grant microphone only if they want voice.

If install is blocked: Settings → Apps → special access → **Install unknown apps** → the app they used to open the APK.

### Before Play closed testing

Uninstall the sideloaded app first. Play-signed installs use Google’s signing key; a sideload signed with the **upload** key will **not** update in place. Same package name + different signature = conflict.

Debug APKs (`assembleDebug`) are fine for you; prefer **release** for others (closer to the store build).
