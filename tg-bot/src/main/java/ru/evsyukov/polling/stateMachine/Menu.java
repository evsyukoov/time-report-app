package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import ru.evsyukov.polling.messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

@Service
@Slf4j
public class Menu implements BotState {

    private final ClientRepository clientRepository;

    private final MainCommandsHandler mainHandler;

    private final ButtonsProperties buttonsProperties;

    @Autowired
    public Menu(ClientRepository clientRepository, MainCommandsHandler mainHandler, ButtonsProperties buttonsProperties) {
        this.clientRepository = clientRepository;
        this.mainHandler = mainHandler;
        this.buttonsProperties = buttonsProperties;
    }

    @Override
    public State getState() {
        return State.MENU;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm;
        if (!context.isCallBackQuery()) {
            return;
        }
        if ((sm = mainHandler.handleBackButton(context, Message.REGISTER_NAME, State.CHECK_NAME)) != null
                && !clientRepository.findById(context.getClient().getUid()).get().isRegistered()) {
            question(sm, context);
        } else {
            sm = new SendMessage();
            sm.setText((Utils.generateResultMessage(Message.REGISTER_IS_FINISHED, Message.MENU)));
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);

            Client client = context.getClient();
            client.setState(State.MENU_CHOICE);
            client.setName(context.getClient().getName());
            client.setRegistered(true);
            clientRepository.save(client);
            log.info("Successfully update client {} at database", client);

            question(sm, context);
        }
    }

}
