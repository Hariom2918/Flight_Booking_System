package fbs_user;

public class Manager extends AbstractUser {
    private String adminId;

    public Manager(String userId, String name, String email, String phoneNumber, String adminId) {
        super(userId, name, email, phoneNumber);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    @Override
    public void displayDetails() {
        System.out.println("Admin ID: " + adminId + ", Name: " + name);
    }
}