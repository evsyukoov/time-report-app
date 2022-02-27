package stateMachine;

import bot.BotContext;

import static stateMachine.State.*;

public class BotStateFactory {

    public static AbstractBotState createBotState(State state, BotContext context) {
        if (state == REGISTER_NAME) {
            return new RegisterName(context);
        } else if (state == MENU) {
            return new Menu(context);
        } else if (state == MENU_CHOICE) {
            return new MenuChoice(context);
        } else if (state == VACATION) {
            return new Vacation(context);
        } else if (state == NOTIFICATION_CHOICE) {
            return new NotificationChoice(context);
        }  else if (state == REPORT_TYPE) {
            return new ReportType(context);
        } else if (state == CHOOSE_DAY) {
            return new ChooseDay(context);
        } else if (state == PARSE_DATE) {
            return new ParseDate(context);
        } else if (state == SELECT_PROJECT) {
            return new SelectProject(context);
        }
        return null;
    }
}
