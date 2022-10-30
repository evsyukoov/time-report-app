package ru.evsyukov.polling.stateMachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.utils.SendHelper;

@Service
public class MenuChoice implements BotState {

    private final MainCommandsHandler mainHandler;

    @Autowired
    public MenuChoice(MainCommandsHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.MENU_CHOICE;
    }

    @Override
    public void handleMessage(BotContext context) {
        SendMessage sm;
        if ((sm = mainHandler.handleMenuChoice(context)) != null) {
            SendHelper.refreshInlineKeyboard(context);
            question(sm, context);
        }
    }
}
