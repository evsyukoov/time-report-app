package ru.evsyukov.polling.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.polling.handlers.NewMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evsyukov.polling.stateMachine.BotState;
import ru.evsyukov.polling.utils.SendHelper;
import ru.evsyukov.polling.utils.Utils;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@Component
@Slf4j
public class ReportingBot extends TelegramLongPollingBot {

    private String token;

    private String botName;

    private final NewMessageHandler newMessageHandler;

    @Autowired
    public ReportingBot(NewMessageHandler newMessageHandler) {
        this.newMessageHandler = newMessageHandler;
    }

    @Value("${polling-bot.token}")
    public void setToken(String token) {
        this.token = token;
    }

    @Value("${polling-bot.name}")
    public void setBotName(String botName) {
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update != null && (update.getMessage() != null || update.getCallbackQuery() != null)) {
                log.info("Received request by polling with client-id: {}", Utils.getCurrentChat(update).getId());
                Client client = newMessageHandler.getClient(update);
                BotContext context = newMessageHandler.initBotContext(client, update, this);
                BotState botState = newMessageHandler.getBotState(context);
                SendMessage sendMessage = newMessageHandler.getSendMessage(context);
                if (sendMessage != null) {
                    log.info("Send response to client {}", context.getClient());
                    SendHelper.sendMessage(sendMessage, context);
                    return;
                }
                botState.handleMessage(context);
            }
        } catch (Exception e) {
            log.error("Fatal error: ", e);
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

    @PostConstruct
    public void startPollingTelegram() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }
}
