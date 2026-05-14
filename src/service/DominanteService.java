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

    public DominanteService() {
        this.dominanteDAO = new DominanteDAO();
    }

    public ArrayList<Dominante> listAll() {
        List<Dominante> dominantes = dominanteDAO.findAll();
        if (dominantes instanceof ArrayList<Dominante> arrayList) {
            return arrayList;
        }
        return new ArrayList<>(dominantes);
    }

    public ServiceResult create(Dominante dominante) {
        String validation = validateDominante(dominante);
        if (validation != null) {
            return ServiceResult.fail(validation);
        }
        int id = dominanteDAO.create(dominante);
        if (id <= 0) {
            return ServiceResult.fail("Creation dominante echouee");
        }
        return ServiceResult.ok("Dominante creee (id=" + id + ")");
    }

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
        return ServiceResult.ok("Dominante modifiee");
    }

    public ServiceResult deleteById(int dominanteId) {
        if (dominanteId <= 0) {
            return ServiceResult.fail("Id dominante invalide");
        }
        boolean ok = dominanteDAO.deleteById(dominanteId);
        if (!ok) {
            return ServiceResult.fail("Suppression dominante echouee");
        }
        return ServiceResult.ok("Dominante supprimee");
    }

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
