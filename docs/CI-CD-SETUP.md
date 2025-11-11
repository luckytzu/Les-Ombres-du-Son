# 🚀 Guide de Configuration CI/CD - Les Ombres du Son

Ce guide vous accompagne dans la configuration complète de la CI/CD pour le projet.

## 📋 Table des matières

1. [Prérequis](#prérequis)
2. [Configuration SonarCloud](#1-configuration-sonarcloud)
3. [Configuration Firebase App Distribution](#2-configuration-firebase-app-distribution)
4. [Configuration des Secrets GitHub](#3-configuration-des-secrets-github)
5. [Test de la CI/CD](#4-test-de-la-cicd)
6. [Workflow de développement](#5-workflow-de-développement)
7. [Troubleshooting](#6-troubleshooting)

---

## Prérequis

- ✅ Compte GitHub avec accès au repository
- ✅ Projet Firebase configuré
- ✅ Fichier `google-services.json` disponible
- ⏳ Compte SonarCloud (gratuit pour projets open source)
- ⏳ Firebase CLI installé (optionnel)

---

## 1. Configuration SonarCloud

### Étape 1.1 : Créer un compte SonarCloud

1. Allez sur [sonarcloud.io](https://sonarcloud.io)
2. Cliquez sur **"Sign up"**
3. Connectez-vous avec votre compte GitHub
4. Autorisez SonarCloud à accéder à vos repositories

### Étape 1.2 : Importer le projet

1. Cliquez sur **"+"** → **"Analyze new project"**
2. Sélectionnez **"luckytzu/Les-Ombres-du-Son"**
3. Choisissez **"With GitHub Actions"**
4. Copiez le **SONAR_TOKEN** généré

### Étape 1.3 : Vérifier la configuration

Le fichier `sonar-project.properties` est déjà configuré avec :
- `sonar.projectKey=luckytzu_Les-Ombres-du-Son`
- `sonar.organization=luckytzu`

⚠️ **Important** : Vérifiez que `sonar.organization` correspond à votre organisation SonarCloud.

---

## 2. Configuration Firebase App Distribution

### Étape 2.1 : Activer Firebase App Distribution

1. Allez sur [Firebase Console](https://console.firebase.google.com)
2. Sélectionnez votre projet
3. Dans le menu latéral, cliquez sur **"App Distribution"**
4. Cliquez sur **"Get started"**

### Étape 2.2 : Ajouter votre application Android

1. Cliquez sur **"Add app"** → **"Android"**
2. Package name : `fr.upjv.lesombresduson`
3. Téléchargez le fichier `google-services.json` (si pas déjà fait)

### Étape 2.3 : Créer les groupes de testeurs

#### Groupe "testers" (pour develop)
1. Allez dans **"Testers & Groups"**
2. Cliquez sur **"Add group"**
3. Nom : `testers`
4. Ajoutez les emails des testeurs internes

#### Groupe "beta" (pour main)
1. Créez un second groupe
2. Nom : `beta`
3. Ajoutez les emails des beta-testeurs externes

### Étape 2.4 : Récupérer l'App ID

1. Dans Firebase Console, allez dans **"Project settings"** ⚙️
2. Sélectionnez votre app Android
3. Copiez l'**App ID** (format : `1:123456789:android:abc123...`)

### Étape 2.5 : Générer le Service Account

```bash
# Installer Firebase CLI
npm install -g firebase-tools

# Se connecter
firebase login

# Générer le token
firebase login:ci
```

Copiez le token généré.

**Alternative** : Créer un Service Account JSON
1. Allez dans [Google Cloud Console](https://console.cloud.google.com)
2. Sélectionnez votre projet Firebase
3. **IAM & Admin** → **Service Accounts**
4. Créez un compte de service avec le rôle **"Firebase App Distribution Admin"**
5. Créez une clé JSON et téléchargez-la

---

## 3. Configuration des Secrets GitHub

### Étape 3.1 : Accéder aux secrets

1. Allez sur GitHub : `https://github.com/luckytzu/Les-Ombres-du-Son`
2. Cliquez sur **"Settings"** → **"Secrets and variables"** → **"Actions"**
3. Cliquez sur **"New repository secret"**

### Étape 3.2 : Ajouter les secrets

#### Secret 1 : `GOOGLE_SERVICES_JSON`
```bash
# Convertir le fichier en base64
cat app/google-services.json | base64 -w 0
```
- Nom : `GOOGLE_SERVICES_JSON`
- Valeur : Collez le contenu base64

#### Secret 2 : `SONAR_TOKEN`
- Nom : `SONAR_TOKEN`
- Valeur : Token copié depuis SonarCloud (étape 1.2)

#### Secret 3 : `FIREBASE_APP_ID`
- Nom : `FIREBASE_APP_ID`
- Valeur : App ID copié depuis Firebase (étape 2.4)

#### Secret 4 : `FIREBASE_SERVICE_ACCOUNT`
- Nom : `FIREBASE_SERVICE_ACCOUNT`
- Valeur : Contenu du fichier JSON du service account OU token Firebase CLI

### Étape 3.3 : Vérifier les secrets

Vous devriez avoir 4 secrets configurés :
- ✅ `GOOGLE_SERVICES_JSON`
- ✅ `SONAR_TOKEN`
- ✅ `FIREBASE_APP_ID`
- ✅ `FIREBASE_SERVICE_ACCOUNT`

---

## 4. Test de la CI/CD

### Test 1 : Feature Branch

```bash
# Créer une branche feature
git checkout develop
git pull
git checkout -b feature/test-cicd

# Faire un changement
echo "# Test CI/CD" >> README.md
git add README.md
git commit -m "test: vérification CI/CD"
git push origin feature/test-cicd
```

**Résultat attendu** :
- ✅ Workflow `CI - Feature Branches` se lance
- ✅ Lint + Tests + SonarCloud
- ✅ Build APK debug
- ❌ Pas de distribution Firebase

### Test 2 : Pull Request vers develop

```bash
# Créer une PR sur GitHub
# feature/test-cicd → develop
```

**Résultat attendu** :
- ✅ Tous les checks de feature
- ✅ Reviewdog commente les problèmes de code
- ✅ SonarCloud décore la PR
- ✅ Approbation requise (@luckytzu ou @InsightSeeker-dev)

### Test 3 : Merge vers develop

```bash
# Merger la PR sur GitHub
```

**Résultat attendu** :
- ✅ Workflow `CI/CD - Develop` se lance
- ✅ Build APK debug
- ✅ 🚀 Distribution Firebase au groupe "testers"
- ✅ Notification envoyée aux testeurs

### Test 4 : Push vers main

```bash
# Merger develop vers main
git checkout main
git pull
git merge develop
git push origin main
```

**Résultat attendu** :
- ✅ Workflow `CI/CD - Production` se lance
- ✅ Build APK release
- ✅ 🚀 Distribution Firebase au groupe "beta"

---

## 5. Workflow de développement

### Créer une nouvelle fonctionnalité

```bash
# 1. Partir de develop
git checkout develop
git pull

# 2. Créer une branche feature
git checkout -b feature/nom-fonctionnalite

# 3. Développer et commiter
git add .
git commit -m "feat: description"

# 4. Pousser et créer une PR
git push origin feature/nom-fonctionnalite
```

### Conventions de commit

Utilisez [Conventional Commits](https://www.conventionalcommits.org/) :

- `feat:` Nouvelle fonctionnalité
- `fix:` Correction de bug
- `docs:` Documentation
- `style:` Formatage
- `refactor:` Refactoring
- `test:` Ajout de tests
- `chore:` Tâches de maintenance

### Cycle de release

```
feature/* → develop → main
    ↓          ↓        ↓
   CI      CI + 🚀    CI + 🚀
           testers    beta
```

---

## 6. Troubleshooting

### Erreur : "google-services.json not found"

**Solution** :
```bash
# Vérifier que le secret est bien configuré
# Reconvertir le fichier en base64
cat app/google-services.json | base64 -w 0
# Mettre à jour le secret GOOGLE_SERVICES_JSON
```

### Erreur : "SonarCloud analysis failed"

**Solution** :
1. Vérifier que le token `SONAR_TOKEN` est valide
2. Vérifier que l'organisation dans `sonar-project.properties` est correcte
3. Vérifier que le projet est bien importé sur SonarCloud

### Erreur : "Firebase App Distribution failed"

**Solution** :
1. Vérifier que `FIREBASE_APP_ID` est correct
2. Vérifier que le service account a les permissions nécessaires
3. Vérifier que les groupes "testers" et "beta" existent

### Erreur : "Tests failed"

**Solution** :
```bash
# Lancer les tests localement
./gradlew testDebugUnitTest

# Voir les rapports
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Erreur : "Build failed"

**Solution** :
```bash
# Nettoyer et rebuilder
./gradlew clean
./gradlew assembleDebug
```

---

## 📊 Dashboards

### SonarCloud
- URL : `https://sonarcloud.io/project/overview?id=luckytzu_Les-Ombres-du-Son`
- Métriques : Bugs, Code Smells, Coverage, Duplications

### Firebase App Distribution
- URL : `https://console.firebase.google.com/project/[PROJECT_ID]/appdistribution`
- Releases, Testeurs, Feedback

### GitHub Actions
- URL : `https://github.com/luckytzu/Les-Ombres-du-Son/actions`
- Historique des builds, Logs

---

## 🎯 Prochaines étapes

### Court terme
- [ ] Créer des tests unitaires
- [ ] Améliorer le code coverage (objectif : 50%+)
- [ ] Ajouter des tests instrumentés

### Moyen terme
- [ ] Créer un keystore pour signer les APK
- [ ] Configurer la signature automatique
- [ ] Créer un compte Google Play Developer

### Long terme
- [ ] Déploiement automatique sur Google Play Store
- [ ] Intégration de tests E2E (Espresso)
- [ ] Monitoring avec Firebase Crashlytics

---

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/luckytzu/Les-Ombres-du-Son/issues)
- **Discussions** : [GitHub Discussions](https://github.com/luckytzu/Les-Ombres-du-Son/discussions)
- **Maintainers** : @luckytzu, @InsightSeeker-dev

---

**✨ La CI/CD est maintenant configurée ! Bon développement ! 🚀**
