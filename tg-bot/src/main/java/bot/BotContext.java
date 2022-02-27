package bot;

import hibernate.entities.Client;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BotContext {
    private final ReportingBot bot;
    private final Update update;
    private final Client client;
    private final boolean callBackQuery;
    private final String message;


    public BotContext(ReportingBot bot, Update update,
                      Client client, boolean callBackQuery,
                      String message) {
        this.bot = bot;
        this.update = update;
        this.client = client;
        this.callBackQuery = callBackQuery;
        this.message = message;
    }

    public ReportingBot getBot() {
        return bot;
    }

    public Update getUpdate() {
        return update;
    }

    public Client getClient() {
        return client;
    }

    public boolean isCallBackQuery() {
        return callBackQuery;
    }

    public String getMessage() {
        return message;
    }
}
