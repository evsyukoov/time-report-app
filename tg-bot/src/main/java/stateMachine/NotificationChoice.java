package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;

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
