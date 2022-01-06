package ru.nightmirror.telegrambotforschool;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;

import java.util.List;

public class TelegramBotForSchool {

    private static String token;
    private static TelegramBot bot; // About: https://github.com/pengrad/java-telegram-bot-api
    private static int offset = 0;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You forgot to specify the token in the arguments at startup");
            return;
        }
        token = args[0];

        bot = new TelegramBot(token);

        Data.getInstance().init();

        while (true) {
            try {
                List<Update> updates = bot.execute(new GetUpdates().limit(1).offset(offset).timeout(0)).updates();

                if (updates != null && !updates.isEmpty()) {
                    Update update = updates.get(0);

                    offset = update.updateId() + 1;

                    // Processing each message in a new thread
                    new Thread(new MessageHandler(bot, update)).start();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
