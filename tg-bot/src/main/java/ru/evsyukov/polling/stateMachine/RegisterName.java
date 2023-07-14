package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.data.BotDataService;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.utils.messages.Message;

@Service
@Slf4j
public class RegisterName implements BotState {

    private final BotDataService botDataService;

    @Autowired
    public RegisterName(BotDataService botDataService) {
        this.botDataService = botDataService;
    }

    @Override
    public State getState() {
        return State.REGISTER_NAME;
    }

    @Override
    public void handleMessage(BotContext context) {
        log.info("State {} with client {} start", getState().name(), context.getClient());
        SendMessage sm = new SendMessage();
        botDataService.updateClientState(context.getClient(), State.CHECK_NAME);

        sm.setText(Message.REGISTER_NAME);
        SendHelper.setInlineKeyboardOneColumn(sm,
                botDataService.getFreeEmployeeNamesSorted(),null);
        question(sm, context);
    }

}
