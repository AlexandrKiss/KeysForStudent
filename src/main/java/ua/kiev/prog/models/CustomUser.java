package ua.kiev.prog.models;

import org.telegram.telegrambots.meta.api.objects.Contact;

import javax.persistence.*;

@Entity
public class CustomUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private Long userID;
    private Long phoneNumber;
    private String firstName;
    private String lastName;

    public CustomUser() { }

    public CustomUser(Contact contact) {
        this.userID = Long.valueOf(contact.getUserID());
        this.phoneNumber = Long.valueOf(contact.getPhoneNumber());
        this.firstName = contact.getFirstName();
        this.lastName = contact.getLastName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "CustomUser{" +
                "userID=" + userID +
                ", phoneNumber=" + phoneNumber +
                ", firstName='" + '\'' + firstName + '\'' +
                ", lastName='" + '\'' + lastName + '\'' +
                '}';
    }
}
