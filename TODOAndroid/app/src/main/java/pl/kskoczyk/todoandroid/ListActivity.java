// mainActivity
// LONG CLICK JEST ZAGŁUSZANY PRZEZ NORMALNY CLICK - kod zostawiłem, ale to nie zadziała
// TODO: onpause, ondestroy - przeładować i zrobić zapis z użyciem SharedPrefs

package pl.kskoczyk.todoandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ListActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, RecyclerViewAdapter.ItemLongClickListener {

    RecyclerViewAdapter adapter;
    // globale przydadzą się do paru funkcji, gdzie będę potrzebował dostęp spoza funkcji onCreate() - po prostu referencje
    ArrayList<TODOtask> globalTaskNames;
    SharedPreferences globalSharedPref; //TODO: sprawdzić, czy może być sam global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ArrayList<TODOtask> taskNames = new ArrayList<>();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        globalSharedPref = sharedPref;

        // guzik musi być wewnątrz funkcji, inaczej NullPointerException
        FloatingActionButton newTaskButton = findViewById(R.id.newTaskButton); // primary action
        newTaskButton.setOnClickListener(new View.OnClickListener() { // obsłuż kliknięcie
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ListActivity.this, EditTaskActivity.class);
                TODOtask sendTask = null;
                myIntent.putExtra("task", sendTask); // przekazanie pustego Taska oznacza, że tworzę nowy
                ListActivity.this.startActivityForResult(myIntent, 0); // 0 z tego guzika, 1 z menu kontekstowego (do edycji)
            }
        });

        // guzik zapisu do SharedPrefs
        Button spSave = findViewById(R.id.spSave);
        spSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToSharedPref(globalTaskNames, globalSharedPref);
            }
        });

        // data to populate the RecyclerView with

        /*SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Size", taskNames.size());
        String basicKey = "Task"; // dla obu
        for(Integer  i = 0; i < taskNames.size(); i++)  { // dla każdego zadania utwórz indywidualny klucz i zakoduj
            String taskKey = basicKey + i.toString();
            editor.putString(taskKey, new Gson().toJson(taskNames.get(i)));
        }
        editor.commit(); // wyślij zmiany*/


        // Legacy code, gdybym zapomniał jak obsługiwać Gson'a
        // Type listType = new TypeToken<ArrayList<TODOtask>>(){}.getType();
        // taskNames = new Gson().fromJson(sharedPref.getString("LabelName", ""), new TypeToken<ArrayList<TODOtask>>(){}.getType()); // Jezus Maria

        // wczytaj zmiany
        taskNames = loadFromSharedPref(sharedPref);
        globalTaskNames = taskNames;
        /*String basicKey = "Task";
        int size = sharedPref.getInt("Size", 0);
        for(Integer i = 0; i < size; i++) {
            String taskKey = basicKey + i.toString();
            taskNames.add(new Gson().fromJson(sharedPref.getString(taskKey, ""), TODOtask.class));
        }*/

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.TODOview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, this, taskNames);
        adapter.setClickListener(this);
        adapter.setLongClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, adapter.getItem(position).getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position).toString() + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    // obsłuż zwrot informacji z EditActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 0 - nowy Task, 1 - edit Task
        if (requestCode == 0) {
            if(resultCode == Activity.RESULT_OK){
                TODOtask resultTask = (TODOtask) data.getSerializableExtra("task"); // przechwyć zwrócony TODOtask
                if(resultTask == null) {
                    return;
                }

                globalTaskNames.add(resultTask); // dodaj zaktualizowane zadanie do listy
                Integer newIndex = globalTaskNames.size() - 1;
                adapter.notifyItemInserted(newIndex); // poinformuj RecyclerView o zmianie
                }

            }
        // edycja
        else if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                TODOtask resultTask = (TODOtask) data.getSerializableExtra("task"); // przechwyć zwrócony TODOtask
                if(resultTask == null) {
                    return;
                }

                // odczytaj zmieniany indeks zadania
                int editedIndex = globalSharedPref.getInt("EditedTaskIndex", -1);
                if(editedIndex == -1) {
                    return;
                }
                globalTaskNames.set(editedIndex, resultTask); // podmień edytowane zadanie w liście
                adapter.notifyItemChanged(editedIndex); // poinformuj RecyclerView o zmianie
                //adapter.notifyItemRangeChanged(editedIndex, globalTaskNames.size());

                // usuń wpis o edytowanym indeksie z SharedPrefs
                SharedPreferences.Editor editor = globalSharedPref.edit();
                editor.remove("EditedTaskIndex");
                editor.apply();
            }
        }
        else {
            return;
        }
    }

    public void saveToSharedPref(ArrayList<TODOtask> tasksToSave, SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("Size", tasksToSave.size());
        String basicKey = "Task"; // dla obu
        for(Integer  i = 0; i < tasksToSave.size(); i++)  { // dla każdego zadania utwórz indywidualny klucz i zakoduj
            String taskKey = basicKey + i.toString();
            editor.putString(taskKey, new Gson().toJson(tasksToSave.get(i)));
        }
        editor.commit(); // wyślij zmiany
    }

    public ArrayList<TODOtask> loadFromSharedPref(SharedPreferences sp) {
        ArrayList<TODOtask> loaded = new ArrayList<TODOtask>();

        String basicKey = "Task";
        int size = sp.getInt("Size", 0);
        for(Integer i = 0; i < size; i++) {
            String taskKey = basicKey + i.toString();
            loaded.add(new Gson().fromJson(sp.getString(taskKey, ""), TODOtask.class));
        }

        return loaded;
    }
}
