package bot;

import handlers.NewMessageHandler;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import tasks.MessageNotificator;
import tasks.VacationThread;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import stateMachine.AbstractBotState;
import utils.SendHelper;

import java.io.FileInputStream;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ReportingBot extends TelegramLongPollingBot {

    final String token = "";

    private final String botName = "";

    public ReportingBot() {
        MessageNotificator notificator = new MessageNotificator(this);
        VacationThread vacationThread = new VacationThread(this);
        notificator.notificate();
        notificator.updateMessage();
        vacationThread.doJob();
    }

    private static final Logger logger = Logger.getLogger(ReportingBot.class.getName());

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && (update.getMessage() != null || update.getCallbackQuery() != null)) {
            NewMessageHandler handler = new NewMessageHandler(update, this);
            AbstractBotState botState = handler.getBotState();
            if (botState == null) {
                if (handler.getSendMessage() != null) {
                    SendHelper.sendMessage(handler.getSendMessage(), handler.getContext());
                }
                return;
            }
            botState.handleMessage();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public static void main(String[] args) throws Exception {
        //TODO логирование на отправку сообщений, логирование в тредах
        LogManager.getLogManager().readConfiguration
                (new FileInputStream("./src/main/resources/logging.properties"));
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(new ReportingBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
