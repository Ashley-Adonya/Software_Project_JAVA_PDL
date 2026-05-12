# Service Usage Guide

This document explains how to use the project services to perform the main business actions.

## 1. Goal

The service layer is used to:
- centralize business logic
- orchestrate multiple DAOs
- validate rules before writing to the database

UI screens should call services, not DAOs directly.

## 2. Available service files

- main/service/AuthService.java
- main/service/CampaignService.java
- main/service/SessionService.java
- main/service/ChoiceService.java
- main/service/AssignmentService.java
- main/service/ServiceResult.java

## 3. Return contract

Services return:
- either a business object (for example User, Campaign)
- or a ServiceResult

ServiceResult contains:
- success: true/false
- message: user-friendly or technical message

## 4. Main actions

### 4.1 Authentication

Service: AuthService

Actions:
- login(login, password)
- isAdmin(user)
- isStudent(user)

Recommended flow:
1. Call login
2. If null, show error
3. Otherwise route to admin screen or student screen based on role

### 4.2 Campaign management

Service: CampaignService

Actions:
- createCampaign(campaign)
- getCampaign(id)
- getCampaignsByPromo(promo)
- getCampaignsByStatus(status)
- updateSettings(campaignId, name, registrationDay, maxChoices)
- changeStatus(campaignId, nextStatus)

Allowed lifecycle:
- PREPARATION -> OPEN
- OPEN -> CLOSED
- CLOSED -> PROCESSING
- PROCESSING -> VALIDATED
- VALIDATED -> ARCHIVED

### 4.3 Session management

Service: SessionService

Actions:
- createSession(session)
- updateSession(session)
- deleteSession(sessionId)
- listByCampaign(campaignId)
- searchSessions(campaignId, dominanteNameLike, fromMinute, toMinute)

Applied rules:
- create/update/delete only in PREPARATION
- allowed time windows validation
- capacity > 0
- endMinute > startMinute

### 4.4 Student choices

Service: ChoiceService

Actions:
- replaceStudentChoices(campaignId, studentId, newChoices)
- getStudentChoices(campaignId, studentId)

Applied rules:
- campaign must be OPEN
- student must belong to campaign promo
- number of choices <= maxChoices
- no duplicated rank
- no duplicated session

### 4.5 Automatic assignment

Service: AssignmentService

Action:
- runAutoAssignment(campaignId)

Behavior:
1. Check campaign is CLOSED
2. Switch to PROCESSING
3. Delete previous registrations for this campaign
4. Iterate students in the promo
5. Sort choices by rank
6. Assign ALLOCATED if capacity available, otherwise WAITLIST
7. Switch campaign to VALIDATED

## 5. Example scenarios

### 5.1 Admin scenario: open registration

1. Load campaign with getCampaign
2. Update settings with updateSettings
3. Call changeStatus to OPEN

### 5.2 Student scenario: submit choices

1. Load/filter sessions with searchSessions
2. Build an ordered Choice list
3. Call replaceStudentChoices
4. Display ServiceResult message

### 5.3 Admin scenario: final processing

1. Close campaign with changeStatus to CLOSED
2. Run runAutoAssignment
3. Display processing summary

## 6. Best practices

- Always check ServiceResult before next steps
- Keep business logic in services, not in screens
- Avoid direct DAO calls in screen classes
- Log service error messages in UI console

## 7. Attention point

Current automatic assignment creates one registration per ranked choice.
If you want to limit final registrations per student (for example only one validated session), add this rule in AssignmentService.

## 8. Quick UI integration plan

1. LoginScreen uses AuthService
2. Admin screen uses CampaignService and SessionService
3. Student screen uses SessionService and ChoiceService
4. Process button uses AssignmentService

This plan allows fast integration with a clean architecture.
