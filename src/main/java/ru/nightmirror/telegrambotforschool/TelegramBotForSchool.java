package ru.nightmirror.telegrambotforschool;

public class TelegramBotForSchool {

    private static String TOKEN = "5075363214:AAF_-Wncdxa_nqbG2wsqakRqFv4bCU-AK2s";

    public static void main(String[] args) {
//        TOKEN = args[0];

        Data.getInstance().init();

        new Thread(new MessageHandler(TOKEN)).start();
    }
}
