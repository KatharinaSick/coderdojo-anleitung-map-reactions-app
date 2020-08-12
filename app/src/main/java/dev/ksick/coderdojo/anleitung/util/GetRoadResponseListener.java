package dev.ksick.coderdojo.anleitung.util;

import org.osmdroid.bonuspack.routing.Road;

public interface GetRoadResponseListener {
    // Wird aufgerufen, wenn die Route geladen wurde
    void onResponse(Road road);
}
