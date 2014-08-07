package com.example.magic8ball;

import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, SensorEventListener {
  
  // textToSpeech
  String[] prediction_array = { "Outlook uncertain", "Probably yes", "No way", "It is certain", "Not likely", "Reply hazy", "Maybe", "Without a doubt" };
  private TextToSpeech tts;
  private ImageButton btnPlay;

  // accelerometer
  private SensorManager senSensorManager;
  private Sensor senAccelerometer;
  private long lastUpdate = 0;
  private float last_x, last_y, last_z;
  private static final int SHAKE_THRESHOLD = 600;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // textToSpeech
    tts = new TextToSpeech(this, this);

    btnPlay = (ImageButton) findViewById(R.id.eightBallButton);

    btnPlay.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        setPrediction();
      }
    });

    // accelerometer
    senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }

  private void setPrediction() {
    int idx = new Random().nextInt(prediction_array.length);
    String randomText = prediction_array[idx];
    TextView textView2 = (TextView) findViewById(R.id.textView2);
    textView2.setText(randomText);
    speakOut(randomText);
  }

  @Override
  public void onDestroy() {
    if (tts != null) {
      tts.stop();
      tts.shutdown();
    }
    super.onDestroy();
  }

  // textToSpeech
  @Override
  public void onInit(int status) {
    if (status != TextToSpeech.ERROR) {
      tts.setLanguage(Locale.UK);
    }
  }

  private void speakOut(String randomText) {
    tts.speak(randomText, TextToSpeech.QUEUE_FLUSH, null);
  }

  // accelerometer
  @Override
  protected void onResume() {
    super.onResume();
    senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    super.onPause();
    senSensorManager.unregisterListener(this);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    Sensor mySensor = sensorEvent.sensor;

    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      float x = sensorEvent.values[0];
      float y = sensorEvent.values[1];
      float z = sensorEvent.values[2];

      long curTime = System.currentTimeMillis();

      if ((curTime - lastUpdate) > 100) {
        long diffTime = (curTime - lastUpdate);
        lastUpdate = curTime;

        float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

        if (speed > SHAKE_THRESHOLD && !tts.isSpeaking()) {
          setPrediction();
        }

        last_x = x;
        last_y = y;
        last_z = z;
      }
    }
  }
  
}
