package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;

@Service
@Slf4j
public class NotificationChoice implements BotState {

    private final MainCommandsHandler mainHandler;

    public NotificationChoice(MainCommandsHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.NOTIFICATION_CHOICE;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm;
        if ((sm = mainHandler.handleTimeChoice(context)) != null) {
            question(sm, context);
        }
    }

}
