package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import messages.Message;

public class ChooseDay extends AbstractBotState {

    public ChooseDay(BotContext context) {
        super(context);
    }

    @Override
    public void handleMessage() {

        MainCommandsHandler handler = new MainCommandsHandler(context,
                State.MENU_CHOICE, Message.MENU);
        if ((sm = handler.handleBackButton()) != null
        || (sm = handler.handleReportChoice()) != null)
            question();
    }
}
