# Guide d utilisation des services

Ce document explique comment utiliser les services du projet pour faire les actions metier principales.

## 1. Objectif

La couche service sert a:
- centraliser la logique metier
- orchestrer plusieurs DAO
- valider les regles avant de toucher la base

Les ecrans UI appellent les services, pas directement les DAO.

## 2. Fichiers service disponibles

- main/service/AuthService.java
- main/service/CampaignService.java
- main/service/SessionService.java
- main/service/ChoiceService.java
- main/service/AssignmentService.java
- main/service/ServiceResult.java

## 3. Contrat de retour

Les services renvoient:
- soit un objet metier (ex: User, Campaign)
- soit ServiceResult

ServiceResult contient:
- success: true/false
- message: message utilisateur ou technique

## 4. Actions principales

### 4.1 Authentification

Service: AuthService

Actions:
- login(login, password)
- isAdmin(user)
- isStudent(user)

Flux recommande:
1. Appeler login
2. Si null, afficher erreur
3. Sinon router vers ecran admin ou etudiant selon role

### 4.2 Gestion campagne

Service: CampaignService

Actions:
- createCampaign(campaign)
- getCampaign(id)
- getCampaignsByPromo(promo)
- getCampaignsByStatus(status)
- updateSettings(campaignId, name, registrationDay, maxChoices)
- changeStatus(campaignId, nextStatus)

Cycle de vie autorise:
- PREPARATION -> OPEN
- OPEN -> CLOSED
- CLOSED -> PROCESSING
- PROCESSING -> VALIDATED
- VALIDATED -> ARCHIVED

### 4.3 Gestion sessions

Service: SessionService

Actions:
- createSession(session)
- updateSession(session)
- deleteSession(sessionId)
- listByCampaign(campaignId)
- searchSessions(campaignId, dominanteNameLike, fromMinute, toMinute)

Regles appliquees:
- creation/modification/suppression uniquement en PREPARATION
- controle des bornes horaires
- capacite > 0
- endMinute > startMinute

### 4.4 Gestion choix etudiant

Service: ChoiceService

Actions:
- replaceStudentChoices(campaignId, studentId, newChoices)
- getStudentChoices(campaignId, studentId)

Regles appliquees:
- campagne OPEN obligatoire
- etudiant de la meme promo que la campagne
- nombre de choix <= maxChoices
- pas de rang duplique
- pas de session dupliquee

### 4.5 Traitement automatique

Service: AssignmentService

Action:
- runAutoAssignment(campaignId)

Comportement:
1. Verifie que la campagne est CLOSED
2. Passe en PROCESSING
3. Supprime les anciennes inscriptions de la campagne
4. Parcourt les etudiants de la promo
5. Trie les choix par rang
6. Attribue ALLOCATED si place dispo, sinon WAITLIST
7. Passe la campagne en VALIDATED

## 5. Exemples de scenarios

### 5.1 Scenario admin: ouverture des inscriptions

1. Charger campagne via getCampaign
2. Modifier parametres via updateSettings
3. Appeler changeStatus vers OPEN

### 5.2 Scenario etudiant: depot des choix

1. Charger sessions filtrables avec searchSessions
2. Construire une liste de Choice ordonnee
3. Appeler replaceStudentChoices
4. Afficher le message de ServiceResult

### 5.3 Scenario admin: traitement final

1. Fermer la campagne: changeStatus vers CLOSED
2. Lancer runAutoAssignment
3. Afficher le bilan de traitement

## 6. Bonnes pratiques

- Toujours verifier le retour ServiceResult avant d enchainer
- Garder la logique metier dans service, pas dans les ecrans
- Eviter les appels DAO directs dans les classes screen
- Journaliser les messages d erreur des services dans la console UI

## 7. Point de vigilance

Le traitement automatique actuel attribue une inscription par choix dans l ordre des rangs.
Si vous voulez limiter le nombre final d inscriptions par etudiant (par exemple 1 seule session validee), il faut ajouter cette regle dans AssignmentService.

## 8. Plan d integration rapide UI

1. LoginScreen utilise AuthService
2. Ecran admin utilise CampaignService et SessionService
3. Ecran etudiant utilise SessionService et ChoiceService
4. Bouton traiter utilise AssignmentService

Ce plan permet de brancher l application rapidement avec une architecture propre.
