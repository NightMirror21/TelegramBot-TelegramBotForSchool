package ru.nightmirror.telegrambotforschool.data;

import com.google.gson.Gson;
import ru.nightmirror.telegrambotforschool.data.classes.Lesson;
import ru.nightmirror.telegrambotforschool.data.classes.SchoolClass;
import ru.nightmirror.telegrambotforschool.data.classes.Timetable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Data {

    private static Data instance;
    private final String filename = "data.json";
    private long lastTimeModified;
    private List<SchoolClass> schoolClassCache;

    private Data() {
        copyFile();
    }

    public static Data getInstance() {
        if (instance == null) instance = new Data();
        return instance;
    }

    public void copyFile() {
        try {
            File outputFile = new File(filename);
            if (!outputFile.exists()) {
                outputFile.createNewFile();

                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(filename);

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

    public List<SchoolClass> getClasses() {
        File file = new File(filename);

        if (file.lastModified() != lastTimeModified) {
            try (
                    FileReader reader = new FileReader(filename, StandardCharsets.UTF_8)
            ) {
                Gson gson = new Gson();
                schoolClassCache = Arrays.asList(gson.fromJson(reader, SchoolClass[].class));
                lastTimeModified = file.lastModified();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return schoolClassCache;
    }

    public SchoolClass getClass(String number, String profile) {
        for (SchoolClass temp : getClasses()) {
            if (temp.getNumber().equalsIgnoreCase(number) && temp.getProfile().equalsIgnoreCase(profile)) {
                return temp;
            }
        }

        return null;
    }

    public List<Lesson> getLessons(String number, String profile, String day) {
        for (SchoolClass temp : getClasses()) {
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



