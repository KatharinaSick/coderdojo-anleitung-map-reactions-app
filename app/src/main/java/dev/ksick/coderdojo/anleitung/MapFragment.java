package dev.ksick.coderdojo.anleitung;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.SpeechBalloonOverlay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import dev.ksick.coderdojo.anleitung.model.Place;

public class MapFragment extends Fragment {

    private final int PERMISSION_REQUEST_CODE = 123;

    private TextView textViewInfo;
    private String phrase;

    private MapView mapView;
    private List<Place> route;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewInfo = view.findViewById(R.id.textview_info);
        mapView = view.findViewById(R.id.mapview);

        phrase = getArguments().getString("phrase");
        textViewInfo.setText(phrase);

        loadRoute();
    }

    private void loadRoute() {
        // Erstellt eine Queue, die alle Requests ausführt, die zu ihr hinzugefügt werden
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        // Die URL für den Request. Diese wird aus den folgenden Teilen zusammengesetzt:
        //   - Die Base URL https://api.map-reactions.ksick.dev/v0-1
        //   - Der Endpunkt: /route
        //   - Der Paremeter phrase: ?phrase=<der eingegebene satz>
        // Der Satz, der vom Benutzer eingegeben wurde, muss kodiert werden, damit die gesamte URL gültig ist
        // Sollte dabei etwas schief laufen, wird eine Exception geworfen, und ein Fehler angezeigt
        String url = null;
        try {
            url = "https://api.map-reactions.ksick.dev/v0-1/route?phrase=" + URLEncoder.encode(phrase, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Zeigt einen Fehler in der Info-TextView an und führe diese Methode nicht weiter aus
            textViewInfo.setText(e.getMessage());
            return;
        }

        // Erstellt den Request, der später abgesetzt werden soll
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parst das JSON in der Variable response zu einer Liste von Place Objekten
                        route = new Gson().fromJson(response, new TypeToken<List<Place>>() {
                        }.getType());
                        // Überprüft ob der Benutzer der App erlaubt hat auf den Speicher zuzugreifen
                        if (isStoragePermissionGranted()) {
                            // Wenn Ja wird showMap() aufgerufen
                            showMap();
                        } else {
                            // Wenn Nein wird ein Dialog angezeigt, in dem der Benutzer um die Berechtigung gefragt wird
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Der Code in dieser Methode wird ausgeführt, wenn ein Fehler passiert ist.
                        textViewInfo.setText(error.getMessage());
                    }
                });

        // Fügt den Request zur RequestQueue hinzu um ihn abzusetzen
        requestQueue.add(stringRequest);
    }

    private void showMap() {
        // Lädt oder initiliasiert die osmdroid Konfiguration
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        // Definiert wie die Karte aussehen soll, MAPNIK ist quasi die Standard Open Street Map Karte
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // Braucht man, damit man mit zwei Fingern zoomen kann
        mapView.setMultiTouchControls(true);
        // Zoomt auf das Zoom Level 6
        mapView.getController().zoomTo(6.0);

        // Erstellt eine Liste mit GeoPoint Objekten. Diese wird später benötigt um eine Route auf der Karte zu zeigen.
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();

        // Iteriert über alle Places, die in der Route enthalten sind
        for (int i = 0; i < route.size(); i++) {
            // Speichert den aktuellen Place in die Variable place
            Place place = route.get(i);
            // Erstellt einen GeoPoint mit den Koordinaten des aktuellen Place
            GeoPoint geoPoint = new GeoPoint(place.getLatitude(), place.getLongitude());
            // Fügt den soeben erstellten GeoPoint zur Liste der Punkte hinzu.
            geoPoints.add(geoPoint);

            // Erstellt das Overlay (= Marker) der später angezeigt wird
            SpeechBalloonOverlay textOverlay = new SpeechBalloonOverlay();
            // Positioniert das Overlay
            textOverlay.setGeoPoint(geoPoint);
            // Definiert den Titel/Text des Overlays. Dieser sieht später zum Biespiel so aus: "1 - Ortsname"
            textOverlay.setTitle((i + 1) + " - " + place.getName());

            // Definiert wie die Schrift aussehen soll, also weiß und in der Schriftgröße 48
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(48);

            // Definiert die Hintergrundfarbe, in diesem Fall schwarz
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.BLACK);

            // Übergibt die soeben erstellten Paints für Text und Hintergrund an das Overlay, damit diese verwendet werden
            textOverlay.setForeground(textPaint);
            textOverlay.setBackground(backgroundPaint);

            // Legt einen Rahmen fest, damit die Marker ein bisschen besser aussehen
            textOverlay.setMargin(16);

            // Fügt das Overlay zur Karte hinzu
            mapView.getOverlays().add(textOverlay);
        }

        // Die folgenden zwei Zeilen werden benötigt um HTTP Requests am Main Thread abzusetzen.
        // Mehr dazu und wie man es besser macht im Abschnitt "Bonus: Verbesserungen" der Anleitung.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Erstellt einen neuen RoadManager, der sich ums Erstellen der Route kümmert
        RoadManager roadManager = new OSRMRoadManager(getContext());
        // Lädt eine Route, die alle GeoPoints beinhaltet, die oben zur Liste hinzugefügt wurden.
        // Diese methode macht im Hintergrund einen HTTP Request am Main Thread.
        Road road = roadManager.getRoad(geoPoints);
        // Erstellt die Linie, die dann auf der Karte angezeigt wird.
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.BLACK, 6);
        // Zeigt die Linie auf der Karte an.
        mapView.getOverlays().add(roadOverlay);

        // Erlaubt der Karte nicht weiter als auf dieses Level zu zoomen
        mapView.setMaxZoomLevel(5.0);
        // Zentriert die Karte über der Route
        mapView.zoomToBoundingBox(roadOverlay.getBounds(), true, 150);
        // Aktualisiert die MapView, damit die Overlays richtig angezeigt werden.
        mapView.invalidate();
    }

    private boolean isStoragePermissionGranted() {
        // Holt den Status der Berechtigung, also ob sie zugelassen (granted) oder abgelehnt (denied) wurde.
        int permissionStatus = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Gibt true zurück, wenn die Berechtigung zugelassen wurde. Sonst wird false zurückgegeben.
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Führt die Implementierung dieser Methode in der Fragment.java Klasse aus
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Überprüft ob der request code mit dem übereinstimmt, der vorher abgesetzt wurde.
        // Falls er nicht übereinstimmt, ist es nicht das Ergebnis auf deinen Request und du musst nichts machen.
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Überprüft ob der Benutzer der App erlaubt hat auf den Speicher zuzugreifen
            if (isStoragePermissionGranted()) {
                // Wenn Ja wird showMap() aufgerufen
                showMap();
            } else {
                // Wenn Nein wird die App geschlossen
                getActivity().finish();
            }
        }
    }
}