package com.example.traveling.path;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExporter {

    public static void exportParcours(Context context, Parcours parcours) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        canvas.drawText("Votre Itinéraire : " + parcours.getTitre(), 50, 50, paint);

        paint.setTextSize(16);
        canvas.drawText("Budget total estimé : " + parcours.getBudgetTotal() + "€", 50, 100, paint);
        canvas.drawText("Durée totale : " + (parcours.getDureeTotaleMinutes() / 60) + "h " + (parcours.getDureeTotaleMinutes() % 60) + "m", 50, 130, paint);
        canvas.drawText("Distance totale : " + String.format("%.1f", parcours.getDistanceTotaleKM()) + " km", 50, 160, paint);
        
        paint.setColor(Color.BLUE);
        canvas.drawText("Impact Santé : " + parcours.getCaloriesBrulees() + " kcal brûlées", 50, 190, paint);
        paint.setColor(Color.argb(255, 0, 150, 0)); // Vert
        canvas.drawText("Impact Éco : " + String.format("%.2f", parcours.getCo2EconomiseKG()) + " kg de CO2 économisés", 50, 220, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText("Étapes :", 50, 270, paint);
        int y = 300;
        for (Lieu l : parcours.getEtapes()) {
            canvas.drawText("- " + l.getNom() + " (" + l.getPrix() + "€)", 70, y, paint);
            y += 30;
        }

        document.finishPage(page);

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Parcours_" + parcours.getTitre().replace(" ", "_") + ".pdf";
        File file = new File(downloadsDir, fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "PDF exporté dans Téléchargements", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Erreur lors de l'export PDF", Toast.LENGTH_SHORT).show();
        }

        document.close();
    }
}
