package com.mobapps.covidcontacttracer;

//Purpose of the class file:
/*

 */

public class User {
    public String name, email, age, phone,city_residence,gender,Status;
    public User() {// Default constructor that takes in no parameters and is required as such by Firebase SDK.
    }

    public User(String name, String email, String age, String phone, String city_residence, String gender, String status) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.phone = phone;
        this.city_residence=city_residence;
        this.gender=gender;
        this.Status=status;
    }
}
