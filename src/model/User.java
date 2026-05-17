package model;

/**
 * Represents a user of the system (administrator or student).
 * Contains login credentials, profile (role, promotion), activation state,
 * and personal information (full name).
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class User {
    private int id;
    private String login;
    private String password;
    private String fullName;
    private String role;
    private String promo;
    private boolean active;

    public User() {
    }
    public User(int id, String login, String password, String fullName, String role, String promo, boolean active) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.promo = promo;
        this.active = active;
    }
    public int getId() { 
        return id;
     }
    public void setId(int id) { 
        this.id = id;
     }
    public String getLogin() { 
        return login;
     }
    public void setLogin(String login) {
         this.login = login;
         }
    public String getPassword() {
         return password;
    }
    public void setPassword(String password) {
         this.password = password; 
    }
    public String getFullName() {
         return fullName; 
    }
    public void setFullName(String fullName) {
         this.fullName = fullName; 
    }
    public String getRole() {
         return role; 
    }
    public void setRole(String role) {
     this.role = role; 
    }
    public String getPromo() {
     return promo; 
    }
    public void setPromo(String promo) {
         this.promo = promo; 
    }
    public boolean isActive() {
         return active; 
    }
    public void setActive(boolean active) {
         this.active = active; 
    }
}
