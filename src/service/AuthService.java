package service;

import dao.UserDAO;
import model.User;
import java.util.Locale;
import java.text.Normalizer;

/**
 * Service gérant l'authentification et les autorisations des utilisateurs.
 * 
 * Nous avons conçu ce service pour centraliser la logique de connexion et la vérification
 * des rôles (Admin, Étudiant), en utilisant une normalisation stricte des chaînes de caractères.
 */
public class AuthService {
    private final UserDAO userDAO;

    /**
     * Default constructor initializing the UserDAO.
     */
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user with login and password credentials.
     *
     * @param login    the user login
     * @param password the user password
     * @return the authenticated User if credentials match, or null otherwise
     */
    public User login(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null) {
            return null;
        }
        return userDAO.findByLoginAndPassword(login.trim(), password);
    }

    /**
     * Checks if the given user has an administrative role (ADMIN, RESPONSABLE, or MANAGER).
     *
     * @param user the user to check
     * @return true if the user has an admin role, false otherwise
     */
    public boolean isAdmin(User user) {
        String role = normalizeRole(user);
        return role.contains("ADMIN")
            || role.contains("RESPONSABLE")
            || role.contains("MANAGER");
    }

    /**
     * Checks if the given user has a student role (STUDENT, ETUDIANT, or ELEVE).
     *
     * @param user the user to check
     * @return true if the user has a student role, false otherwise
     */
    public boolean isStudent(User user) {
        String role = normalizeRole(user);
        return role.contains("STUDENT")
            || role.contains("ETUDIANT")
            || role.contains("ELEVE");
    }

    /**
     * Normalizes a user's role string by removing diacritics, extra spaces, and non-alphabetic characters.
     *
     * @param user the user whose role to normalize
     * @return the normalized uppercase role string, or empty string if user or role is null
     */
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
