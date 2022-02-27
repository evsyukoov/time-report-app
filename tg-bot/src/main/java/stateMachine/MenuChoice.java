package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import utils.SendHelper;

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
