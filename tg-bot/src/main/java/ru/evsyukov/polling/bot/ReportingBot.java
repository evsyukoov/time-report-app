package ru.evsyukov.polling.bot;

import ru.evsyukov.polling.handlers.NewMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.evsyukov.polling.stateMachine.AbstractBotState;
import ru.evsyukov.polling.utils.SendHelper;

import javax.annotation.PostConstruct;


@Component
@Slf4j
public class ReportingBot extends TelegramLongPollingBot {

    private String token;

    private String botName;

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

    @PostConstruct
    public void startPollingTelegram() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }
}
