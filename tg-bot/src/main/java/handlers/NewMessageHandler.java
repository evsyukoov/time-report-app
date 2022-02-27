package handlers;

import bot.BotContext;
import bot.ReportingBot;
import hibernate.access.ClientDao;
import hibernate.access.EmployeeDao;
import hibernate.entities.Client;
import messages.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import stateMachine.AbstractBotState;
import stateMachine.BotStateFactory;
import stateMachine.State;
import utils.SendHelper;
import utils.Utils;

import java.util.Collections;

public class NewMessageHandler {

    private final Update update;

    private final ReportingBot bot;

    private SendMessage sendMessage;

    private BotContext context;

    public SendMessage getSendMessage() {
        return sendMessage;
    }

    public NewMessageHandler(Update update, ReportingBot bot) {
        this.update = update;
        this.bot = bot;
    }

    public BotContext getContext() {
        return context;
    }

    public AbstractBotState getBotState() {
        State current;
        boolean callBack = update.getMessage() == null;
        Chat chat = callBack ?
                 update.getCallbackQuery().getMessage().getChat() : update.getMessage().getChat();
        String newMsg = (callBack ? update.getCallbackQuery().getData():
                update.getMessage().getText());
        long id = chat.getId();
        Client client = ClientDao.getClient(id);

        if (client == null) {
            current = State.REGISTER_NAME;
            client = ClientDao.createClient(id, current);
        } else {
            current = State.values()[client.getState()];
        }
        context = new BotContext(bot, update, client, callBack, newMsg);
        //хендлим старт и стоп сразу для всех, в логику каждого стейта заходить не нужно
        if ((sendMessage = handleStartStop()) != null) {
            return null;
        }
        //vacationMode также хенлим сразу
        if (client.isOnVacation()) {
            sendMessage = new MainCommandsHandler(context).handleClearVacation();
            return null;
        }
        return BotStateFactory.createBotState(current, new BotContext(bot, update, client, callBack, newMsg));
    }

    private SendMessage handleStartStop() {
        String command = context.getMessage();
        SendMessage sm = null;
        if (command.equals(Message.START) || command.equals(Message.STOP)) {
            sm = new SendMessage();
            Client client = context.getClient();
            if (!client.isOnVacation()) {
                if (client.isRegistered()) {
                    ClientDao.updateState(client, State.MENU_CHOICE.ordinal());
                    SendHelper.setInlineKeyboard(sm, Message.actionsMenu, null, 3);
                    sm.setText(Message.MENU);
                } else {
                    ClientDao.updateState(client, State.MENU.ordinal());
                    SendHelper.setInlineKeyboardOneColumn(sm, EmployeeDao.getEmployeeNames(), null);
                    sm.setText(Message.REGISTER_NAME);
                }
            } else {
                SendHelper.setInlineKeyboard(sm, Collections.emptyList(), Message.CLEAR_VACATION, 1);
                sm.setText(Message.YOU_ARE_IN_VACATION);
            }
        }
        return sm;
    }

}
