package com.whomentors.sadajura.ui.Voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Created by Michael Yoon Huh on 8/1/2015.
 */
public class SJVoice {

    // SPEECH VARIABLES
    private TextToSpeech speechInstance; // Used to reference the TTS instance for the class.

    /** NARRATION FUNCTIONALITY ________________________________________________________________ **/

    // startSpeech(): Activates voice functionality to say something.
    public static void startSpeech(final String script, Context context) {

        // Creates a new instance to begin TextToSpeech functionality.
        TextToSpeech speechInstance = null;
        final TextToSpeech finalSpeechInstance = speechInstance;
        new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                finalSpeechInstance.speak(script, TextToSpeech.QUEUE_FLUSH, null); // The script is spoken by the TTS system.
            }
        });
    }
}
