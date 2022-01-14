package ru.nightmirror.telegrambotforschool.data.classes;

public class Lesson {
    private String name;
    private String time;

    public Lesson(String name, String time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
