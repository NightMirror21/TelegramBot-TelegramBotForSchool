package ru.nightmirror.telegrambotforschool;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessageHandler implements Runnable {

    // About: https://github.com/pengrad/java-telegram-bot-api
    private final TelegramBot bot;

    // To store the previous message from the user
    private static final HashMap<User, String> userTempData = new HashMap<>();

    private final Message message;
    private final Data data = Data.getInstance();

    public MessageHandler(TelegramBot bot, Update update) {
        this.bot = bot;
        this.message = update.message();
    }

    @Override
    public void run() {
        try {
            if (message != null && message.text() != null) {
                Chat chat = message.chat();
                User user = message.from();

                // Getting available class numbers
                List<String> classNumbers = new ArrayList<>();
                for (Class temp : data.getAll()) {
                    if (!classNumbers.contains(temp.getNumber())) classNumbers.add(temp.getNumber());
                }

                // Checking the previous message from this user
                if (userTempData.containsKey(user)) {

                    /*
                    i=0 - number of class; i=1 - profile
                    Example: 9 И -> {"9", "И"}
                     */
                    String[] userSplitData = (userTempData.get(user)+" ").split(" ");

                    // Click on the Back button
                    if (message.text().equalsIgnoreCase("Назад")) {

                        // If bot know only class -> Show classes again
                        if (userSplitData.length == 1) {
                            userTempData.remove(user);

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(classNumbers.toArray(), classNumbers.size(), String[].class))
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите класс").replyMarkup(replyKeyboardMarkup));

                            return;
                        }

                        // If bot know class and profile -> Show profiles again
                        if (userSplitData.length == 2) {
                            userTempData.replace(user, userSplitData[0]);

                            // Getting available class profiles
                            List<String> classProfiles = new ArrayList<>();
                            for (Class temp : data.getAll()) {
                                if (temp.getNumber().equalsIgnoreCase(userSplitData[0]) && !classProfiles.contains(temp.getProfile()))
                                    classProfiles.add(temp.getProfile());
                            }

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(classProfiles.toArray(), classProfiles.size(), String[].class),
                                    new String[]{"Назад"})
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите профиль").replyMarkup(replyKeyboardMarkup));

                            return;
                        }
                    }

                    // Bot know class from data; Now user wrote profile
                    if (userSplitData.length == 1) {

                        // Getting available class profiles
                        List<String> classProfiles = new ArrayList<>();
                        for (Class temp : data.getAll()) {
                            if (temp.getNumber().equalsIgnoreCase(userSplitData[0]) && !classProfiles.contains(temp.getProfile())) classProfiles.add(temp.getProfile());
                        }

                        if (classProfiles.contains(message.text())) {
                            // Getting available class days of timetable
                            List<String> days = new ArrayList<>();
                            for (Timetable temp : data.get(userSplitData[0], message.text()).getTimetables()) {
                                days.add(temp.getDay());
                            }

                            Keyboard replyKeyboardMarkup = null;

                            /*
                            Sorry, but I really do not know how to optimize
                            the input of arguments depending on the length in a different way
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

                                userTempData.replace(user, userSplitData[0] + " " + message.text());

                                return;
                            }
                        } else {
                            bot.execute(new SendMessage(chat.id(), "Такого профиля нет"));

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(classProfiles.toArray(), classProfiles.size(), String[].class),
                                    new String[]{"Назад"})
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите профиль").replyMarkup(replyKeyboardMarkup));
                        }
                    }

                    // Bot know class and profile from data; Now user wrote day
                    if (userSplitData.length == 2) {
                        // Getting available class days of timetable
                        List<String> days = new ArrayList<>();
                        for (Timetable temp : data.get(userSplitData[0], userSplitData[1]).getTimetables()) {
                            days.add(temp.getDay());
                        }

                        if (days.contains(message.text())) {
                            StringBuilder out = new StringBuilder(userSplitData[0]+" "+userSplitData[1]+" - "+message.text()+"\n\n");

                            for (Lesson temp : data.getLessons(userSplitData[0], userSplitData[1], message.text())) {
                                out.append(temp.getTime()+" > "+temp.getName()+"\n");
                            }

                            bot.execute(new SendMessage(chat.id(), out.toString()).parseMode(ParseMode.Markdown));
                        } else {
                            bot.execute(new SendMessage(chat.id(), "Такого дня нет"));

                            Keyboard replyKeyboardMarkup = null;

                            /*
                            Sorry, but I really do not know how to optimize
                            the input of arguments depending on the length in a different way
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
                            }
                        }
                    }
                } else {
                    // User is new and bot don't know what he wrote

                    // He wrote class -> Show profiles
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
                        // If channel created
                        if (message.text().equalsIgnoreCase("/start")) {
                            bot.execute(new SendMessage(chat.id(), "Привет!\nЯ бот, который знает расписание всех классов в главном здании школы 1375"));
                        }

                        // Bot don't couldn't recognize
                        Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                Arrays.copyOf(classNumbers.toArray(), classNumbers.size(), String[].class))
                                .oneTimeKeyboard(false)
                                .resizeKeyboard(true);

                        bot.execute(new SendMessage(chat.id(), "Пожалуйста, выберите класс").replyMarkup(replyKeyboardMarkup));
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
