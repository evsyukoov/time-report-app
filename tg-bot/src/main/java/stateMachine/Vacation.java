package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import messages.Message;

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
