package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.jvnet.hk2.annotations.Service;

@Service
public class Vacation extends AbstractBotState {

    public Vacation(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        MainCommandsHandler handler = new MainCommandsHandler(context,
                State.MENU_CHOICE, Message.MENU);
        if ((super.sm = handler.handleBackButton()) == null) {
            sm = handler.handleVacationsDate();
        }
        question();
    }
}
