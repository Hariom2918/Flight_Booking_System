package fbs_user;

public class Client extends AbstractUser {
    private String passportNumber;

    public Client(String userId, String name, String email, String phoneNumber, String passportNumber) {
        super(userId, name, email, phoneNumber);
        this.passportNumber = passportNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    @Override
    public void displayDetails() {
        System.out.println("Passenger ID: " + userId + ", Name: " + name + ", Email: " + email);
    }
}