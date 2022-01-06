package ru.nightmirror.telegrambotforschool;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class Data {

    private final String filename = "data.json";
    private static Data instance;

    public static Data getInstance() {
        if (instance == null) instance = new Data();
        return instance;
    }

    public void init() {
        try {
            File outputFile = new File("data.json");
            if (!outputFile.exists()) {
                outputFile.createNewFile();

                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("data.json");

                OutputStream outputStream = new FileOutputStream(outputFile);
                outputStream.write(inputStream.read());
                outputStream.flush();

                int i;
                while ((i = inputStream.read()) != -1) {
                    outputStream.write(i);
                }

                outputStream.flush();

                outputStream.close();
                inputStream.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public List<Class> getAll() {
        try (
                FileReader reader = new FileReader("data.json", Charset.forName("UTF-8"));
                ) {
            Gson gson = new Gson();
            Classes classes = gson.fromJson(reader, Classes.class);

            return classes.getClasses();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public Class get(String number, String profile) {
        for (Class temp : getAll()) {
            if (temp.getNumber().equalsIgnoreCase(number) && temp.getProfile().equalsIgnoreCase(profile)) {
                return temp;
            }
        }

        return null;
    }

    public List<Lesson> getLessons(String number, String profile, String day) {
        for (Class temp : getAll()) {
            if (temp.getNumber().equalsIgnoreCase(number) && temp.getProfile().equalsIgnoreCase(profile)) {
                for (Timetable timetable : temp.getTimetables()) {
                    if (timetable.getDay().equalsIgnoreCase(day)) {
                        return timetable.getLessons();
                    }
                }
            }
        }

        return null;
    }
}

/*
Why all these classes?

These classes allow the GSON library to decompose data from a json file into classes.
The data in this form looks more structured.
 */

class Classes {
    List<Class> classes;

    public Classes(List<Class> classes) {
        this.classes = classes;
    }

    public List<Class> getClasses() {
        return classes;
    }

    public void setClasses(List<Class> classes) {
        this.classes = classes;
    }
}

class Class {
    private String number;
    private String profile;
    private List<Timetable> timetables;

    public Class(String number, String profile, List<Timetable> timetables) {
        this.number = number;
        this.profile = profile;
        this.timetables = timetables;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
    }
}

class Timetable {
    private String day;
    private List<Lesson> lessons;

    public Timetable(String day, List<Lesson> lessons) {
        this.day = day;
        this.lessons = lessons;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
}

class Lesson {
    private String name;
    private String time;

    public Lesson(String name, String time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
