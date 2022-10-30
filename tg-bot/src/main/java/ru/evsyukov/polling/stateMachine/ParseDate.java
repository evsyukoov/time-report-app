package ru.evsyukov.polling.stateMachine;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;

@Service
public class ParseDate implements BotState {

    private final MainCommandsHandler mainHandler;

    public ParseDate(MainCommandsHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public State getState() {
        return State.PARSE_DATE;
    }

    @Override
    public void handleMessage(BotContext context) {
        SendMessage sm;
        if ((sm = mainHandler.handleBackButton(context, Message.CHOOSE_REPORT_TYPE, State.CHOOSE_DAY)) != null
        || (sm = mainHandler.parseDate(context)) != null) {
            question(sm , context);
        }
    }
}
