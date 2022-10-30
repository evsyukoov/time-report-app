package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;

@Service
public class ReportType extends AbstractBotState{

    public ReportType(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {
        MainCommandsHandler handler = new MainCommandsHandler(context,
                State.REGISTER_NAME, Message.REGISTER_DEPARTMENT);
        if ((sm = handler.handleBackButton()) != null) {
            question();
        } else {
            sm = new SendMessage();
            sm.setText(Message.CHOOSE_REPORT_TYPE);
            SendHelper.setInlineKeyboard(sm, Message.days, null, 2);
            question();
        }
    }

}
