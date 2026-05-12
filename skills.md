# Kola UI Skills - Guide Complet (Base Code Reelle)

Ce document est une synthese pratique de Kola UI basee sur l'analyse du code source du depot:
https://github.com/bjeff17/kola-ui

Objectif: utiliser Kola UI efficacement dans le projet PDL, en suivant la logique interne reelle de la librairie (pas uniquement la doc marketing).

## 1. Philosophie Kola UI

Kola UI n'est pas Swing/JavaFX classique. C'est un moteur UI custom Java2D avec:

- Une arborescence de composants (`BaseComp`) avec rendu manuel.
- Un fenetrage principal (`BaseWindow`) qui gere input, focus, couches, modal, drag/resize fenetre.
- Un style manager avec parsing Tailwind-like (`TailwindParser`) applique sur `StyleManager`.
- Un layout par moteur (`flex`, `grid`, `block`, `absolute`).
- Un rendu optimise par zones sales (dirty rectangles) via `DirtyManager`.
- Une distribution d'evenements par bubbling (du composant cible vers ses parents).

En pratique: on construit des composants, on place des enfants, on invalide quand l'etat change, et Kola UI gere le repaint.

## 2. Architecture Interne (a connaitre)

## 2.1 Noyau

- `src/BaseComp.java`
  - Base de tout composant.
  - Position/taille locales + conversion global/local.
  - Arbre parent/enfants.
  - `EventManager` par composant.
  - Support focus, cursor, drag, visibility.
  - Container queries (`addContainerQuery`, `addWidthContainerQuery`).

- `src/BaseWindow.java`
  - Fenetre undecorated custom (header et boutons systeme dessines a la src).
  - Boucle de rendu (timer si `fps > 0`, sinon repaint a la demande).
  - Hit test des composants via `HitTester`.
  - Bubbling evenements souris/roulette (`UiEvent`).
  - Focus clavier vers le composant focusable actif.
  - Layers/modals (`openLayer`, `openModal`, `closeTopLayer`).

## 2.2 Styling et layout

- `style/StyleManager.java`
  - Couleur de fond, border radius, tailles, et type de layout engine.
  - Delivre `doLayout(component)`.

- `style/TailwindParser.java`
  - Parse des classes utilitaires et les convertit sur `StyleManager`.

- `layout/*.java`
  - `FlexLayoutEngine`, `GridLayoutEngine`, `BlockLayoutEngine`, `AbsoluteLayoutEngine`.

## 2.3 Events

- `event/UiEvent.java`
  - Types: `POINTER_DOWN`, `POINTER_MOVE`, `POINTER_UP`, `CLICK`, `WHEEL`.
  - Donnees: x/y local fenetre, screenX/screenY, button, wheelRotation, clickCount, shiftDown.
  - `stopPropagation()` pour stopper le bubbling.

- `event/EventManager.java`
  - `register(UiEvent.Type, Action)`
  - `trigger(UiEvent, BaseComp)`

## 2.4 Performance

- `utils/DirtyManager.java`
  - Merge des dirty rects, fallback full redraw si surface sale trop grande.
  - Point cle: appeler `invalidate()` / `invalidateLocalRect(...)` apres mutation visible.

## 3. API Reelle a Utiliser (version code)

Important: plusieurs exemples README/docs sont anciens ou simplifies.
Toujours preferer les signatures vues dans le code source.

## 3.1 Creation fenetre

```java
BaseWindow win = new BaseWindow("Titre", 1200, 800);         // mode event-driven
// ou
BaseWindow win = new BaseWindow("Titre", 1200, 800, 60);     // boucle 60 FPS

BaseComp content = win.getContent();
win.show();
```

## 3.2 BaseComp - operations cle

```java
BaseComp root = new BaseComp(null);
root.setBounds(0, 0, 400, 300);
root.setClass("bg-gray-100 rounded-lg");

root.addChild(child);
root.removeChild(child);

root.setFocusable(true);
root.setDraggable(true);
root.invalidate();
```

## 3.3 Composants (constructeurs verifies)

- `Button(String text, int x, int y, int width, int height, Runnable onClick)`
- `CheckBox(String label, int x, int y, int width, int height, boolean initialValue)`
- `Label(String text, int x, int y, int width, int height)`
- `H(String text, int x, int y, int width, int height)`
- `TextField(int x, int y, int width, int height)`
- `TextAreaInput(int x, int y, int width, int height)`
- `TextArea(String text, int x, int y, int width, int height)`
- `Div(int x, int y, int width, int height, Color background, int radius)`
- `ScrollView(int x, int y, int width, int height)`
- `SegmentedSelect(int x, int y, int width, int height)`
- `SelectInput(int x, int y, int width, int height)`
- `NavMenuBar(int x, int y, int width, int height)`
- `ResizableDiv(int x, int y, int width, int height, Color background, int radius)`
- `FormModal(int width, int height, String title, Runnable onClose)`
- `ConfirmDialog(int width, int height, String title, String message, Runnable onConfirm, Runnable onCancel)`
- `ImageComp(String path, int x, int y, int width, int height)`
- `ImageComp(String source, int x, int y, int width, int height, String altText)`
- `SvgFromStringComp(String svgSource, int x, int y, int width, int height)`
- `LiveClockLabel(int x, int y, int width, int height, String pattern)`

## 4. Styling Tailwind-like (ce qui marche vraiment)

Appliquer via:

```java
component.setClass("bg-blue-500 rounded-lg w-64 h-32 flex gap-4");
```

Tokens verifies dans `TailwindParser`:

- Background:
  - `bg-blue-500`
  - `bg-[#ffcc00]`
  - `bg-[rgb(12,120,220)]`
  - `bg-[rgba(12,120,220,0.4)]`
  - Opacite suffixe: `bg-blue-500/60`
- Layout display:
  - `flex`, `grid`, `block`, `absolute`
  - `relative`, `fixed`, `sticky` => mappes sur comportement `absolute`
- Flex:
  - `flex-col`, `flex-row`, `gap-*`
- Grid:
  - `grid-cols-N`, `grid-rows-N`, `gap-*`
- Radius:
  - `rounded-none/sm/md/lg/xl/2xl/3xl/full`
  - `rounded-[18]`
- Dimensions:
  - `w-64`, `h-32` (echelle *4)
  - `w-[320]`, `h-[180]`
  - `w-full` (special), `h-full`
  - fractions (`w-1/2`) converties sur echelle interne

Remarque importante:
- Le parser actuel couvre surtout fond, radius, dimensions, gap, flex/grid/display.
- Les tokens type `p-*`, `m-*`, `border-*`, `shadow-*` ne sont pas tous implementes dans ce parser.

## 5. Events et Interaction (bon pattern)

Pattern recommande:

1. Enregistrer sur `getEventManager().register(...)`
2. Verifier la cible si necessaire (`event.getTarget() == this`)
3. Mettre a jour l'etat
4. Appeler `invalidate()`
5. Eventuellement `event.stopPropagation()`

Exemple:

```java
getEventManager().register(UiEvent.Type.POINTER_DOWN, (component, event) -> {
    if (event.getTarget() != this) return;
    pressed = true;
    event.stopPropagation();
    invalidate();
});
```

Pour clavier:

- Rendre le composant focusable: `setFocusable(true)`
- Implementer `onKeyPressed(KeyEvent)` / `onKeyTyped(KeyEvent)`
- Retourner `true` pour consommer l'evenement

## 6. Layers, Modal, Overlays

- `window.openLayer(layerComp)` pour stacker une couche custom.
- `window.openModal(modalContent)` pour fond assombri + fermeture click exterieur.
- `window.closeTopLayer()` pour fermer la couche du dessus.

Bonne pratique:
- Toujours dimensionner la modal avant ouverture.
- Garder une seule responsabilite par layer (menu mobile, popup de confirmation, etc.).

## 7. Scroll et UX

`ScrollView` supporte:

- Scroll vertical/horizontal.
- Latch d'axe pour trackpad (stabilise l'intention utilisateur).
- Scrollbars custom auto-hide.
- Drag thumb vertical/horizontal.

Usage:

```java
ScrollView scroll = new ScrollView(0, 0, 600, 400);
BaseComp content = scroll.getContent();
content.addChild(item1);
content.addChild(item2);
scroll.setContentHeight(1400);
scroll.setContentWidth(600);
```

## 8. Responsive interne (container queries)

Kola UI n'a pas media queries CSS globales, mais expose des queries de taille composant:

```java
topBar.addWidthContainerQuery(
    980,
    () -> applyCompactHeader(),
    () -> applyDesktopHeader()
);
```

Pratique pour mode desktop/mobile dans une fenetre redimensionnable.

## 9. Performance et robustesse (regles d'equipe)

1. Toujours invalider apres mutation visuelle.
2. Eviter de recreer des composants a chaque frame.
3. Preferer update de props + `invalidate()`.
4. Eviter les heavy operations dans `customGraphics`.
5. Limiter les allocations en hot path (drag, wheel, typing).
6. Utiliser `fps=0` pour interfaces formulaire/outils classiques; `fps>0` pour animations/jeu.

## 10. Pieges frequents (importants)

1. Utiliser `setStyle(...)` au lieu de `setClass(...)`.
   - Dans ce code, l'API est `setClass(String)` sur `BaseComp`.

2. Se fier aux exemples de README sans verifier les signatures.
   - Plusieurs exemples docs ne reflettent pas les constructeurs actuels.

3. Oublier `setFocusable(true)` sur un input custom.
   - Aucun clavier sans focusable + focus actif.

4. Oublier la taille du contenu dans `ScrollView`.
   - Sans `setContentHeight/Width`, pas de vrai overflow.

5. Ne pas stopper la propagation quand necessaire.
   - Peut provoquer clics ou drags non souhaites chez les parents.

## 11. Organisation recommandee pour PDL

Pattern simple:

1. `src/screen/*` pour ecrans (ex: `LoginScreen`).
2. `src/components/*` pour composants composites reutilisables.
3. `src/dao/*` pour acces donnees.
4. Une methode `buildUI()` par ecran.
5. Une methode `wireEvents()` par ecran.
6. Une methode `refreshViewState()` par ecran.

Cycle conseille:

1. Construire composants
2. Positionner et styler
3. Connecter events
4. Monter dans `window.getContent()`
5. `window.show()`

## 12. Checklist avant merge

1. Les callbacks mettent-ils a jour l'UI puis `invalidate()` ?
2. Le clavier est-il teste (focus, tabulation logique, raccourcis) ?
3. Le resize fenetre conserve-t-il la lisibilite ?
4. Les overlays/modal se ferment-ils proprement ?
5. Les composants scrollables gerent-ils bien l'overflow ?
6. Les styles `setClass(...)` utilises existent-ils dans `TailwindParser` ?

## 13. Exemple minimal fidele (API actuelle)

```java
import java.awt.Color;
import components.Button;
import components.Label;
import components.TextField;
import src.BaseComp;
import src.BaseWindow;

public class MiniLogin {
    public static void src(String[] args) {
        BaseWindow win = new BaseWindow("PDL Login", 640, 420);
        BaseComp content = win.getContent();

        Label title = new Label("Connexion", 24, 24, 240, 30);
        title.setColor(new Color(33, 42, 56));

        TextField user = new TextField(24, 74, 280, 36);
        user.setPlaceholder("Nom utilisateur");

        TextField pass = new TextField(24, 122, 280, 36);
        pass.setPlaceholder("Mot de passe");

        Button submit = new Button("Se connecter", 24, 176, 160, 38, () -> {
            System.out.println("Login: " + user.getText());
        });
        submit.setBackground(new Color(45, 121, 238));

        content.addChild(title);
        content.addChild(user);
        content.addChild(pass);
        content.addChild(submit);

        win.show();
    }
}
```

## 14. Resume ultra-court

- Kola UI = moteur UI Java2D a composants, rendu dirty regions, events en bubbling.
- API centrale: `BaseWindow`, `BaseComp`, `EventManager`, `StyleManager`.
- Styling via `setClass(...)` et parser Tailwind-like partiel.
- Toujours invalider apres changement d'etat.
- Verifier les signatures dans le code source avant implementation.
