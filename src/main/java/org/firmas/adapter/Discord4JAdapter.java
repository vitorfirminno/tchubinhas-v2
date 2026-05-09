package org.firmas.adapter;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.firmas.application.port.out.Discord4JOutputPort;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@ApplicationScoped
public class Discord4JAdapter implements Discord4JOutputPort {

    private static Logger logger = Logger.getLogger(Discord4JAdapter.class.getName());

    private GatewayDiscordClient gateway;


    @Override
    public void onStart(StartupEvent ev) {
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null || token.isEmpty()){
            logger.warning("DISCORD_TOKEN não configurada");
            return;
        }

        Thread botThread = new Thread(
                () -> {
                    DiscordClient.create(token)
                            .withGateway(client -> {
                                Mono<?> onMessageCreate = client.on(MessageCreateEvent.class, event -> {
                                    if (event.getMessage().getAuthor().map(u -> !u.isBot()).orElse(false)) {
                                        return event.getMessage().getChannel()
                                                .flatMap(channel ->
                                                        channel.createMessage("Pong " + event.getMessage().getContent()));
                                    }
                                    return Mono.empty();
                                }).then();

                                return client.onDisconnect()
                                        .doFirst(() -> logger.info("BOT conectado!"))
                                        .doFinally(signal -> logger.info("BOT desconectado!"))
                                        .then(onMessageCreate);
                            })
                            .block();
                }
        );
        botThread.setDaemon(true);
        botThread.setName("Discord4J-Bot");
        botThread.start();
        logger.info("Bot iniciando em thread separada");


    }
}
