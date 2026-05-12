/*
 * Decompiled with CFR 0.152.
 */
package event;

import event.UiEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.BaseComp;

public class EventManager {
    private final Map<String, Action> legacyActions = new HashMap<String, Action>();
    private final Map<UiEvent.Type, List<Action>> typedActions = new EnumMap<UiEvent.Type, List<Action>>(UiEvent.Type.class);

    public void register(String string, Action action) {
        this.legacyActions.put(string, action);
    }

    public void trigger(String string, BaseComp baseComp) {
        Action action = this.legacyActions.get(string);
        if (action != null) {
            action.run(baseComp, null);
        }
    }

    public void register(UiEvent.Type type2, Action action) {
        this.typedActions.computeIfAbsent(type2, type -> new ArrayList()).add(action);
    }

    public void trigger(UiEvent uiEvent, BaseComp baseComp) {
        if (uiEvent == null) {
            return;
        }
        List<Action> list = this.typedActions.get((Object)uiEvent.getType());
        if (list == null) {
            return;
        }
        for (Action action : list) {
            action.run(baseComp, uiEvent);
        }
    }

    public boolean hasHandlers(UiEvent.Type type) {
        List<Action> list = this.typedActions.get((Object)type);
        return list != null && !list.isEmpty();
    }

    @FunctionalInterface
    public static interface Action {
        public void run(BaseComp var1, UiEvent var2);
    }
}

