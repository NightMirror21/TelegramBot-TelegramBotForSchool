package ru.nightmirror.telegrambotforschool.data;

public class UserData {
    private String classNumber;
    private String profile;

    public UserData() {
        this.classNumber = null;
        this.profile = null;
    }

    public String getClassNumber() {
        return classNumber;
    }

    public String getProfile() {
        return profile;
    }

    public UserData setClass(String classNumber) {
        this.classNumber = classNumber;
        return this;
    }

    public UserData setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public Boolean isClassChosen() {
        return classNumber != null;
    }

    public Boolean isProfileChosen() {
        return profile != null;
    }
}
