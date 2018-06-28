package pl.kskoczyk.diceshaker;
/*
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

//
//     A native method that is implemented by the 'native-lib' native library,
//     which is packaged with this application.
//
    public native String stringFromJNI();
}
*/

import java.util.Random;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener, TextToSpeech.OnInitListener {
    //sensory
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    // text-to-speech
    private TextToSpeech mTts;

    // "guziki" z main_layout
    private RelativeLayout relativeLayout;
    private TextView mNumber;
    private Button startButton;

    // stałe wyznaczone ekperymentalnie
    private static double ACCELEROMETER_THRESHOLD = 2.5;
    private static double GYROSCOPE_THRESHOLD = 0.1; // mamy pewność, że nie są to randomowe skoki odczytów
    private static double ACC_TO_GYRO_RATIO = 3.5; // przybliżyć zapis z żyroskopu do akcelerometru
    private static double DICE_ROLL_THRESHOLD = 100; // jeśli accumulation przekroczy tą wartość, wywołaj rand() - im szybciej się telefon porusza, tym szybciej przekroczy

    // zmienne do regulowania losowania
    String mode = "d_6_"; // póki co nie przewiduję dodatkowych

    private double accumulation = 0; // dodawaj odczyty do siebie
    boolean dicable = false; // czy program powinien losować
    long lastActiveTime; // do pomiaru czasu, muszę utworzyć poza funkcjami, żeby móc się do niej odnieść w onSensorChanged()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.MainLayout);
        relativeLayout.setBackgroundResource(R.drawable.d_6_1);
        mNumber = findViewById(R.id.number);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mTts = new TextToSpeech(this, this);

        startButton = findViewById(R.id.startDiceButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dicable = true; // program zacznie przetwarzać odczyty z sensorów
                lastActiveTime = System.currentTimeMillis(); // zacznij odliczać czas bezczynności
                startButton.setEnabled(false); // nie włączaj losowania od nowa, póki nie skończy się stare
                startButton.setBackgroundColor(Color.BLACK);
                Toast.makeText(getBaseContext(), "Dice started!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // przywróc rejestrowanie sensorów
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI); // SENSOR_DELAY_UI - rate suitable for the user interface
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // wyłącz sensory, by nie zżerały baterii (wyłączenie ekranu nie wyłącza automatycznie sensorów)
        mSensorManager.unregisterListener(this);
    }

    /**Text-to-speech**/
    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
            mTts.setLanguage(getResources().getConfiguration().locale);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }
    /**Text-to-speech**/

    private void generateRandomNumber() {
        Random randomGenerator = new Random();
        int randomNum = randomGenerator.nextInt(6) + 1;
        mNumber.setText(Integer.toString(randomNum));
        mTts.speak(Integer.toString(randomNum), TextToSpeech.QUEUE_FLUSH, null);
        setBackground(randomNum);
    }

    private void stopDice() {
        dicable = false; // zatrzymaj odczyty
        accumulation = 0; // wyzeruj aktywność poruszania się
        startButton.setEnabled(true);
        startButton.setBackgroundColor(Color.WHITE);
        Toast.makeText(getBaseContext(), "Dice stopped!", Toast.LENGTH_SHORT).show();
    }

    private void setBackground(Integer pips) { // pips - oczka
        String res = mode + pips.toString(); // D_6_ 1,2,3... itd...
        int resId = getResources().getIdentifier(res, "drawable", getPackageName()); // zdobądż ID tła o podanej nazwie

        relativeLayout.setBackgroundResource(resId); // ustaw tło o danym ID
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        counter++;
//        mNumber.setText(Integer.toString(counter));
//        long difference = System.currentTimeMillis() - lastActiveTime;
//        if(difference / 1000 >= 1) {
//            SystemClock.sleep(2000); // ignoruje InterruptedException
//            counter = 0;
//            lastActiveTime = System.currentTimeMillis();
//        }

        // nie można odebrać danych z dwóch sensorów jednocześnie, trzeba sprawdzać typ
        if (dicable && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            double vector = Math.sqrt(x*x + y*y + z*z); // wektor przyspieszenia
            double acceleration = Math.abs(vector - SensorManager.GRAVITY_EARTH); // abs - jeśli siła działa do góry i przeciwdziała grawitacji, uwzględnij jako ruch
            if (acceleration > ACCELEROMETER_THRESHOLD) { // jeżeli za przyspieszenie nie odpowiada sama grawitacja
                accumulation += vector;
                if(accumulation > DICE_ROLL_THRESHOLD) {
                    generateRandomNumber();
                    accumulation -= DICE_ROLL_THRESHOLD;
                }
                lastActiveTime = System.currentTimeMillis();
            }
            else {
                long difference = System.currentTimeMillis() - lastActiveTime;
                if(difference / 1000 >= 2) { // czas bezczynności dłuższy od 2sek
                        stopDice();
                    }
            }
        }
        else if (dicable && event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            double sum = Math.abs(x) + Math.abs(y) + Math.abs(z);
            if(sum > GYROSCOPE_THRESHOLD) {
                double unify = sum * ACC_TO_GYRO_RATIO; // przybliż do odczytów z akcelerometru
                accumulation += unify;
                if(accumulation > DICE_ROLL_THRESHOLD) {
                    generateRandomNumber();
                    accumulation -= DICE_ROLL_THRESHOLD;
                }
                lastActiveTime = System.currentTimeMillis();
            }
            else {
                long difference = System.currentTimeMillis() - lastActiveTime;
                if(difference / 1000 >= 2) {
                    stopDice();
                }
            }
        }
    }
}