# Blindfold Chess Trainer

Application Android calme pour s'entraîner aux échecs à l'aveugle — visualisation mentale pure, sans stress ni dark patterns.

**Dépôt privé** — Tous droits réservés.

Contexte complet : [`docs/PROJECT_CONTEXT.md`](docs/PROJECT_CONTEXT.md)

---

## Où en est le projet ?

| Élément | Statut |
|---|---|
| Projet Android (Kotlin + Jetpack Compose) | ✅ |
| Module `core:chess` (logique métier) | ✅ |
| Drill « Square Colors » (case claire ou foncée ?) | ✅ |
| Tests unitaires | ✅ |
| CI GitHub Actions | ✅ |
| Parties à l'aveugle / Stockfish / Room | ❌ pas encore |

**Ce que tu peux tester aujourd'hui :** l'app se lance, affiche un écran d'accueil, puis un drill où une case aléatoire (`e4`, `d5`…) s'affiche et tu dois répondre *Light* ou *Dark*. Un score local s'affiche, sans streak ni pression.

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

1. **Écran d'accueil** — titre « Blindfold Chess Trainer », fond sombre, carte « Square Colors ».
2. **Bouton « Start drill »** — ouvre le drill.
3. **Drill** — une case en grand (`g7`, `a1`…), deux boutons *Light* / *Dark*.
4. **Après ta réponse** — feedback discret (*Correct* ou *Not quite*), bouton *Next*, score `X / Y`.
5. **Bouton Back** — retour à l'accueil.

Pour valider mentalement : `a1` est **foncée**, `a2` est **claire**, `e4` est **claire**.

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
app/              → UI Compose (accueil, drills)
core/chess/       → logique échecs + tests unitaires
preview/          → aperçu web statique (navigateur)
scripts/          → utilitaires (droits build, etc.)
.github/workflows → CI (tests + APK debug)
docker-compose.yml
Dockerfile
```

---

## Prochaines étapes prévues

1. Drills supplémentaires (noms de cases, diagonales, tours de cavalier…)
2. Persistance des sessions (Room)
3. Parties à l'aveugle contre Stockfish