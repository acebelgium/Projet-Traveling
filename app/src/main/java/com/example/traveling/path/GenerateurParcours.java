package com.example.traveling.path;
import com.example.traveling.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateurParcours {

    public static List<Parcours> genererOptions(List<Lieu> tousLesLieux, List<Lieu> favoris,
                                                double budgetMax, int effortMax, boolean sensiblePluie) {
        List<Parcours> options = new ArrayList<>();

        // Si l'utilisateur a fait une sélection, on n'utilise QUE ses favoris
        List<Lieu> baseLieux = favoris.isEmpty() ? tousLesLieux : favoris;

        options.add(creerOption(baseLieux, "Itinéraire Économique", budgetMax * 0.5, effortMax, sensiblePluie));
        options.add(creerOption(baseLieux, "Itinéraire Équilibré", budgetMax * 0.8, effortMax, sensiblePluie));
        options.add(creerOption(baseLieux, "Itinéraire Confort", budgetMax, Math.max(0, effortMax - 1), sensiblePluie));

        return options;
    }

    private static Parcours creerOption(List<Lieu> sources, String nom,
                                        double budgetCible, int effortCible, boolean eviterPluie) {
        List<Lieu> selection = new ArrayList<>();
        double budgetActuel = 0;
        
        // 1. Sélection selon budget
        for (Lieu l : sources) {
            if (budgetActuel + l.getPrix() <= budgetCible) {
                selection.add(l);
                budgetActuel += l.getPrix();
            }
            if (selection.size() >= 8) break; 
        }

        // 2. Calcul du chemin avec temps de trajet RÉEL (Marche à 5km/h)
        List<Lieu> cheminOrdonne = trierGeographiquement(selection);

        String effortLabel = (effortCible <= 0) ? "Facile" : (effortCible == 1) ? "Moyen" : "Intense";
        Parcours p = new Parcours(nom, cheminOrdonne, effortLabel);
        
        // Calculer les horaires réels
        calculerHoraires(p);
        
        return p;
    }

    private static List<Lieu> trierGeographiquement(List<Lieu> lieux) {
        if (lieux.size() <= 1) return lieux;
        List<Lieu> aVisiter = new ArrayList<>(lieux);
        List<Lieu> resultat = new ArrayList<>();
        
        Lieu actuel = aVisiter.remove(0);
        resultat.add(actuel);

        while (!aVisiter.isEmpty()) {
            Lieu plusProche = null;
            double distMin = Double.MAX_VALUE;
            for (Lieu l : aVisiter) {
                double d = calculerDistanceKM(actuel.getLatitude(), actuel.getLongitude(), l.getLatitude(), l.getLongitude());
                if (d < distMin) {
                    distMin = d;
                    plusProche = l;
                }
            }
            if (plusProche != null) {
                actuel = plusProche;
                aVisiter.remove(plusProche);
                resultat.add(plusProche);
            }
        }
        return resultat;
    }

    private static void calculerHoraires(Parcours p) {
        int tempsActuel = 540; // 09:00
        boolean pauseDejeunerFaite = false;
        List<Lieu> nouvellesEtapes = new ArrayList<>();
        
        for (int i = 0; i < p.getEtapes().size(); i++) {
            Lieu l = p.getEtapes().get(i);
            
            // Insertion automatique du déjeuner entre 12h et 13h
            if (!pauseDejeunerFaite && tempsActuel >= 720) { // 720 = 12h00
                nouvellesEtapes.add(new Lieu(
                    "Pause Déjeuner",
                    "Moment de repos et restauration",
                    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800",
                    "Restauration",
                    15.0,
                    60,
                    l.getLatitude(),
                    l.getLongitude()
                ));
                tempsActuel += 60;
                pauseDejeunerFaite = true;
            }

            if (i > 0) {
                Lieu precedent = p.getEtapes().get(i-1);
                double distance = calculerDistanceKM(precedent.getLatitude(), precedent.getLongitude(), l.getLatitude(), l.getLongitude());
                int minutesTrajet = (int) (distance * 12); // 5km/h
                if (minutesTrajet < 5) minutesTrajet = 10;
                
                tempsActuel += minutesTrajet;
            }
            
            nouvellesEtapes.add(l);
            tempsActuel += l.getTempsVisiteMinutes();
        }
        
        p.setEtapes(nouvellesEtapes);
    }

    private static double calculerDistanceKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) 
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.853159616;
    }
}
