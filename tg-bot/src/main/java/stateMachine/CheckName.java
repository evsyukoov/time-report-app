package stateMachine;

import bot.BotContext;
import hibernate.access.ClientDao;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;

import java.util.List;

public class CheckName extends AbstractBotState {

    public CheckName(BotContext context) {
        super(context);
        sm = new SendMessage();
    }

    @Override
    public void handleMessage() {
        ClientDao.updateState(context.getClient(), State.MENU.ordinal());
        sm.setText(String.format(Message.NAME_CHOSEN, context.getMessage()));
        SendHelper.setInlineKeyboard(sm,
                List.of(Message.BACK, Message.APPROVE),null, 2);
        question();
    }
}
