package model;

import java.awt.Color;

/**
 * Représente une dominante (domaine d'étude/spécialité) proposée par l'établissement.
 * Une dominante regroupe plusieurs sessions de présentation et dispose de caractéristiques
 * visuelles (couleur) et administratives (responsable, description, code).
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class Dominante {
    private int id;
    private String code;
    private String name;
    private String responsibleName;
    private String description;
    private String color;
    private boolean active;

    public Dominante() {
    }

    public Dominante(int id, String code, String name, String responsibleName, String description, String color, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.responsibleName = responsibleName;
        this.description = description;
        this.color = color;
        this.active = active;
    }

    public int getId() {
         return id; 
    }
    public void setId(int id) {
         this.id = id; 
    }
    public String getCode() {
         return code; 
    }
    public void setCode(String code) { this.code = code; }
    public String getName() {
         return name; 
    }
    public void setName(String name) {
         this.name = name; 
    }
    public String getResponsibleName() {
         return responsibleName; 
    }
    public void setResponsibleName(String responsibleName) {
         this.responsibleName = responsibleName; 
    }
    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) {
         this.description = description; 
    }
    public String getColor() {
         return color; 
    }
    public void setColor(String color) {
         this.color = color; 
    }
    public void setColor(Color color) {
         this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    public boolean isActive() {
         return active; 
    }
    public void setActive(boolean active) {
         this.active = active; 
    }

}
