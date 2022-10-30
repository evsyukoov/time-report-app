package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import org.jvnet.hk2.annotations.Service;

@Service
public class NotificationChoice extends AbstractBotState{

    public NotificationChoice(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        MainCommandsHandler handler = new MainCommandsHandler(context);
        if ((sm = handler.handleTimeChoice()) != null) {
            question();
        }
    }

}
