package com.example.emergencyapp;

public class ContactModel {
    private String id;  // Firebase unique key
    private String name;
    private String phone;
    private String bloodGroup;

    public ContactModel() {
        // Default constructor required for calls to DataSnapshot.getValue(ContactModel.class)
    }

    public ContactModel(String name, String phone, String bloodGroup) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }
}
