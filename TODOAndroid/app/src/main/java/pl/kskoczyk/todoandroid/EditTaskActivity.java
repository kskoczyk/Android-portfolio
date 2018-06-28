package pl.kskoczyk.todoandroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EditTaskActivity extends AppCompatActivity {
    TextView titleText;
    TextView dateText;
    TextView descriptionText;

    // ustaw tekst w dateText po wybraniu daty z DatePicker
    Calendar deadlineCalendar = Calendar.getInstance();
    private void updateLabel() {
        String myFormat = "dd.MM.yyyy"; // WAŻNE: "y" muszą być małe!
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        String txt = sdf.format(deadlineCalendar.getTime());
        dateText.setText(sdf.format(deadlineCalendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // odbierz dane z ListActivity lub adaptera RecyclerView
        Intent intent = getIntent(); // zawsze coś wysyłam
        TODOtask receivedTask = (TODOtask) intent.getSerializableExtra("task"); // odbierz task wysłany do edycji

        // guzik do tworzenia/aktualizacji zadań
        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() { // obsłuż kliknięcie
            @Override
            public void onClick(View view) {
                // odtwórz klasę z TextView'ów, przytnij białe znaki na końcu name i description
//                TODOtask updatedTask; // jeśli parsowanie zawiedzie, zwróć nulla
                Intent returnIntent = new Intent();
                try {
                    TODOtask updatedTask = new TODOtask(titleText.getText().toString().trim(), descriptionText.getText().toString().trim(), new SimpleDateFormat("dd.MM.yyyy").parse(dateText.getText().toString()));
                    returnIntent.putExtra("task", updatedTask);
                } catch (ParseException e) {
                    e.printStackTrace(); // TODO?: pusta data rzuca wyjątek, nie tworzy zadania
                    TODOtask updatedTask = null;
                    returnIntent.putExtra("task", updatedTask); // nie można przekazać bezpośrednio nulla
                }

                // zwróc rezultat i zakończ Activity
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("task", updatedTask);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        // KONFIGURACJA - WYWOŁAJ DATE PICKER, GDY USER KLIKNIE NA DATETEXT
        dateText = this.findViewById(R.id.dateText);

        // skonfiguruj DatePicker
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                deadlineCalendar.set(Calendar.YEAR, year);
                deadlineCalendar.set(Calendar.MONTH, monthOfYear);
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(); // ustaw datę w dateText
            }

        };

        // wywołaj datePicker (klik!)
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditTaskActivity.this, date, deadlineCalendar
                        .get(Calendar.YEAR), deadlineCalendar.get(Calendar.MONTH),
                        deadlineCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // początkowy tekst
        String sName = "";
        String sDate = "";
        String sDescription = ""; // jeśli receivedTask==null, wyświetli pusty
        if(receivedTask != null) {
            sName = receivedTask.getName();
            sDate = receivedTask.getStringDate();
            sDescription = receivedTask.getDescription();
        }

        // ustaw wartości początkowe
        titleText = this.findViewById(R.id.titleText);
        titleText.setText(sName);
        dateText.setText(sDate); // zbindowany na samym początku
        descriptionText = this.findViewById(R.id.descriptionText);
        descriptionText.setText(sDescription);
    }
}
