package dev.ksick.coderdojo.anleitung.util;

import android.app.Activity;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GetRoadRunnable implements Runnable {

    // Die Activity, in der das Runnable erstellt wurde
    private Activity activity;
    // Die Wegpunkte, die enthalten sein sollen
    private ArrayList<GeoPoint> wayPoints;
    // Der ResponseListener, dessen "onResponse" aufgerufen wird, sobald eine Antwort verfügbar ist
    private GetRoadResponseListener responseListener;

    // Konstruktor - Erstellt eine neue Instanz des GetRoadRunnable
    public GetRoadRunnable(Activity activity, ArrayList<GeoPoint> wayPoints, GetRoadResponseListener responseListener) {
        this.activity = activity;
        this.wayPoints = wayPoints;
        this.responseListener = responseListener;
    }

    @Override
    public void run() {
        // Erstellt einen neuen RoadManager, der sich ums Erstellen der Route kümmert
        RoadManager roadManager = new OSRMRoadManager(activity);
        // Lädt eine Route, die alle GeoPoints beinhaltet, die oben übergeben wurden.
        final Road road = roadManager.getRoad(wayPoints);

        // Führt den Code innerhalb des run() {} Blocks am UI Thread aus.
        // UI Elemente können nur auf diesem Thread manipuliert werden.
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Ruft die onResponse() des ResponseListeners auf
                responseListener.onResponse(road);
            }
        });
    }
}
