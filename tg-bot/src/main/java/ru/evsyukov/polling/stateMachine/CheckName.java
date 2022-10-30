package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.messages.Message;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckName implements BotState {

    private final EmployeeRepository employeeRepository;

    private final ClientRepository clientRepository;

    @Autowired
    public CheckName(EmployeeRepository employeeRepository, ClientRepository clientRepository) {
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public State getState() {
        return State.CHECK_NAME;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm = new SendMessage();
        if (!context.isCallBackQuery()) {
            return;
        }
        List<String> expected = employeeRepository.getAllEmployeeNames();
        String receive = context.getMessage().replace(Message.EMPTY_SYMBOL, "");
        if (!expected.contains(receive)) {
            return;
        }
        List<String> allRegisteredClientsNames = clientRepository.findAll()
                .stream()
                .map(Client::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (allRegisteredClientsNames.contains(receive)) {
            sm.setText(Message.WRONG_NAME_CHOSEN);
            SendHelper.setInlineKeyboardOneColumn(sm,employeeRepository.getAllEmployeeNames(), null);
        } else {
            updateClient(context.getClient(), State.MENU, receive);
            sm.setText(String.format(Message.NAME_CHOSEN, context.getMessage()));
            SendHelper.setInlineKeyboard(sm,
                    List.of(Message.BACK, Message.APPROVE), null, 2);
        }
        SendHelper.refreshInlineKeyboard(context);
        question(sm, context);
    }

    private void updateClient(Client client, State state, String name) {
        client.setState(state.ordinal());
        client.setName(name);
        client.setRegistered(false);

        clientRepository.save(client);
        log.info("Successfully update client {} at database", client);
    }
}
