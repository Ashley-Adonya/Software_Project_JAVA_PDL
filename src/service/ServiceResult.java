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

    /**
     * Creates a service result with a success status and descriptive message.
     *
     * @param success true if the operation succeeded, false otherwise
     * @param message a description of the result
     */
    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Returns whether the operation was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the descriptive message for this result.
     *
     * @return the result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Creates a successful result with the given message.
     *
     * @param message the success description
     * @return a new ServiceResult with success=true
     */
    public static ServiceResult ok(String message) {
        return new ServiceResult(true, message);
    }

    /**
     * Creates a failed result with the given message.
     *
     * @param message the failure description
     * @return a new ServiceResult with success=false
     */
    public static ServiceResult fail(String message) {
        return new ServiceResult(false, message);
    }
}
