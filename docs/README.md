# 📚 Documentation - Les Ombres du Son

Bienvenue dans la documentation du projet **Les Ombres du Son**.

## 📖 Guides disponibles

### [🚀 Guide CI/CD](./CI-CD-SETUP.md)
Guide complet pour configurer et utiliser la CI/CD du projet :
- Configuration SonarCloud
- Configuration Firebase App Distribution
- Configuration des secrets GitHub
- Workflow de développement
- Troubleshooting

## 🏗️ Architecture du projet

```
Les-Ombres-du-Son/
├── app/                          # Code source Android
│   ├── src/main/java/           # Code Java
│   ├── src/main/res/            # Ressources (layouts, drawables)
│   └── build.gradle.kts         # Configuration Gradle
├── .github/
│   ├── workflows/               # Workflows CI/CD
│   │   ├── ci-feature.yml      # CI pour features
│   │   ├── ci-develop.yml      # CI/CD pour develop
│   │   └── ci-main.yml         # CI/CD pour main
│   ├── dependabot.yml          # Mises à jour automatiques
│   └── CODEOWNERS              # Propriétaires du code
├── docs/                        # Documentation
└── sonar-project.properties    # Configuration SonarCloud
```

## 🔄 Workflow Git

```
main (production)
  ↑
develop (développement)
  ↑
feature/* (fonctionnalités)
```

## 🛠️ Stack technique

- **Langage** : Java 11
- **Framework** : Android SDK 36
- **Backend** : Firebase (Auth + Firestore)
- **CI/CD** : GitHub Actions
- **Qualité** : SonarCloud + Reviewdog
- **Distribution** : Firebase App Distribution

## 📝 Conventions

### Commits
Suivre [Conventional Commits](https://www.conventionalcommits.org/) :
- `feat:` Nouvelle fonctionnalité
- `fix:` Correction de bug
- `docs:` Documentation
- `test:` Tests
- `chore:` Maintenance

### Branches
- `main` : Production
- `develop` : Développement
- `feature/*` : Nouvelles fonctionnalités
- `fix/*` : Corrections de bugs
- `hotfix/*` : Corrections urgentes

## 🧪 Tests

```bash
# Tests unitaires
./gradlew testDebugUnitTest

# Tests instrumentés
./gradlew connectedAndroidTest

# Code coverage
./gradlew jacocoTestReport
```

## 🚀 Déploiement

- **Develop** → Firebase App Distribution (groupe: testers)
- **Main** → Firebase App Distribution (groupe: beta)
- **Release** → Google Play Store (à venir)

## 📊 Dashboards

- **SonarCloud** : [Voir le projet](https://sonarcloud.io/project/overview?id=luckytzu_Les-Ombres-du-Son)
- **GitHub Actions** : [Voir les workflows](https://github.com/luckytzu/Les-Ombres-du-Son/actions)
- **Firebase Console** : [Voir le projet](https://console.firebase.google.com)

## 👥 Contributeurs

- [@luckytzu](https://github.com/luckytzu)
- [@InsightSeeker-dev](https://github.com/InsightSeeker-dev)

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/luckytzu/Les-Ombres-du-Son/issues)
- **Discussions** : [GitHub Discussions](https://github.com/luckytzu/Les-Ombres-du-Son/discussions)
