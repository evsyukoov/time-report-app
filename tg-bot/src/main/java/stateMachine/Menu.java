package stateMachine;

import bot.BotContext;
import handlers.MainCommandsHandler;
import hibernate.access.ClientDao;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;
import utils.Utils;

public class Menu extends AbstractBotState {

    public Menu(BotContext context) {
        super(context);
        sm = new SendMessage();
    }

    @Override
    public void handleMessage() {
        if (!context.isCallBackQuery()) {
            return;
        }
        MainCommandsHandler backButtonHandler = new MainCommandsHandler(context, State.CHECK_NAME, Message.REGISTER_NAME);
        if ((sm = backButtonHandler.handleBackButton()) != null && !ClientDao.getClient(context.getClient().getUid()).isRegistered()) {
            question();
        } else {
            sm = new SendMessage();
            sm.setText((Utils.generateResultMessage(Message.REGISTER_IS_FINISHED, Message.MENU)));
            SendHelper.setInlineKeyboard(sm, Message.actionsMenu, null, 3);
            ClientDao.updateName(context.getClient(), State.MENU_CHOICE.ordinal(), context.getClient().getName(), true);
            question();
        }
    }

}
