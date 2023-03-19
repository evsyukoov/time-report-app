package ru.evsyukov.polling.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.evsyukov.app.state.State;
import ru.evsyukov.polling.bot.BotContext;
import ru.evsyukov.polling.data.BotDataService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.utils.messages.Message;

import java.util.List;

@Service
@Slf4j
public class CheckName implements BotState {

    private final BotDataService botDataService;

    @Autowired
    public CheckName(BotDataService botDataService) {
        this.botDataService = botDataService;
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
            log.warn("Callback expected, client {}", context.getClient());
            return;
        }
        List<String> expected = botDataService.getFreeEmployeeNamesSorted();
        String receive = context.getMessage().replace(Message.EMPTY_SYMBOL, "");
        if (!expected.contains(receive)) {
            log.warn("No such client, wrong text received by {}", context.getClient());
            return;
        }
        List<String> allRegisteredClientsNames = botDataService.getAllRegisteredClientNames();

        if (allRegisteredClientsNames.contains(receive)) {
            log.warn("Already has such client at database {}", receive);
            sm.setText(Message.WRONG_NAME_CHOSEN);
            SendHelper.setInlineKeyboardOneColumn(sm, botDataService.getFreeEmployeeNamesSorted(), null);
        } else {
            botDataService.updateClientStateAndName(context.getClient(), State.MENU, receive, false);
            sm.setText(String.format(Message.NAME_CHOSEN, context.getMessage()));
            SendHelper.setInlineKeyboard(sm,
                    List.of(Message.BACK, Message.APPROVE), null, 2);
        }
        SendHelper.refreshInlineKeyboard(context);
        question(sm, context);
    }

}
