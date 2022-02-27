package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import hibernate.access.ClientDao;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;
import utils.Utils;

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
