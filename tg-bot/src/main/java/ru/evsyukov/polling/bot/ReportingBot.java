package ru.evsyukov.polling.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.polling.handlers.InlineMessageHandler;
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

    private final ThreadPoolTaskExecutor threadPoolExecutor;

    private final InlineMessageHandler inlineMessageHandler;

    @Autowired
    public ReportingBot(NewMessageHandler newMessageHandler,
                        ThreadPoolTaskExecutor threadPoolExecutor,
                        InlineMessageHandler inlineMessageHandler) {
        this.newMessageHandler = newMessageHandler;
        this.threadPoolExecutor = threadPoolExecutor;
        this.inlineMessageHandler = inlineMessageHandler;
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
        threadPoolExecutor.execute(() -> {
            try {
                if (update != null && update.getInlineQuery() != null) {
                    AnswerInlineQuery query = inlineMessageHandler.getInlineAnswer(update);
                    this.execute(query);
                    return;
                }
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
        });
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
