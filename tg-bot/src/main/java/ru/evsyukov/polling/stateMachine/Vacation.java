package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;

@Service
@Slf4j
public class Vacation implements BotState {

    private final MainCommandsHandler mainHandler;

    public Vacation(MainCommandsHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.VACATION;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm;
        if ((sm = mainHandler.handleBackButton(context, Message.MENU, State.MENU_CHOICE)) == null) {
            sm = mainHandler.handleVacationsDate(context);
        }
        question(sm, context);
    }
}
