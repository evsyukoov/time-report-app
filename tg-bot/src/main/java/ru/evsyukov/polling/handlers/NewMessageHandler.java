package ru.evsyukov.polling.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.bot.ReportingBot;
import ru.evsyukov.polling.data.BotDataService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.stateMachine.BotState;
import ru.evsyukov.polling.stateMachine.BotStateFactory;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;
import ru.evsyukov.utils.messages.Message;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class NewMessageHandler {

    private final MainCommandsHandler mainHandler;

    private final BotDataService botDataService;

    private final ButtonsProperties buttonsProperties;

    @Autowired
    public NewMessageHandler(MainCommandsHandler mainHandler,
                             BotDataService botDataService,
                             ButtonsProperties buttonsProperties) {
        this.mainHandler = mainHandler;
        this.botDataService = botDataService;
        this.buttonsProperties = buttonsProperties;
    }

    public Client getClient(Update update) {
        Chat chat = Utils.getCurrentChat(update);
        Optional<Client> clientOpt = botDataService.getClientById(chat.getId());
        Client client;
        if (clientOpt.isEmpty()) {
            client = botDataService.saveNewClient(chat.getId());
        } else {
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
                    botDataService.updateClientState(client, State.MENU_CHOICE);
                    SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);
                    sm.setText(Message.MENU);
                } else {
                    botDataService.updateClientState(client, State.CHECK_NAME);
                    SendHelper.setInlineKeyboardOneColumn(sm, botDataService.getAllEmployeeNamesSorted(), null);
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
