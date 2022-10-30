package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;

@Service
public class ChooseDay extends AbstractBotState {

    public ChooseDay(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {

        MainCommandsHandler handler = new MainCommandsHandler(context,
                State.MENU_CHOICE, Message.MENU);
        if ((sm = handler.handleBackButton()) != null
        || (sm = handler.handleReportChoice()) != null)
            question();
    }
}
