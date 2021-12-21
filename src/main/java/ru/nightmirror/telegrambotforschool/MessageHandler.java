package ru.nightmirror.telegrambotforschool;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessageHandler implements Runnable {

    private TelegramBot bot; // About: https://github.com/pengrad/java-telegram-bot-api
    private int offset = 0;
    private Data data = Data.getInstance();
    private HashMap<User, String> userTempData;

    public MessageHandler(String token) {
        bot = new TelegramBot(token);
        userTempData = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Update> updates = bot.execute(new GetUpdates().limit(1).offset(offset).timeout(0)).updates();

                if (updates != null && !updates.isEmpty()) {
                    Update update = updates.get(0);
                    Message message = update.message();

                    offset = update.updateId() + 1;

                    if (message != null && message.text() != null) {
                        Chat chat = message.chat();
                        User user = message.from();

                        // Getting available class numbers
                        List<String> classNumbers = new ArrayList<>();
                        for (Class temp : data.getAll()) {
                            if (!classNumbers.contains(temp.getNumber())) classNumbers.add(temp.getNumber());
                        }

                        if (userTempData.containsKey(user)) {
                            String[] userSplitedData = (userTempData.get(user)+" ").split(" ");

                            // Back button
                            if (message.text().equalsIgnoreCase("Назад")) {
                                if (userSplitedData.length == 1) {
                                    userTempData.remove(user);

                                    Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                            Arrays.copyOf(classNumbers.toArray(), classNumbers.size(), String[].class))
                                            .oneTimeKeyboard(false)
                                            .resizeKeyboard(true);

                                    bot.execute(new SendMessage(chat.id(), "Выберите класс").replyMarkup(replyKeyboardMarkup));

                                    continue;
                                }

                                if (userSplitedData.length == 2) {
                                    userTempData.replace(user, userSplitedData[0]);

                                    // Getting available class profiles
                                    List<String> classProfiles = new ArrayList<>();
                                    for (Class temp : data.getAll()) {
                                        if (temp.getNumber().equalsIgnoreCase(userSplitedData[0]) && !classProfiles.contains(temp.getProfile())) classProfiles.add(temp.getProfile());
                                    }

                                    Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                            Arrays.copyOf(classProfiles.toArray(), classProfiles.size(), String[].class),
                                            new String[]{"Назад"})
                                            .oneTimeKeyboard(false)
                                            .resizeKeyboard(true);

                                    bot.execute(new SendMessage(chat.id(), "Выберите профиль").replyMarkup(replyKeyboardMarkup));

                                    continue;
                                }
                            }

                            if (userSplitedData.length == 1) {
                                // Getting available class profiles
                                List<String> classProfiles = new ArrayList<>();
                                for (Class temp : data.getAll()) {
                                    if (temp.getNumber().equalsIgnoreCase(userSplitedData[0]) && !classProfiles.contains(temp.getProfile())) classProfiles.add(temp.getProfile());
                                }

                                if (classProfiles.contains(message.text())) {
                                    // Getting available class days of timetable
                                    List<String> days = new ArrayList<>();
                                    for (Timetable temp : data.get(userSplitedData[0], message.text()).getTimetables()) {
                                        days.add(temp.getDay());
                                    }

                                    Keyboard replyKeyboardMarkup = null;

                                    /*
                                    Sorry, but I really do not know how to optimize
                                    the input of arguments depending on the length in a different way.
                                     */
                                    if (days.size() == 5) {
                                        replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                                new String[]{days.get(0)},
                                                new String[]{days.get(1)},
                                                new String[]{days.get(2)},
                                                new String[]{days.get(3)},
                                                new String[]{days.get(4)},
                                                new String[]{"Назад"})
                                                .oneTimeKeyboard(false)
                                                .resizeKeyboard(true);
                                    } else if (days.size() == 6) {
                                        replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                                new String[]{days.get(0)},
                                                new String[]{days.get(1)},
                                                new String[]{days.get(2)},
                                                new String[]{days.get(3)},
                                                new String[]{days.get(4)},
                                                new String[]{days.get(5)},
                                                new String[]{"Назад"})
                                                .oneTimeKeyboard(false)
                                                .resizeKeyboard(true);
                                    }

                                    if (replyKeyboardMarkup != null) {
                                        bot.execute(new SendMessage(chat.id(), "Выберите день").replyMarkup(replyKeyboardMarkup));

                                        userTempData.replace(user, userSplitedData[0] + " " + message.text());

                                        continue;
                                    }
                                } else {
                                    bot.execute(new SendMessage(chat.id(), "Такого профиля нет"));
                                }
                            }

                            if (userSplitedData.length == 2) {
                                // Getting available class days of timetable
                                List<String> days = new ArrayList<>();
                                for (Timetable temp : data.get(userSplitedData[0], userSplitedData[1]).getTimetables()) {
                                    days.add(temp.getDay());
                                }

                                if (days.contains(message.text())) {
                                    StringBuilder out = new StringBuilder(userSplitedData[0]+" "+userSplitedData[1]+" - "+message.text()+"\n\n");

                                    for (Lesson temp : data.getLessons(userSplitedData[0], userSplitedData[1], message.text())) {
                                        out.append(temp.getTime()+" > "+temp.getName()+"\n");
                                    }

                                    bot.execute(new SendMessage(chat.id(), out.toString()).parseMode(ParseMode.Markdown));
                                } else {
                                    bot.execute(new SendMessage(chat.id(), "Такого дня нет"));
                                }
                            }
                        } else {
                            if (classNumbers.contains(message.text())) {
                                userTempData.put(user, message.text());

                                // Getting available class profiles
                                List<String> classProfiles = new ArrayList<>();
                                for (Class temp : data.getAll()) {
                                    if (temp.getNumber().equalsIgnoreCase(message.text()) && !classProfiles.contains(temp.getProfile())) classProfiles.add(temp.getProfile());
                                }

                                Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                        Arrays.copyOf(classProfiles.toArray(), classProfiles.size(), String[].class),
                                        new String[]{"Назад"})
                                        .oneTimeKeyboard(false)
                                        .resizeKeyboard(true);

                                bot.execute(new SendMessage(chat.id(), "Выберите профиль").replyMarkup(replyKeyboardMarkup));
                            } else {
                                Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                        Arrays.copyOf(classNumbers.toArray(), classNumbers.size(), String[].class))
                                        .oneTimeKeyboard(false)
                                        .resizeKeyboard(true);

                                bot.execute(new SendMessage(chat.id(), "Пожалуйста, выберите класс").replyMarkup(replyKeyboardMarkup));
                            }
                        }
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
