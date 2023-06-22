package christine.moldovan.recycle.loginscreen;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * user class to keep track of registered user
 * and their data (profile info)
 */

public class User {

    private String email;
    private String fullName;
    private String phone;
    private boolean isAdmin;

    public User() {
    }

    public User(String fullName, String email, String phone, boolean isAdmin) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.isAdmin = isAdmin;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fullName", fullName);
        result.put("email", email);
        result.put("phone", phone);
        return result;
    }
}
