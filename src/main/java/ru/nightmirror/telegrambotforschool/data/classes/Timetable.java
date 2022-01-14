package ru.nightmirror.telegrambotforschool.data.classes;

import java.util.List;

public class Timetable {
    private String day;
    private List<Lesson> lessons;

    public Timetable(String day, List<Lesson> lessons) {
        this.day = day;
        this.lessons = lessons;
    }

    public String getDay() {
        return day;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}
