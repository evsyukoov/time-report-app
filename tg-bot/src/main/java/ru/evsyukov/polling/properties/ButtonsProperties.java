package ru.evsyukov.polling.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "polling-bot.buttons")
//@PropertySource(value = "classpath:application.yaml", encoding = "ISO-8859-1")
@Component
public class ButtonsProperties {

    private List<String> actionsMenu;

    private List<String> days;

    public List<String> getActionsMenu() {
        return actionsMenu;
    }

    public void setActionsMenu(List<String> actionsMenu) {
        this.actionsMenu = actionsMenu;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }
}
