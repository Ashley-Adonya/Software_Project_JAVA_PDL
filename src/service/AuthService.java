package service;

import dao.UserDAO;
import model.User;
import java.util.Locale;
import java.text.Normalizer;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User login(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null) {
            return null;
        }
        return userDAO.findByLoginAndPassword(login.trim(), password);
    }

    public boolean isAdmin(User user) {
        String role = normalizeRole(user);
        return role.contains("ADMIN")
            || role.contains("RESPONSABLE")
            || role.contains("MANAGER");
    }

    public boolean isStudent(User user) {
        String role = normalizeRole(user);
        return role.contains("STUDENT")
            || role.contains("ETUDIANT")
            || role.contains("ELEVE");
    }

    private String normalizeRole(User user) {
        if (user == null || user.getRole() == null) {
            return "";
        }
        String normalized = Normalizer.normalize(user.getRole(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('\u00A0', ' ')
                .strip()
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z]", "");
    }
}
