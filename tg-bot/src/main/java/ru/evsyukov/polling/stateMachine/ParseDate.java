package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.jvnet.hk2.annotations.Service;

@Service
public class ParseDate extends AbstractBotState {

    public ParseDate(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        MainCommandsHandler handler = new MainCommandsHandler(context,
                State.CHOOSE_DAY, Message.CHOOSE_REPORT_TYPE);
        if ((sm = handler.handleBackButton()) != null
        || (sm = handler.parseDate()) != null) {
            question();
        }
    }
}
