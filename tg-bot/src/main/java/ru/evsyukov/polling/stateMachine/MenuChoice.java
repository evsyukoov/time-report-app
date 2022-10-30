package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import org.jvnet.hk2.annotations.Service;
import ru.evsyukov.polling.utils.SendHelper;

@Service
public class MenuChoice extends AbstractBotState {

    public MenuChoice(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        MainCommandsHandler handler = new MainCommandsHandler(context);
        if ((sm = handler.handleMenuChoice()) != null) {
            SendHelper.refreshInlineKeyboard(context);
            question();
        }
    }
}
