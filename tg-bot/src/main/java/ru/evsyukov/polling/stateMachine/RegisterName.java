package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.polling.bot.BotContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.messages.Message;

@Service
@Slf4j
public class RegisterName implements BotState {

    private final ClientRepository clientRepository;

    private final EmployeeRepository employeeRepository;

    @Autowired
    public RegisterName(ClientRepository clientRepository,
                        EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public State getState() {
        return State.REGISTER_NAME;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm = new SendMessage();

        Client client = context.getClient();
        client.setState(State.CHECK_NAME.ordinal());
        clientRepository.save(client);
        log.info("Successfully update client {} at database", client);

        sm.setText(Message.REGISTER_NAME);
        SendHelper.setInlineKeyboardOneColumn(sm,
                employeeRepository.getAllEmployeeNames(),null);
        question(sm, context);
    }

}
