package service;

/**
 * Encapsule le résultat d'une opération de service avec un statut de succès et un message descriptif.
 * Cette classe permet une gestion uniforme des erreurs et des succès dans toute la couche métier.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class ServiceResult {
    private boolean success;
    private String message;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static ServiceResult ok(String message) {
        return new ServiceResult(true, message);
    }

    public static ServiceResult fail(String message) {
        return new ServiceResult(false, message);
    }
}
