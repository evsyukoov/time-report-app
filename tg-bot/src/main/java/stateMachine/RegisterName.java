package stateMachine;

import bot.BotContext;
import hibernate.access.EmployeeDao;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import utils.SendHelper;
import hibernate.access.ClientDao;
import messages.Message;
import utils.Utils;

public class RegisterName extends AbstractBotState {

    public RegisterName(BotContext context) {
        super(context);
        sm = new SendMessage();
    }

    @Override
    public void handleMessage() {
        ClientDao.updateState(context.getClient(), State.MENU.ordinal());
        sm.setText(Message.REGISTER_NAME);
        SendHelper.setInlineKeyboardOneColumn(sm,
                EmployeeDao.getEmployeeNames(),null);
        question();
    }

}
