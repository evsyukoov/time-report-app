package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChooseDay implements BotState {

    private final MainCommandsHandler mainHandler;

    @Autowired
    public ChooseDay(MainCommandsHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.CHOOSE_DAY;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm;
        if ((sm = mainHandler.handleBackButton(context, Message.MENU, State.MENU_CHOICE)) != null
        || (sm = mainHandler.handleReportChoice(context)) != null)
            question(sm, context);
    }
}
