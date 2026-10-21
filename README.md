# Taxi Fleet — Système de gestion de flotte de taxis

Application web Spring Boot (Java 21 / Maven) de gestion d'une flotte de taxis :
clients, chauffeurs, véhicules, courses, affectation automatique des chauffeurs,
statistiques de CA et configuration du score de rémunération.

## Stack technique

- Java 21
- Spring Boot 3.4.0
- Spring Web, Spring Data JPA, Spring Security, Spring Validation
- Thymeleaf (interface web) + Bootstrap
- MariaDB (production locale) / H2 (démo et tests)

## Fonctionnalités

- **Authentification** avec rôles `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_OPERATOR`, `ROLE_USER`
- **Clients** : CRUD, recherche
- **Chauffeurs** : CRUD, statut (disponible / en pause / indisponible / suspendu), commission
- **Véhicules** : CRUD, immatriculation, statut, valeur
- **Courses** : création, affectation automatique du chauffeur le plus proche,
  scores de priorité (charge / ancienneté / véhicule), cycle de vie complet
  (créée → assignée → en cours → terminée)
- **Statistiques** : chiffre d'affaires par jour, répartition des courses, activité
- **Administration** : gestion des comptes utilisateurs, configuration du scoring

## Démarrage rapide

```bash
git clone https://github.com/<ton-compte>/Taxi_Management.git
cd Taxi_Management
mvn spring-boot:run
```

L'application démarre sur <http://localhost:8085>.

> Par défaut, l'application utilise une base **H2 en mémoire** : aucune installation
> de base de données n'est nécessaire pour essayer le projet.

### Comptes de démonstration

| Utilisateur | Rôle              | Mot de passe |
|-------------|-------------------|--------------|
| `admin`     | Administrateur    | `password`   |
| `manager`   | Responsable exploitation | `password` |
| `operator`  | Opérateur radio   | `password`   |
| `user`      | Consultant        | `password`   |

Ces comptes sont créés automatiquement au premier démarrage (via `DemoDataLoader`).

## Utiliser MariaDB en local

Le mot de passe de la base de données **n'est jamais commité**. Il se trouve dans
`application-local.properties`, un fichier ignoré par git.

1. Crée la base :

   ```sql
   CREATE DATABASE taxidb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Copie le fichier d'exemple et renseigne tes identifiants :

   ```bash
   cp application-local.properties.example application-local.properties
   ```

3. Remplace `username` et `password` par les tiens, puis relance l'application.

Le fichier est chargé automatiquement grâce à :

```properties
spring.config.import=optional:file:application-local.properties
```

## Tests

```bash
mvn test
```

## Sécurité du dépôt

- `.gitignore` exclut `target/`, `data/`, les fichiers de configuration locale
  (`application-local.properties`), les bases locales et les fichiers d'IDE.
- `application.properties` ne contient **aucun mot de passe** : uniquement des
  valeurs par défaut et des variables d'environnement (`DB_URL`, `DB_USERNAME`,
  `DB_PASSWORD`, `DEMO_PASSWORD`).

## Structure du projet

```
src/main/java/com/example/taxi/
├── config/        # SecurityConfig, DemoDataLoader
├── controller/
│   ├── rest/      # API REST
│   └── web/       # contrôleurs Thymeleaf
├── dto/           # requête / réponse
├── entity/        # entités JPA
├── exception/     # gestion d'erreurs
├── mapper/        # conversion entité → DTO
├── repository/    # accès données
├── security/      # rôles, CustomUserDetailsService
├── service/       # logique métier
└── util/          # utilitaires
```
