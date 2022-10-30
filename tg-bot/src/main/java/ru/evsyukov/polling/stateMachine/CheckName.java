package ru.evsyukov.polling.stateMachine;

import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.hibernate.access.ClientDao;
import ru.evsyukov.polling.hibernate.access.EmployeeDao;
import ru.evsyukov.polling.hibernate.entities.Client;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CheckName extends AbstractBotState {

    public CheckName(BotContext context) {
        super(context);
        sm = new SendMessage();
    }

    @Override
    public void handleMessage() {
        if (!context.isCallBackQuery()) {
            return;
        }
        List<String> expected = EmployeeDao.getEmployeeNames();
        String receive = context.getMessage().replace(Message.EMPTY_SYMBOL, "");
        if (!expected.contains(receive)) {
            return;
        }
        List<String> allRegisteredClientsNames = ClientDao.getAllClients()
                .stream()
                .map(Client::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (allRegisteredClientsNames.contains(receive)) {
            sm.setText(Message.WRONG_NAME_CHOSEN);
            SendHelper.setInlineKeyboardOneColumn(sm, EmployeeDao.getEmployeeNames(), null);
        } else {
            sm.setText(String.format(Message.NAME_CHOSEN, context.getMessage()));
            ClientDao.updateName(context.getClient(), State.MENU.ordinal(), receive, false);
            SendHelper.setInlineKeyboard(sm,
                    List.of(Message.BACK, Message.APPROVE), null, 2);
        }
        SendHelper.refreshInlineKeyboard(context);
        question();
    }
}
