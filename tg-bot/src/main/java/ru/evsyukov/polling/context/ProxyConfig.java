package ru.evsyukov.polling.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
@Slf4j
public class ProxyConfig {

    @Bean
    DefaultBotOptions botOptions(
            @Value("${polling-bot.proxy.enabled:false}") boolean proxyEnabled,
            @Value("${polling-bot.proxy.host:}") String proxyHost,
            @Value("${polling-bot.proxy.port:0}") int proxyPort,
            @Value("${polling-bot.proxy.type:SOCKS5}") DefaultBotOptions.ProxyType proxyType) {
        DefaultBotOptions options = new DefaultBotOptions();
        if (proxyEnabled) {
            options.setProxyHost(proxyHost);
            options.setProxyPort(proxyPort);
            options.setProxyType(proxyType);
            log.info("Telegram bot proxy enabled: {}:{} ({})", proxyHost, proxyPort, proxyType);
        }
        return options;
    }
}
