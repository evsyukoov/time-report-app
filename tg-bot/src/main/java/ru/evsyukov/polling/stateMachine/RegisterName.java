package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.hibernate.access.EmployeeDao;
import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.hibernate.access.ClientDao;
import ru.evsyukov.polling.messages.Message;

@Service
public class RegisterName extends AbstractBotState {

    public RegisterName(BotContext context) {
        super(context);
        sm = new SendMessage();
    }

    @Override
    public void handleMessage() {
        ClientDao.updateState(context.getClient(), State.CHECK_NAME.ordinal());
        sm.setText(Message.REGISTER_NAME);
        SendHelper.setInlineKeyboardOneColumn(sm,
                EmployeeDao.getEmployeeNames(),null);
        question();
    }

}
