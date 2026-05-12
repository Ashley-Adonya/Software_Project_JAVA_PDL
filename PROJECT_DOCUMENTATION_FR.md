# Documentation Projet PDL (FR)

## 1. Contexte

Le projet PDL vise a gerer les inscriptions des etudiants a des sessions de presentation de dominantes.
Chaque dominante propose des sessions avec une capacite limitee, sur une journee, avec des contraintes horaires strictes.

Le systeme doit permettre:
- aux administrateurs de preparer et piloter une campagne
- aux etudiants de formuler des choix ordonnes
- au systeme de realiser une attribution automatique des places

## 2. But du projet

Construire une application Java (JDBC + DAO + services + UI Kola) qui couvre le cycle complet:
- preparation des campagnes et sessions
- ouverture/fermeture des inscriptions
- saisie et modification des choix etudiants
- traitement automatique et validation des inscriptions

## 3. Objectifs fonctionnels

1. Gerer les roles
- ADMIN
- STUDENT

2. Gerer les campagnes d inscription
- creation
- parametrage (promo, date, nombre max de choix)
- changement de statut

3. Gerer les dominantes
- CRUD
- nom du responsable

4. Gerer les sessions
- CRUD en phase PREPARATION uniquement
- capacite
- horaire
- rattachement a une campagne et une dominante

5. Gerer les choix etudiants
- liste ordonnee
- modification autorisee uniquement en OPEN
- respect du max de choix

6. Lancer le traitement automatique
- attribution selon ordre de preference et places disponibles
- production d inscriptions ALLOCATED/WAITLIST

## 4. Contraintes metier

1. Plages horaires autorisees
- matin: 08:30 a 12:30
- apres-midi: 13:30 a 17:30

2. Cycle de vie d une campagne
- PREPARATION
- OPEN
- CLOSED
- PROCESSING
- VALIDATED
- ARCHIVED

3. Regles de stabilite
- sessions modifiables uniquement en PREPARATION
- choix modifiables uniquement en OPEN
- coherence campaign/session pour choix et inscriptions
- respect de la capacite lors des ALLOCATED

## 5. Perimetre technique

1. Langage
- Java

2. IDE
- Eclipse (contrainte pedagogique)
- VS Code utilise en support, avec configuration Java adaptee

3. Acces donnees
- JDBC obligatoire
- pattern DAO

4. Architecture applicative
- model: objets metier
- dao: acces SQL
- service: logique metier et orchestration
- screen: interface utilisateur Kola UI

## 6. Structure de donnees

Tables principales:
- users
- dominantes
- campaigns
- sessions
- choices
- registrations

Principes:
- une campagne concerne une seule promo
- une session appartient a une campagne et a une dominante
- un choix est lie a un etudiant et ordonne par rang
- une inscription est le resultat du traitement

## 7. Services applicatifs

Services disponibles:
- AuthService
- CampaignService
- SessionService
- ChoiceService
- AssignmentService

Responsabilites:
- valider les regles metier
- combiner plusieurs DAO pour des actions complexes
- exposer des retours simples via ServiceResult

## 8. Scenarios cle

1. Scenario administrateur
- creer campagne
- creer sessions
- ouvrir inscriptions
- fermer inscriptions
- lancer traitement auto
- valider puis archiver

2. Scenario etudiant
- se connecter
- consulter sessions filtrables
- saisir ses choix ordonnes
- modifier ses choix tant que campagne OPEN
- consulter resultat final

## 9. Regles d implementation

1. Les ecrans n appellent pas la base directement
2. Toute regle metier doit etre en service
3. Les DAO restent centrees sur la persistance
4. Les validations critiques existent aussi en base (contraintes/triggers)

## 10. Livrables projet

1. Schema SQL d initialisation
- 001_init_pdl.sql

2. Scripts de seed
- 002_seed_minimal_users.sql
- 003_seed_dataset.sql

3. Couches Java
- model
- dao
- service
- screen

4. Documentation
- SERVICES_GUIDE.md
- SERVICES_GUIDE_EN.md
- PROJECT_DOCUMENTATION_FR.md

## 11. Hypotheses actuelles

1. Les mots de passe sont stockes en clair dans cette version pedagogique
2. Le traitement auto actuel attribue par ordre de preference, avec WAITLIST en cas de saturation
3. Les ameliorations futures peuvent inclure des regles de conflit horaire plus fines

## 12. Evolutions possibles

1. Limiter le nombre final d inscriptions par etudiant
2. Ajouter une vraie strategie d optimisation d attribution
3. Ajouter un tableau de bord statistique
4. Introduire un historique detaille des operations
5. Renforcer la securite (hash mot de passe, audit, droits plus fins)
