package com.abhi.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status, result;
    Button micButton;
    SpeechRecognizer recognizer;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.rgb(8, 12, 30));

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextSize(34);
        title.setTextColor(Color.CYAN);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Your personal AI assistant");
        status.setTextSize(17);
        status.setTextColor(Color.WHITE);
        status.setGravity(Gravity.CENTER);

        result = new TextView(this);
        result.setText("Tap the microphone and speak");
        result.setTextSize(18);
        result.setTextColor(Color.WHITE);
        result.setGravity(Gravity.CENTER);
        result.setPadding(10, 40, 10, 40);

        micButton = new Button(this);
        micButton.setText("🎙 TALK TO JARVIS");

        layout.addView(title);
        layout.addView(status);
        layout.addView(result);
        layout.addView(micButton);

        setContentView(layout);

        tts = new TextToSpeech(this, code -> {
            if (code == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "IN"));
            }
        });

        micButton.setOnClickListener(v -> startListening());
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech recognition unavailable");
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            return;
        }

        if (recognizer != null) recognizer.destroy();

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                status.setText("Listening...");
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                status.setText("Processing...");
            }

            @Override public void onError(int error) {
                status.setText("Try again. Speech error: " + error);
            }

            @Override public void onResults(Bundle data) {
                ArrayList<String> matches =
                    data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {
                    String spoken = matches.get(0);
                    result.setText("You: " + spoken);
                    status.setText("JARVIS is ready");
                    tts.speak("I heard you say " + spoken,
                        TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "te-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "te-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        recognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
