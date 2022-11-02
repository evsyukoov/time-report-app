package ru.evsyukov.polling.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.bot.ReportingBot;
import ru.evsyukov.polling.messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.stateMachine.BotState;
import ru.evsyukov.polling.stateMachine.BotStateFactory;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class NewMessageHandler {

    private final ClientRepository clientRepository;

    private final MainCommandsHandler mainHandler;

    private final EmployeeRepository employeeRepository;

    private final ButtonsProperties buttonsProperties;

    @Autowired
    public NewMessageHandler(ClientRepository clientRepository,
                             MainCommandsHandler mainHandler,
                             EmployeeRepository employeeRepository,
                             ButtonsProperties buttonsProperties) {
        this.clientRepository = clientRepository;
        this.mainHandler = mainHandler;
        this.employeeRepository = employeeRepository;
        this.buttonsProperties = buttonsProperties;
    }

    public Client getClient(Update update) {
        State current;
        Chat chat = Utils.getCurrentChat(update);
        Optional<Client> clientOpt = clientRepository.findById(chat.getId());
        Client client;
        if (clientOpt.isEmpty()) {
            client = new Client();
            current = State.REGISTER_NAME;
            client.setState(current);
            client.setUid(chat.getId());
            clientRepository.save(client);
            log.info("Create client with id {}", client);
        }
        else {
            client = clientOpt.get();
            log.info("Find client with id {}", client);
        }
        return client;
    }

    public BotContext initBotContext(Client client, Update update, ReportingBot bot) {
        boolean isCallBack = Utils.isCallBackMessage(update);
        return new BotContext(bot, update, client, isCallBack, isCallBack ? update.getCallbackQuery().getData():
                update.getMessage().getText());
    }

    public SendMessage getSendMessage(BotContext context) {
        SendMessage sendMessage;
        if ((sendMessage = handleStartStop(context)) != null) {
            return sendMessage;
        }
        //vacationMode также хендлим сразу
        if (context.getClient().isOnVacation()) {
            sendMessage = mainHandler.handleClearVacation(context);
            return sendMessage;
        }
        return null;
    }

    public BotState getBotState(BotContext context) {
        return BotStateFactory.createBotState(context.getClient().getState());
    }

    private SendMessage handleStartStop(BotContext context) {
        String command = context.getMessage();
        SendMessage sm = null;
        if (command.equals(Message.START) || command.equals(Message.STOP)) {
            log.info("Received START or STOP command from client {}", context.getClient());
            sm = new SendMessage();
            Client client = context.getClient();
            if (!client.isOnVacation()) {
                if (client.isRegistered()) {
                    client.setState(State.MENU_CHOICE);
                    clientRepository.save(client);
                    log.info("Update client id {} to state {}", client.getUid(), client.getState());

                    SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
                    sm.setText(Message.MENU);
                } else {
                    client.setState(State.CHECK_NAME);
                    clientRepository.save(client);
                    log.info("Update client id {} to state {}", client.getUid(), client.getState());

                    SendHelper.setInlineKeyboardOneColumn(sm, employeeRepository.getAllEmployeeNames(), null);
                    sm.setText(Message.REGISTER_NAME);
                }
            } else {
                log.info("Request was received by client {} on vacation", context.getClient());
                SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.CLEAR_VACATION, 1);
                sm.setText(Message.YOU_ARE_IN_VACATION);
            }
        }
        return sm;
    }

}
