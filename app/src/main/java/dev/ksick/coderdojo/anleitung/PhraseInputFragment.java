package dev.ksick.coderdojo.anleitung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringUtils;

public class PhraseInputFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phrase_input, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bindet das EditText, dass du im Layout erstellt hast an diese Variable
        final EditText editText = view.findViewById(R.id.edittext_phrase);

        view.findViewById(R.id.button_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Liest den Text aus dem EditText ein und speichert ihn in die Variable phrase
                String phrase = editText.getText().toString();

                // Überprüft ob ein Satz eingegeben wurde und ob dieser nur aus Buchstaben und Leerzeichen besteht
                if (StringUtils.isBlank(phrase) || !StringUtils.isAlphaSpace(phrase)) {
                    // Wenn nicht wird ein Fehler angezeigt und die Methode nicht weiter ausgeführt
                    editText.setError(getString(R.string.please_enter_a_phrase));
                    return;
                }

                // Erstellt ein Bundle das die Argumente enthält, die ans SecondFragment übergeben werden
                Bundle arguments = new Bundle();
                // Fügt den Text, der oben eingelesen wurde zum Bundle mit den Argumenten hinzu
                arguments.putString("phrase", phrase);

                // Navigiert zum 2. Fragment und übergibt die oben erstellten Argumente
                NavHostFragment.findNavController(PhraseInputFragment.this)
                        .navigate(R.id.action_PhraseInputFragment_to_MapFragment, arguments);
            }
        });

        // Schickt einen Request zum Endpunkt von dem die Route geladen wird.
        // Das Ergebnis wird ignoriert - damit wird nur das "cold start" Problem gelöst.
        warmUpServerlessFunction();
    }

    private void warmUpServerlessFunction() {
        // Erstellt eine Queue, die alle Requests ausführt, die zu ihr hinzugefügt werden
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        // Die URL für den Request. Diese beinhaltet keine phrase, da die Function nur gestartet werden soll;
        String url = "https://api.map-reactions.ksick.dev/v0-1/route?phrase=wakeup";

        // Erstellt den Request, der später abgesetzt werden soll
        StringRequest blankRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Das Ergebnis wird ignoriert
            }
        }, null);

        // Fügt den Request zur RequestQueue hinzu um ihn abzusetzen
        requestQueue.add(blankRequest);
    }
}