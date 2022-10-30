package ru.evsyukov.polling.stateMachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;

@Service
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
        SendMessage sm;
        if ((sm = mainHandler.handleBackButton(context, Message.MENU, State.MENU_CHOICE)) != null
        || (sm = mainHandler.handleReportChoice(context)) != null)
            question(sm, context);
    }
}
