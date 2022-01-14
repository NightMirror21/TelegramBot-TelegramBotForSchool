package ru.nightmirror.telegrambotforschool.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import ru.nightmirror.telegrambotforschool.data.Data;
import ru.nightmirror.telegrambotforschool.data.UserData;
import ru.nightmirror.telegrambotforschool.data.classes.Lesson;
import ru.nightmirror.telegrambotforschool.data.classes.SchoolClass;
import ru.nightmirror.telegrambotforschool.data.classes.Timetable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessageHandler implements Runnable {

    // To store the previous message from the user
    private static final HashMap<User, UserData> userCache = new HashMap<>();
    // About: https://github.com/pengrad/java-telegram-bot-api
    private final TelegramBot bot;
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

                List<String> classNumbers = new ArrayList<>();
                for (SchoolClass temp : data.getClasses()) {
                    if (!classNumbers.contains(temp.getNumber())) classNumbers.add(temp.getNumber());
                }

                // Checking the data for this user
                if (userCache.containsKey(user) && userCache.get(user).isClassChosen()) {
                    UserData userData = userCache.get(user);

                    if (message.text().equalsIgnoreCase("Назад")) {
                        // [BACK BUTTON] If bot know only class -> Show classes again
                        if (userData.isClassChosen() && !userData.isProfileChosen()) {
                            userCache.replace(user, new UserData());

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(classNumbers.toArray(), classNumbers.size(), String[].class))
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите класс").replyMarkup(replyKeyboardMarkup));

                            return;
                        }

                        // [BACK BUTTON] If bot know class and profile -> Show profiles again
                        if (userData.isClassChosen() && userData.isProfileChosen()) {
                            userCache.replace(user, userData.setProfile(null));

                            List<String> classProfiles = getProfiles(userData);

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
                    if (userData.isClassChosen() && !userData.isProfileChosen()) {

                        List<String> classProfiles = getProfiles(userData);

                        if (classProfiles.contains(message.text())) {
                            userData = userData.setProfile(message.text());

                            List<String> days = getDays(userData);

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(days.toArray(), days.size(), String[].class),
                                    new String[]{"Назад"})
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите день").replyMarkup(replyKeyboardMarkup));
                            userCache.replace(user, userData.setProfile(message.text()));

                            return;
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
                    if (userData.isClassChosen() && userData.isProfileChosen()) {

                        List<String> days = getDays(userData);

                        if (days.contains(message.text())) {
                            StringBuilder out = new StringBuilder("*" + userData.getClassNumber() + " " + userData.getProfile() + " - " + message.text() + "*\n\n");

                            for (Lesson temp : data.getLessons(userData.getClassNumber(), userData.getProfile(), message.text())) {
                                out.append(temp.getTime() + " > " + temp.getName() + "\n");
                            }

                            bot.execute(new SendMessage(chat.id(), out.toString()).parseMode(ParseMode.Markdown));
                        } else {
                            bot.execute(new SendMessage(chat.id(), "Такого дня нет"));

                            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                    Arrays.copyOf(days.toArray(), days.size(), String[].class),
                                    new String[]{"Назад"})
                                    .oneTimeKeyboard(false)
                                    .resizeKeyboard(true);

                            bot.execute(new SendMessage(chat.id(), "Выберите день").replyMarkup(replyKeyboardMarkup));
                        }
                    }
                } else {
                    UserData userData = new UserData();

                    // He wrote class -> Show profiles
                    if (classNumbers.contains(message.text())) {
                        userCache.put(user, userData.setClass(message.text()));

                        List<String> classProfiles = getProfiles(userData);

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

                        if (!userCache.containsKey(user)) {
                            bot.execute(new SendMessage(chat.id(), "Привет!\n" +
                                    "Я бот, который знает расписание всех классов в главном здании школы 1375\n\n" +
                                    "Пожалуйста, выберите класс").replyMarkup(replyKeyboardMarkup));
                            userCache.put(user, userData);
                        } else {
                            bot.execute(new SendMessage(chat.id(), "Пожалуйста, выберите класс").replyMarkup(replyKeyboardMarkup));
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private List<String> getProfiles(UserData userData) {
        List<String> classProfiles = new ArrayList<>();
        for (SchoolClass temp : data.getClasses()) {
            if (temp.getNumber().equalsIgnoreCase(userData.getClassNumber()) && !classProfiles.contains(temp.getProfile()))
                classProfiles.add(temp.getProfile());
        }
        return classProfiles;
    }

    private List<String> getDays(UserData userData) {
        List<String> days = new ArrayList<>();
        for (Timetable temp : data.getClass(userData.getClassNumber(), userData.getProfile()).getTimetables()) {
            days.add(temp.getDay());
        }
        return days;
    }
}
