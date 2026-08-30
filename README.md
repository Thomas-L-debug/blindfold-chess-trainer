# Blindfold Chess Trainer

Application Android calme pour s'entraîner aux échecs à l'aveugle — visualisation mentale pure, sans stress ni dark patterns.

**Dépôt privé** — Tous droits réservés.

Contexte complet : [`docs/PROJECT_CONTEXT.md`](docs/PROJECT_CONTEXT.md)

---

## Où en est le projet ? (30 août 2026)

Workspace principal : `D:\CodingProject\blindfold-chess-trainer`  
Branche : `main` (dernier commit feature : *piece-path drill, board feedback, compact board chrome*).

| Élément | Statut | Notes |
|---|---|---|
| Projet Android (Kotlin + Jetpack Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, minSdk 26, targetSdk 35 |
| Module `core:chess` | ✅ | Cases, couleurs, pièces (B/N/R/Q), coups légaux plateau vide |
| Drill **Square Colors** | ✅ | Case aléatoire → Light / Dark, flash vert/rouge 0,5 s sur l'échiquier |
| Drill **Piece Path** | ✅ | Fou / cavalier / tour / dame, départ → arrivée, pavé a–h / 1–8 |
| Échiquier global (`AppShell`) | ✅ | Compact (hauteur = carré du plateau), pas la moitié de l'écran |
| Coordinates | ✅ | Rangs à gauche, files en bas ; l'échiquier ne saute pas |
| Arrows | ✅ | Flèches orange coucher de soleil + cercles sur les cases du parcours |
| Hide board | ✅ | Petit bouton **dans** le coin bas-droit de la zone échiquier |
| Show board | ✅ | Bouton pleine largeur seulement quand le plateau est masqué |
| Tests unitaires | ✅ | Couleurs, coups, reset Piece Path, flèches (`:core:chess:test` + `:app:testDebugUnitTest`) |
| CI GitHub Actions | ✅ | `.github/workflows/ci.yml` : tests + APK debug sur push/PR `main` |
| Pièces dessinées sur l'échiquier | ❌ | Plateau encore vide (flèches / surlignage seulement) |
| Room / Stockfish / puzzles | ❌ | pas encore |
| Preview web (`preview/`) | ⚠️ | Démo Square Colors seulement — **pas** à jour avec Piece Path ni le board |

**Ce que tu peux tester aujourd'hui :**

1. **Square Colors** — une case (`e4`, `d5`…) → *Light* / *Dark*. Bonne réponse : case en vert 0,5 s. Mauvaise : rouge 0,5 s. Score local, sans streak.
2. **Piece Path** — choisir une pièce, aller du départ à l'arrivée par des coups légaux (lettre puis chiffre). Coup illégal → message immédiat et **reset au départ** (même exercice). Les flèches + cercles suivent le trajet si **Arrows** est coché.

---

## Aperçu dans le navigateur (sans build Android)

L'app Android **ne tourne pas nativement dans un navigateur** (c'est du Kotlin/Compose, pas du web). En revanche, une **démo web statique** reproduit l'UI actuelle pour voir où on en est visuellement :

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
python3 -m http.server 8080 --directory preview
```

Puis ouvre : [http://localhost:8080](http://localhost:8080)

Tu peux aussi ouvrir directement `preview/index.html` dans Chrome/Firefox (double-clic). C'est la même logique de drill (*Square Colors*), mais ce n'est **pas** l'APK — juste un aperçu rapide.

Pour l'app réelle : Android Studio + émulateur, ou `./gradlew installDebug` sur un téléphone.

---

## Prérequis

Choisis **une** des deux approches ci-dessous.

### Option A — Gradle en local (recommandé si ça compile déjà)

- **JDK 21**
- **Android SDK** (API 35, build-tools 35.0.0) — installé via Android Studio ou `sdkmanager`
- Variable `ANDROID_HOME` pointant vers le SDK

Vérification rapide :

```bash
java -version          # doit afficher 21
echo $ANDROID_HOME     # ex. /home/thomas/Android/Sdk
./gradlew --version
```

### Option B — Docker (build reproductible, sans SDK local)

- Docker + Docker Compose
- Utile pour compiler et lancer les tests sans installer le SDK sur la machine

---

## Tester en local — étape par étape

### 1. Cloner et entrer dans le projet

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
```

### 2. Configurer le SDK Android (si pas déjà fait)

```bash
cp local.properties.example local.properties
# Édite local.properties et mets le bon chemin vers ton Android SDK
```

### 3. Vérifier que tout compile (sans téléphone)

```bash
chmod +x gradlew
./gradlew test assembleDebug
```

Si ça passe, tu obtiens l'APK ici :

```
app/build/outputs/apk/debug/app-debug.apk
```

C'est le test le plus rapide pour confirmer que le code actuel fonctionne sur ton PC.

### 4. Lancer le module `app` (l'application Android)

#### C'est quoi le « module app » ?

Ce projet contient **2 modules Gradle** :

| Module | Rôle | Tu le lances ? |
|---|---|---|
| **`app`** | L'application Android (écrans, boutons) | **Oui** — c'est celui-ci |
| **`core:chess`** | Bibliothèque de logique échecs (pas d'UI) | **Non** — juste une dépendance de `app` |

Quand on dit « Run sur le module app », ça veut dire : **compiler et installer l'APK sur un téléphone ou un émulateur**. Tu n'as rien à lancer sur `core:chess`.

---

#### Méthode 1 — Android Studio (recommandé sous WSL2)

**Où ouvrir le projet :** lance **Android Studio sur Windows** (pas dans le terminal WSL), puis ouvre le dossier du projet :

```
\\wsl$\Ubuntu\home\thomas\Projects\blindfold-chess-trainer
```

*(Adapte `Ubuntu` si ta distro WSL a un autre nom — visible dans l'explorateur Windows → « Linux ».)*

**Étapes une par une :**

1. **File → Open** → sélectionne le dossier `blindfold-chess-trainer` (celui qui contient `build.gradle.kts`).
2. Attends la fin du **Gradle Sync** (barre de progression en bas). Si Android Studio demande le SDK ou JDK 21, accepte les installs proposées.
3. En haut à droite, vérifie la barre d'outils :
   - Menu déroulant de config : choisis **`app`** (pas `core:chess`, pas un autre nom).
   - Menu déroulant d'appareil : choisis un **émulateur** ou un **téléphone branché en USB**.
4. Si aucun appareil n'apparaît :
   - **Tools → Device Manager → Create Device** (ex. Pixel 7, API 35).
   - Clique sur ▶ à côté de l'émulateur pour le démarrer.
5. Clique sur le bouton vert **Run ▶** (ou `Shift+F10`).

Android Studio exécute en interne l'équivalent de :

```bash
./gradlew :app:installDebug
```

et lance l'app sur l'appareil sélectionné.

**Tu sais que ça marche si** l'émulateur s'ouvre et affiche l'écran « Blindfold Chess Trainer ».

---

#### Méthode 2 — Ligne de commande (WSL ou terminal)

Il te faut un **appareil déjà visible** par `adb` avant d'installer.

```bash
cd /home/thomas/Projects/blindfold-chess-trainer

# 1) Vérifier qu'un téléphone/émulateur est connecté
adb devices
# Tu dois voir une ligne du type :
#   emulator-5554   device
# ou
#   XXXXXXXX        device
```

Si la liste est **vide** ou dit `unauthorized` :
- émulateur : démarre-le d'abord depuis Android Studio (Device Manager) ;
- téléphone : active **Options développeur → Débogage USB**, branche le câble, accepte la popup sur le téléphone.

```bash
# 2) Compiler + installer le module app sur l'appareil
./gradlew :app:installDebug

# 3) Ouvrir l'app (optionnel si installDebug ne l'a pas déjà lancée)
adb shell am start -n com.blindfoldchess.trainer/.MainActivity
```

> **Note :** `./gradlew installDebug` et `./gradlew :app:installDebug` font la même chose ici, car seul le module `app` est une application installable.

**Sans appareil connecté**, tu peux quand même **compiler** l'APK (mais pas l'installer) :

```bash
./gradlew :app:assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk
```

---

#### Méthode 3 — Pas d'Android Studio / pas d'émulateur : aperçu navigateur

Si tu veux juste **voir l'interface** sans configurer un émulateur :

```bash
python3 -m http.server 8080 --directory preview
```

→ [http://localhost:8080](http://localhost:8080) (démo web, pas l'app Android réelle).

---

#### WSL2 : pourquoi `adb devices` est souvent vide

Sous WSL2, l'émulateur tourne souvent **côté Windows**, alors que `adb` dans WSL ne le voit pas.

**Solutions :**
- **La plus simple :** tout faire via **Android Studio Windows** (méthode 1 ci-dessus).
- **Ou** utiliser `adb` Windows depuis PowerShell pour installer l'APK compilé dans WSL :
  ```powershell
  adb devices
  adb install \\wsl$\Ubuntu\home\thomas\Projects\blindfold-chess-trainer\app\build\outputs\apk\debug\app-debug.apk
  ```

### 5. Via Docker (build + tests uniquement)

```bash
# Première fois : construire l'image (peut prendre plusieurs minutes)
docker compose build

# Lancer les tests et compiler l'APK dans le conteneur
docker compose run --rm android
```

L'APK sera généré dans `app/build/outputs/apk/debug/` sur ton disque (volume monté).

> **Note :** l'émulateur Android dans Docker sous WSL2 est possible mais pénible à configurer. Pour tester l'UI, préfère Android Studio + émulateur ou un téléphone physique.

---

## Ce que tu dois voir dans l'app

1. **Écran d'accueil** — titre « Blindfold Chess Trainer », deux cartes : *Square Colors* et *Piece Path*.
2. **Échiquier en haut** — compact, options à droite : *Coordinates*, *Arrows*, et **Hide board** en bas de cette colonne.
3. **Square Colors** — case en grand, *Light* / *Dark*, flash de la case sur le plateau, *Next*, score `X / Y`.
4. **Piece Path** — sélecteur Bishop / Knight / Rook / Queen, `e2 → f4`, pavé 2×4 lettres puis 2×4 chiffres. Trajet en flèches si Arrows est coché.
5. **Back** — retour accueil.

Pour valider Square Colors : `a1` **foncée**, `a2` **claire**, `e4` **claire**.  
Pour valider Piece Path (tour) : même file ou même rang = légal ; sinon reset.

---

## Commandes utiles

```bash
# Tests uniquement
./gradlew test

# Recompiler l'APK debug
./gradlew assembleDebug

# Nettoyer et tout reconstruire
./gradlew clean test assembleDebug

# Voir les tâches Gradle disponibles
./gradlew tasks
```

---

## Dépannage courant

| Problème | Piste |
|---|---|
| `:app:processDebugResources FAILED` | Voir section ci-dessous |
| `SDK location not found` | Crée `local.properties` avec `sdk.dir=/chemin/vers/Android/Sdk` |
| `java: invalid target release: 21` | Installe JDK 21 et vérifie `JAVA_HOME` |
| `AccessDeniedException` dans `app/build` ou `core/chess/build` | Fichiers créés par Docker en `root` — voir section ci-dessous |
| `adb: command not found` | Installe `platform-tools` ou utilise Android Studio |
| `adb devices` vide sous WSL2 | Lance l'émulateur depuis Android Studio (Windows) ou branche un téléphone en USB |
| `Gradle JVM option is incorrect` (projet sur `\\wsl$\`) | Voir section ci-dessous |
| Build Docker très long la 1ʳᵉ fois | Normal — SDK et dépendances Gradle sont mis en cache dans les volumes Docker |

### Erreur Gradle JVM avec projet WSL (`\\wsl$\Ubuntu\...`)

Message typique :

> Gradle JVM option is incorrect: `C:\Program Files\Android\Android Studio\jbr`  
> The project is located on WSL. Use the JDK installed on the same WSL distribution.

**Pourquoi :** le projet est sur le disque Linux (WSL), mais Gradle est configuré pour utiliser le JDK **Windows** d'Android Studio. Il faut le JDK **Ubuntu**.

#### Fix (2 minutes) — pointer Gradle vers le JDK WSL

1. Dans Android Studio : **File → Settings** (ou **Ctrl+Alt+S**).
2. **Build, Execution, Deployment → Build Tools → Gradle**.
3. Champ **Gradle JDK** : ne choisis **pas** `jbr-21` avec un chemin `C:\Program Files\...`.
4. Sélectionne plutôt une entrée du type :
   - **`WSL: java-21-openjdk-amd64`** ou **`Ubuntu (WSL)`**
   - ou **Add JDK from disk…** puis navigue vers :
     ```
     \\wsl.localhost\Ubuntu\usr\lib\jvm\java-21-openjdk-amd64
     ```
     *(équivalent : `\\wsl$\Ubuntu\usr\lib\jvm\java-21-openjdk-amd64`)*
5. Clique **Apply → OK**.
6. **File → Sync Project with Gradle Files** (icône éléphant avec flèche).

Si le JDK WSL n'apparaît pas, installe-le dans Ubuntu :

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
/usr/lib/jvm/java-21-openjdk-amd64/bin/java -version
```

Puis refais l'étape 4 dans Android Studio.

#### Sync qui affiche juste « fail » sans détail

Android Studio masque souvent la vraie erreur. Dans les logs Windows, c'est en général :

> **Operation result has not been received**

La cause réelle (dans les logs Gradle WSL) est presque toujours :

> **sdk.dir = C:\Users\...\Android\Sdk** → Gradle Linux ne trouve pas ce chemin  
> ou **Build Tools 35.0.0 is corrupted** (SDK Windows `.exe` vu depuis Ubuntu)

**Pourquoi :** Android Studio **réécrit** `local.properties` avec le SDK Windows à chaque sync, alors que Gradle tourne dans **Ubuntu**.

**Fix automatique (déjà dans le projet) :** `settings.gradle.kts` corrige `local.properties` au démarrage de Gradle. Si ça bloque encore :

```bash
./scripts/fix-local-properties.sh
```

Puis **File → Sync Project with Gradle Files** dans Android Studio.

**Voir l'erreur cachée** (si « fail » sans texte) :
- Android Studio → **Help → Show Log in Explorer** → ouvre `idea.log`
- Cherche `Gradle sync failed` ou `MODEL_FETCH_FAILED`
- Ou dans Ubuntu : `grep -i "sync failed\|FAILURE\|corrupted" ~/.gradle/daemon/8.11.1/*.log | tail -5`

**Règle WSL :**

**Vérifier dans Ubuntu :**

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
./scripts/diagnose-sync.sh
```

**Corriger `local.properties`** — il doit pointer vers un SDK **Linux dans WSL** :

```properties
sdk.dir=/home/thomas/Android/Sdk
```

**Pas** vers `C:\Users\...` ni `/mnt/c/Users/...` (les build-tools Windows `.exe` sont « corrompus » vus depuis Gradle Linux).

Si le SDK WSL n'existe pas encore, installe-le une fois :

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
mkdir -p "$ANDROID_HOME"
yes | sdkmanager --sdk_root="$ANDROID_HOME" \
  "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

*(Remplace `sdkmanager` par son chemin si besoin, ex. `~/development/android/cmdline-tools/latest/bin/sdkmanager`.)*

Puis dans Android Studio : **File → Sync Project with Gradle Files**.

**Voir l'erreur réelle dans Android Studio** (quand le message est vague) :
- Onglet **Build** en bas → sous-onglet **Sync** ou **Build Output**
- Ou **View → Tool Windows → Build**
- La ligne rouge au-dessus de « fail » contient la vraie cause (`SDK location`, `Build Tools corrupted`, etc.)

#### Alternative plus simple — éviter WSL pour Android Studio

Si la config WSL te semble fragile, clone le projet sur le disque **Windows** :

```
C:\Users\<toi>\Projects\blindfold-chess-trainer
```

Ouvre **ce** dossier dans Android Studio. Le JDK par défaut (`jbr` d'Android Studio) fonctionnera sans réglage spécial. Tu peux garder une copie dans WSL pour le terminal/Docker si tu veux.

### Erreur `:app:processDebugResources FAILED`

Causes fréquentes :

1. **SDK manquant** — crée `local.properties` (voir `local.properties.example`).
2. **Dossiers `build/` verrouillés** — arrive souvent après `docker compose run` (fichiers owned par `root`). Symptôme : `AccessDeniedException` ou `Could not set file mode 755`.

   ```bash
   # Corrige en une commande (demande ton mot de passe sudo)
   sudo ./scripts/fix-build-permissions.sh

   # Puis relance
   ./gradlew test assembleDebug
   ```

3. **Cache Gradle corrompu** — en dernier recours :

   ```bash
   ./gradlew --stop
   rm -rf .gradle build app/build core/chess/build
   ./gradlew test assembleDebug --no-build-cache
   ```

   Si `rm -rf app/build` échoue avec « Permission denied », utilise d'abord le script `fix-build-permissions.sh`.

Exemple de `local.properties` (ne pas committer ce fichier) :

```properties
sdk.dir=/home/thomas/Android/Sdk
```

---

## Structure du projet

```
app/              → UI Compose (accueil, drills, échiquier)
core/chess/       → logique échecs + tests unitaires
preview/          → aperçu web statique (Square Colors seulement, pas l'app réelle)
scripts/          → utilitaires (SDK Windows/WSL, droits build)
.github/workflows → CI (tests + APK debug)
docs/             → PROJECT_CONTEXT.md (contexte session)
docker-compose.yml
Dockerfile
```

Fichiers UI / drills :

```
app/.../trainer/
├── MainActivity.kt                 # AppScreen : Home | SquareColor | PiecePath
├── feature/board/
│   ├── AppShell.kt                 # Board compact + contenu
│   ├── BoardPanel.kt               # Échiquier + Coordinates / Arrows / Hide
│   └── ChessBoard.kt               # Canvas 8×8, highlight, flèches, cercles
├── feature/home/HomeScreen.kt
└── feature/drills/
    ├── SquareColorDrillScreen.kt
    ├── SquareColorDrillViewModel.kt
    ├── PiecePathDrillScreen.kt     # pavé lettres / chiffres
    └── PiecePathDrillViewModel.kt

core/chess/.../
├── Square.kt / SquareColor.kt / SquareColorDrill.kt
├── PieceType.kt                    # canMove() plateau vide
└── PiecePathDrill.kt               # génération départ / arrivée
```

---

## Prochaines étapes — recommandations

Une feature à la fois. Pas de streaks, pas de dark patterns. `chesslib` est dans Gradle mais **pas encore utilisé**.

**Priorité (ordre conseillé)**

1. **Pièces sur l'échiquier** — dessiner au moins la pièce du drill Piece Path sur la case courante (et idéalement départ / arrivée). Sans ça, les flèches restent abstraites. Option toggle à côté de Coordinates / Arrows.
2. **Cible et départ marqués sur le plateau** (Piece Path) — même sans pièces : un marqueur discret sur la case de départ et d'arrivée pendant tout l'exercice.
3. **Drill « Square names »** — montrer une case (ou la pointer), le joueur la nomme avec le même pavé a–h / 1–8. Réutilise l'UI existante.
4. **Orientation blanc / noir** — retourner le plateau (utile dès qu'on visualise des trajets).
5. **Difficulté Piece Path** — limiter la distance (ex. cavalier 1–3 coups) pour que les puzzles restent jouables à l'aveugle.
6. **Room** — historique calme des sessions (scores, pas de streak). Seulement une fois qu'il y a 2–3 drills stables.
7. **Mettre à jour `preview/index.html`** — ou l'abandonner : il est en retard sur l'app réelle.

**Plus tard (pas maintenant)**

- Stockfish / parties à l'aveugle
- Puzzles tactiques
- Navigation Compose (l'enum `AppScreen` suffit pour 2–3 écrans)
- ktlint / detekt, upgrade AGP
- Saisie vocale
- iOS / KMP

**Règles session suivante**

- Travailler dans `D:\CodingProject\blindfold-chess-trainer`.
- Lire `docs/PROJECT_CONTEXT.md` en plus de ce README.
- Lancer `./gradlew :core:chess:test :app:testDebugUnitTest` après un changement de logique.
- Un drill ou un toggle board à la fois.