# Règles du Projet & Architecture

Ce projet est maintenu collaborativement par l'équipe (Axxio, Amara). Les règles suivantes sont absolues et s'appliquent à toute intervention sur le code :

## 1. Zéro Commentaire dans le Code Source Java
* Ne jamais ajouter de commentaires explicatifs dans les fichiers sources Java (`//`, `/* */`, ou Javadoc redondante). Le code doit être auto-descriptif, épuré et professionnel.

## 2. Messages de Commit en Français & Validation Préalable
* Tout commit doit obligatoirement être rédigé en **français**, préfixé selon la convention standard (`Feat: ...`, `Fix: ...`, `Chore: ...`).
* L'accord de l'utilisateur sur le message est obligatoire avant exécution du commit.

## 3. Accord Explicite pour `git push` & Tests Obligatoires
* Ne jamais lancer `git push` sans l'accord explicite et formel de l'utilisateur.
* Ne jamais pousser de code contenant des erreurs ou n'ayant pas validé la suite de tests unitaires (`./gradlew test`).

## 4. Architecture Multi-Branches
* **`dev` (Branche par défaut)** : Le chantier actif. Tous les développements, nouvelles fonctionnalités et Pull Requests ciblent `dev`.
* **Branches de versions Minecraft** : Branches stables réservées aux releases figées pour chaque version de Minecraft (ex: `1.21.11`). Zéro développement direct sur ces branches (uniquement des merges depuis `dev` ou des hotfixes validés).

## 5. Convention de Versionnage & Tags (« Format Slash »)
* Numéro de version dans `gradle.properties` : `mod_version = 3.1.0`.
* Fichier JAR produit : `francetiers_tagger-3.1.0.jar`.
* Tags Git de Release : **`<version_mc>/v<version_mod>`** (ex: `1.21.11/v3.1.0`).
* Aucun symbole `+` ni de suites de tirets redondantes.

## 6. GitHub Actions CI/CD
* Chaque push ou PR déclenche la compilation et les tests JUnit (`./gradlew check build`) avec badge ✅/❌.
* Seul le push d'un tag de release (`*/*`) déclenche la publication automatique de la Release GitHub avec le `.jar` attaché.
