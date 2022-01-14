package ru.nightmirror.telegrambotforschool.data.classes;

import java.util.List;

public class SchoolClass {
    private String number;
    private String profile;
    private List<Timetable> timetables;

    public SchoolClass(String number, String profile, List<Timetable> timetables) {
        this.number = number;
        this.profile = profile;
        this.timetables = timetables;
    }

    public String getNumber() {
        return number;
    }

    public String getProfile() {
        return profile;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }
}
