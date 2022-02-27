package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;

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
