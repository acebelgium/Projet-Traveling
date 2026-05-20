package com.example.traveling.path;

import java.io.Serializable;
import java.util.List;

public class Parcours implements Serializable {
    private String titre;
    private List<Lieu> etapes;
    private String niveauEffort;
    private double budgetTotal;
    private int dureeTotaleMinutes;
    private double distanceTotaleKM;

    public Parcours(String titre, List<Lieu> etapes, String niveauEffort) {
        this.titre = titre;
        this.etapes = etapes;
        this.niveauEffort = niveauEffort;
        calculerMetriques();
    }

    private void calculerMetriques() {
        this.budgetTotal = 0;
        this.dureeTotaleMinutes = 0;
        this.distanceTotaleKM = 0;
        
        for (int i = 0; i < etapes.size(); i++) {
            Lieu l = etapes.get(i);
            this.budgetTotal += l.getPrix();
            this.dureeTotaleMinutes += l.getTempsVisiteMinutes();
            
            if (i > 0) {
                Lieu prec = etapes.get(i - 1);
                double d = calculerDistanceKM(prec.getLatitude(), prec.getLongitude(), l.getLatitude(), l.getLongitude());
                this.distanceTotaleKM += d;
                // Temps de trajet : 5km/h -> 1km = 12 min
                this.dureeTotaleMinutes += (int)(d * 12);
            }
        }
    }

    private double calculerDistanceKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) 
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.853159616;
    }

    public int getCaloriesBrulees() {
        // Environ 55 kcal par km marché
        return (int) (distanceTotaleKM * 55);
    }

    public double getCo2EconomiseKG() {
        // Une voiture moyenne émet ~0.12kg de CO2 par km
        return distanceTotaleKM * 0.12;
    }

    public void setEtapes(List<Lieu> etapes) {
        this.etapes = etapes;
        calculerMetriques();
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public List<Lieu> getEtapes() { return etapes; }
    public int getNbEtapes() { return etapes.size(); }
    public double getBudgetTotal() { return budgetTotal; }
    public int getDureeTotaleMinutes() { return dureeTotaleMinutes; }
    public String getNiveauEffort() { return niveauEffort; }
    public double getDistanceTotaleKM() { return distanceTotaleKM; }
}
