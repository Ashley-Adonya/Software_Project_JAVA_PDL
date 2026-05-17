package service;

import java.util.ArrayList;
import java.util.List;

import dao.DominanteDAO;
import model.Dominante;

/**
 * Service de gestion des dominantes (domaines d'études proposés).
 * Offre des opérations de création, modification, suppression et récupération des dominantes
 * avec validation des données et messages d'erreur détaillés.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class DominanteService {
    private final DominanteDAO dominanteDAO;

    /**
     * Default constructor initializing the DominanteDAO.
     */
    public DominanteService() {
        this.dominanteDAO = new DominanteDAO();
    }

    /**
     * Retrieves all dominantes from cache or database.
     *
     * @return an ArrayList of all dominantes
     */
    public ArrayList<Dominante> listAll() {
        List<Dominante> dominantes = CacheManager.getOrLoad("dominante:all", () -> dominanteDAO.findAll());
        if (dominantes instanceof ArrayList<Dominante> arrayList) {
            return arrayList;
        }
        return new ArrayList<>(dominantes);
    }

    /**
     * Finds a dominante by its ID, with caching.
     *
     * @param id the ID of the dominante
     * @return the dominante if found, or null if the ID is invalid or not found
     */
    public Dominante findById(int id) {
        if (id <= 0) return null;
        return CacheManager.getOrLoad("dominante:id:" + id, () -> dominanteDAO.findById(id));
    }

    /**
     * Creates a new dominante after validation.
     * Invalidates relevant caches on success.
     *
     * @param dominante the dominante to create
     * @return ServiceResult indicating success with the new ID, or failure with an error message
     */
    public ServiceResult create(Dominante dominante) {
        String validation = validateDominante(dominante);
        if (validation != null) {
            return ServiceResult.fail(validation);
        }
        int id = dominanteDAO.create(dominante);
        if (id <= 0) {
            return ServiceResult.fail("Creation dominante echouee");
        }
        CacheManager.invalidatePrefix("dominante:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Dominante creee (id=" + id + ")");
    }

    /**
     * Updates an existing dominante after validation.
     * Invalidates relevant caches on success.
     *
     * @param dominante the dominante with updated fields
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult update(Dominante dominante) {
        if (dominante == null || dominante.getId() <= 0) {
            return ServiceResult.fail("Dominante invalide");
        }
        String validation = validateDominante(dominante);
        if (validation != null) {
            return ServiceResult.fail(validation);
        }
        boolean ok = dominanteDAO.update(dominante);
        if (!ok) {
            return ServiceResult.fail("Modification dominante echouee");
        }
        CacheManager.invalidatePrefix("dominante:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Dominante modifiee");
    }

    /**
     * Deletes a dominante by its ID.
     * Invalidates relevant caches on success.
     *
     * @param dominanteId the ID of the dominante to delete
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult deleteById(int dominanteId) {
        if (dominanteId <= 0) {
            return ServiceResult.fail("Id dominante invalide");
        }
        boolean ok = dominanteDAO.deleteById(dominanteId);
        if (!ok) {
            return ServiceResult.fail("Suppression dominante echouee");
        }
        CacheManager.invalidatePrefix("dominante:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Dominante supprimee");
    }

    /**
     * Validates a dominante object, checking that name and code are present and non-empty.
     *
     * @param dominante the dominante to validate
     * @return null if valid, or an error message string if invalid
     */
    private String validateDominante(Dominante dominante) {
        if (dominante == null) {
            return "Dominante vide";
        }
        if (dominante.getName() == null || dominante.getName().trim().isEmpty()) {
            return "Nom de dominante obligatoire";
        }
        if (dominante.getCode() == null || dominante.getCode().trim().isEmpty()) {
            return "Code dominante obligatoire";
        }
        return null;
    }
}
