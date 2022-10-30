package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.utils.SendHelper;

@Service
@Slf4j
public class ReportType implements BotState{

    private final MainCommandsHandler mainHandler;

    private final ButtonsProperties buttonsProperties;

    @Autowired
    public ReportType(MainCommandsHandler mainHandler,
                      ButtonsProperties buttonsProperties) {
        this.mainHandler = mainHandler;
        this.buttonsProperties = buttonsProperties;
    }

    @Override
    public State getState() {
        return State.REPORT_TYPE;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm;
        if ((sm = mainHandler.handleBackButton(context, Message.REGISTER_DEPARTMENT, State.REGISTER_NAME)) != null) {
            question(sm, context);
        } else {
            sm = new SendMessage();
            sm.setText(Message.CHOOSE_REPORT_TYPE);
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getDays(), null, 2);
            question(sm, context);
        }
    }

}
