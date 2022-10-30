package ru.evsyukov.polling.stateMachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BotStateFactory {

    private final static Map<State, BotState> states = new EnumMap<>(State.class);

    @Autowired
    public BotStateFactory(List<BotState> botStateList) {
        for (BotState botState : botStateList) {
            states.put(botState.getState(), botState);
        }
    }

    public static BotState createBotState(int st) {
        State state = State.values()[st];
        return states.get(state);
    }
}
