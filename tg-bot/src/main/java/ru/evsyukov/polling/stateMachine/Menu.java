package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.polling.handlers.MainCommandsHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.properties.ButtonsProperties;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;
import ru.evsyukov.utils.messages.Message;

@Service
@Slf4j
public class Menu implements BotState {

    private final MainCommandsHandler mainHandler;

    private final ButtonsProperties buttonsProperties;

    private final BotDataService botDataService;

    @Autowired
    public Menu(MainCommandsHandler mainHandler, ButtonsProperties buttonsProperties, BotDataService botDataService) {
        this.mainHandler = mainHandler;
        this.buttonsProperties = buttonsProperties;
        this.botDataService = botDataService;
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
                && !botDataService.isRegisteredClient(context.getClient())) {
            question(sm, context);
        } else {
            sm = new SendMessage();
            sm.setText((Utils.generateResultMessage(Message.REGISTER_IS_FINISHED, Message.MENU)));
            SendHelper.setInlineKeyboard(sm, buttonsProperties.getActionsMenu(), null, 3);

            botDataService.updateClientStateAndName(context.getClient(), State.MENU_CHOICE,
                    context.getClient().getName(), true);

            question(sm, context);
        }
    }

}
