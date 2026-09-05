# Privacy policy — Blindfold Chess Trainer

**Last updated:** 5 September 2026

This policy describes how the Android app **Blindfold Chess Trainer** (`com.blindfoldchess.trainer`) handles information. The app is free software (GPLv3). It has **no backend**, **no user accounts**, and **no advertising or analytics SDKs**.

Public source: [https://github.com/Thomas-L-debug/blindfold-chess-trainer](https://github.com/Thomas-L-debug/blindfold-chess-trainer)

---

## What the app does *not* do

The app does **not**:

- create accounts or ask for your name, email, or phone number
- include ads, trackers, crash reporters, or analytics
- declare the `INTERNET` permission or contact our servers (there are none)
- store voice recordings, photos, contacts, or location
- sell data or use it for advertising

Chess rules and the Stockfish engine run **on the device**.

---

## Information that stays on the device

| Data | Why | Stored? |
|---|---|---|
| Voice language (French or English) | So Speak keeps the language you picked | Yes — Android `SharedPreferences` (`voice_input` / `language_tag`) |
| Board and drill UI state (show/hide board, coordinates, and similar) | So rotation or process death does not reset the session | In memory / Android saved instance state, not sent anywhere |
| Drill scores | Shown during a session | In memory only; they are not written to a database |

You can delete the language preference with **Android Settings → Apps → Blindfold Chess → Storage → Clear data**, or by uninstalling the app.

If Android Backup is enabled on the phone (`allowBackup`), Google may include that language preference in the device backup. The app does not control that system backup.

---

## Microphone and speech recognition

The app declares `RECORD_AUDIO` so you can **Speak** chess moves or square names (Free Board, Play the Bot, Piece Path, Name the Square).

- The microphone is used **only after you tap Speak** (and grant the permission).
- The app does **not** record in the background and does **not** keep audio files.
- Spoken audio is handed to the **Android `SpeechRecognizer`** provided by the device (often Google or the manufacturer). That service turns speech into text and returns the text to the app. The app then tries to parse it as a chess move or square.
- Depending on the device and whether an **offline speech pack** is installed, that service **may send audio over the network** to the provider of the recognizer. That processing is governed by **that provider’s** privacy policy (for example Google’s), not by a server we operate.
- The app may keep the **recognized text** of the last utterance in memory for the current screen (so you can see what was heard). It is not uploaded and is discarded when you leave the drill or the process is killed.

You can deny or later revoke microphone permission in Android settings. Voice input will stop; the rest of the app still works with the on-screen pad and board taps.

---

## Sharing

We (the app developers) do **not** share, sell, or rent your data.

When you use Speak, audio is processed by the **on-device or vendor speech service**, as described above. That is the only third-party processing involved, and only if you use voice input.

---

## Children

The app is a chess-training tool. It is **not directed at children under 13**. We do not knowingly collect personal information from children.

---

## Changes

If this policy changes, we will update this file in the public repository and the date at the top. Continued use of a new app version after that date means you accept the updated policy.

---

## Contact

Questions about this policy: open an issue on the public repository  
[https://github.com/Thomas-L-debug/blindfold-chess-trainer/issues](https://github.com/Thomas-L-debug/blindfold-chess-trainer/issues)

---

# Politique de confidentialité — Blindfold Chess Trainer

**Dernière mise à jour :** 5 septembre 2026

Cette politique décrit comment l’application Android **Blindfold Chess Trainer** (`com.blindfoldchess.trainer`) traite les informations. L’app est un logiciel libre (GPLv3). Elle n’a **pas de serveur**, **pas de comptes**, **pas de publicité ni de SDK d’analytics**.

Code source : [https://github.com/Thomas-L-debug/blindfold-chess-trainer](https://github.com/Thomas-L-debug/blindfold-chess-trainer)

---

## Ce que l’app ne fait *pas*

L’app **ne** :

- crée **pas** de compte et ne demande **pas** de nom, e-mail ou téléphone
- intègre **pas** de pubs, trackers, rapports de plantage ou analytics
- déclare **pas** la permission `INTERNET` et n’appelle **pas** nos serveurs (il n’y en a pas)
- stocke **pas** d’enregistrements vocaux, photos, contacts ou localisation
- revend **pas** de données et ne les utilise **pas** pour de la publicité

Les règles d’échecs et le moteur Stockfish tournent **sur l’appareil**.

---

## Informations qui restent sur l’appareil

| Donnée | Pourquoi | Stockée ? |
|---|---|---|
| Langue vocale (français ou anglais) | Pour que Speak garde la langue choisie | Oui — `SharedPreferences` Android (`voice_input` / `language_tag`) |
| État de l’interface (plateau affiché ou non, coordonnées, etc.) | Pour que la rotation ou un redémarrage du process ne perde pas la session | En mémoire / état Android, jamais envoyé |
| Scores des drills | Affichés pendant une session | En mémoire seulement ; pas de base de données |

Vous pouvez effacer la langue via **Paramètres Android → Applications → Blindfold Chess → Stockage → Effacer les données**, ou en désinstallant l’app.

Si la sauvegarde Android est activée (`allowBackup`), Google peut inclure cette préférence de langue dans la sauvegarde de l’appareil. L’app ne pilote pas cette sauvegarde système.

---

## Microphone et reconnaissance vocale

L’app déclare `RECORD_AUDIO` pour le bouton **Speak** (coups ou noms de cases : Free Board, Play the Bot, Piece Path, Name the Square).

- Le micro n’est utilisé **qu’après un appui sur Speak** (et l’accord de la permission).
- L’app n’enregistre **pas** en arrière-plan et ne conserve **pas** de fichiers audio.
- L’audio est transmis au **`SpeechRecognizer` Android** du téléphone (souvent Google ou le fabricant). Ce service renvoie du texte ; l’app essaie d’y lire un coup ou une case.
- Selon l’appareil et la présence d’un **pack vocal hors ligne**, ce service **peut envoyer l’audio sur Internet** vers le fournisseur de la reconnaissance. Ce traitement relève de **la politique de ce fournisseur** (par exemple Google), pas d’un serveur que nous opérons.
- L’app peut garder en mémoire le **texte reconnu** de la dernière phrase, le temps que l’écran est ouvert. Il n’est pas envoyé et disparaît en quittant le drill ou quand le process est tué.

Vous pouvez refuser ou retirer la permission micro dans les paramètres Android. La voix s’arrête ; le pavé et les taps sur l’échiquier restent disponibles.

---

## Partage

Nous (les développeurs de l’app) ne partageons, ne vendons et ne louons **pas** vos données.

Si vous utilisez Speak, l’audio est traité par le **service de reconnaissance du système ou du fabricant**, comme ci-dessus. C’est le seul traitement tiers, et uniquement si vous parlez.

---

## Enfants

L’app est un outil d’entraînement aux échecs. Elle **ne cible pas les enfants de moins de 13 ans**. Nous ne collectons pas sciemment de données personnelles d’enfants.

---

## Modifications

En cas de changement, ce fichier et la date en tête seront mis à jour dans le dépôt public. Continuer à utiliser une nouvelle version de l’app après cette date vaut acceptation de la politique mise à jour.

---

## Contact

Questions : ouvrir une issue sur le dépôt  
[https://github.com/Thomas-L-debug/blindfold-chess-trainer/issues](https://github.com/Thomas-L-debug/blindfold-chess-trainer/issues)
