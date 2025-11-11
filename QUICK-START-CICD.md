# 🚀 Quick Start - CI/CD

## ✅ Ce qui a été fait

La CI/CD complète a été mise en place avec :

### 📁 Fichiers créés
- ✅ `.github/workflows/ci-feature.yml` - CI pour les branches feature
- ✅ `.github/workflows/ci-develop.yml` - CI/CD pour develop + Firebase Distribution
- ✅ `.github/workflows/ci-main.yml` - CI/CD pour main + Release
- ✅ `.github/dependabot.yml` - Mises à jour automatiques des dépendances
- ✅ `.github/reviewdog.yml` - Configuration Reviewdog
- ✅ `sonar-project.properties` - Configuration SonarCloud
- ✅ `docs/CI-CD-SETUP.md` - Guide complet de configuration
- ✅ `docs/README.md` - Documentation du projet

### 🔧 Fichiers modifiés
- ✅ `app/build.gradle.kts` - Correction syntaxe + JaCoCo configuré
- ✅ `.gitignore` - Ajout google-services.json et fichiers sensibles

## 🎯 Prochaines étapes (OBLIGATOIRES)

### 1. Configurer SonarCloud (5 min)
1. Allez sur [sonarcloud.io](https://sonarcloud.io)
2. Connectez-vous avec GitHub
3. Importez le projet `luckytzu/Les-Ombres-du-Son`
4. Copiez le `SONAR_TOKEN`

### 2. Configurer Firebase App Distribution (10 min)
1. Allez sur [Firebase Console](https://console.firebase.google.com)
2. Activez "App Distribution"
3. Créez 2 groupes : `testers` et `beta`
4. Récupérez l'`App ID`
5. Générez un Service Account :
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase login:ci
   ```

### 3. Ajouter les Secrets GitHub (5 min)
Allez sur : `https://github.com/luckytzu/Les-Ombres-du-Son/settings/secrets/actions`

Ajoutez ces 4 secrets :

| Secret | Comment l'obtenir |
|--------|-------------------|
| `GOOGLE_SERVICES_JSON` | `cat app/google-services.json \| base64 -w 0` |
| `SONAR_TOKEN` | Copié depuis SonarCloud |
| `FIREBASE_APP_ID` | Copié depuis Firebase Console |
| `FIREBASE_SERVICE_ACCOUNT` | Token Firebase CLI ou JSON Service Account |

### 4. Tester la CI/CD (2 min)
```bash
# Créer une branche de test
git checkout -b feature/test-cicd
echo "# Test" >> README.md
git add README.md
git commit -m "test: CI/CD setup"
git push origin feature/test-cicd

# Créer une PR sur GitHub
# Vérifier que les workflows se lancent
```

## 📊 Architecture CI/CD

```
┌─────────────────────────────────────────────┐
│ Feature Branch (feature/*)                  │
│ ├─ Lint + Tests                            │
│ ├─ SonarCloud                              │
│ ├─ Reviewdog (commentaires PR)             │
│ └─ Build APK debug                         │
└─────────────────────────────────────────────┘
                ↓ (Pull Request)
┌─────────────────────────────────────────────┐
│ Pull Request → develop                      │
│ ├─ Tous les checks ci-dessus               │
│ ├─ Code coverage check                     │
│ └─ Approbation requise                     │
└─────────────────────────────────────────────┘
                ↓ (Merge)
┌─────────────────────────────────────────────┐
│ Develop Branch                              │
│ ├─ Tous les checks                         │
│ ├─ Build APK debug                         │
│ └─ 🚀 Firebase Distribution (testers)      │
└─────────────────────────────────────────────┘
                ↓ (Release)
┌─────────────────────────────────────────────┐
│ Main Branch                                 │
│ ├─ Build APK release                       │
│ └─ 🚀 Firebase Distribution (beta)         │
└─────────────────────────────────────────────┘
```

## 🛠️ Outils configurés

| Outil | Rôle | Gratuit |
|-------|------|---------|
| **GitHub Actions** | Exécution CI/CD | ✅ Oui |
| **SonarCloud** | Analyse qualité code | ✅ Oui (open source) |
| **Reviewdog** | Review automatique PR | ✅ Oui |
| **JaCoCo** | Code coverage | ✅ Oui |
| **Firebase App Distribution** | Distribution beta | ✅ Oui |
| **Dependabot** | Mises à jour dépendances | ✅ Oui |

**Coût total : 0€** 🎉

## 📖 Documentation complète

Pour plus de détails, consultez : [`docs/CI-CD-SETUP.md`](./docs/CI-CD-SETUP.md)

## ⚠️ Note importante

Les warnings dans les workflows concernant les secrets (`GOOGLE_SERVICES_JSON`, `SONAR_TOKEN`, etc.) sont **normaux**. Ils disparaîtront une fois les secrets configurés dans GitHub.

## 🆘 Besoin d'aide ?

- 📚 [Guide complet](./docs/CI-CD-SETUP.md)
- 🐛 [Issues GitHub](https://github.com/luckytzu/Les-Ombres-du-Son/issues)
- 💬 Contactez @luckytzu ou @InsightSeeker-dev

---

**✨ Bon développement ! 🚀**
