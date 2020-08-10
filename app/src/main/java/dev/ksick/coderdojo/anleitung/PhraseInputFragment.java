package dev.ksick.coderdojo.anleitung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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

                // Erstellt ein Bundle das die Argumente enthält, die ans SecondFragment übergeben werden
                Bundle arguments = new Bundle();
                // Fügt den Text, der oben eingelesen wurde zum Bundle mit den Argumenten hinzu
                arguments.putString("phrase", phrase);

                // Navigiert zum 2. Fragment und übergibt die oben erstellten Argumente
                NavHostFragment.findNavController(PhraseInputFragment.this)
                        .navigate(R.id.action_PhraseInputFragment_to_MapFragment, arguments);
            }
        });
    }
}