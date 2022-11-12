package ru.evsyukov.polling.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import ru.evsyukov.utils.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InlineMessageHandler {

    private final List<String> cacheProjects;

    // Отправка валится с TelegramException если список слишком большой
    @Value("${inline-bot.max-result}")
    private Integer maxResult;

    @Autowired
    public InlineMessageHandler(List<String> cacheProjects) {
        this.cacheProjects = cacheProjects;
    }

    public AnswerInlineQuery getInlineAnswer(Update update) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(update.getInlineQuery().getId());
        if (update.getInlineQuery().getQuery().length() == 0) {
            answerInlineQuery.setResults(preparePrompt(update));
        } else {
            List<InlineQueryResult> result = prepareQueryAnswer(update.getInlineQuery().getQuery());
            answerInlineQuery.setResults(result);
        }
        return answerInlineQuery;
    }

    private List<InlineQueryResult> preparePrompt(Update update) {
        InlineQueryResultArticle inlineQueryResultArticle = new InlineQueryResultArticle();
        InputTextMessageContent itmc = new InputTextMessageContent();
        inlineQueryResultArticle.setId(update.getInlineQuery().getId());
        itmc.setMessageText(Message.INLINE_QUERY_PROMPT);
        inlineQueryResultArticle.setInputMessageContent(itmc);
        inlineQueryResultArticle.setTitle(Message.INLINE_QUERY_PROMPT);
        return Collections.singletonList(inlineQueryResultArticle);
    }

    private List<InlineQueryResult> prepareQueryAnswer(String receive) {
        List<InlineQueryResult> result = new ArrayList<>();
        List<String> projects = cacheProjects.stream()
                .filter(proj -> StringUtils.containsIgnoreCase(proj, receive))
                .limit(maxResult)
                .collect(Collectors.toList());
        int i = 0;
        for (String project : projects) {
            InlineQueryResultArticle inlineQueryResultArticle = new InlineQueryResultArticle();
            inlineQueryResultArticle.setId(String.valueOf(++i));
            InputTextMessageContent itmc = new InputTextMessageContent();
            itmc.setMessageText(project);
            inlineQueryResultArticle.setInputMessageContent(itmc);
            inlineQueryResultArticle.setTitle(project);
            result.add(inlineQueryResultArticle);
        }
        return result;
    }
}
