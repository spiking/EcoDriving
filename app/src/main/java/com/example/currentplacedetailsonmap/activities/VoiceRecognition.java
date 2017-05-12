/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.example.currentplacedetailsonmap.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class VoiceRecognition implements
        RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    private static final String START_SEARCH = "start";
    private static final String STOP_SEARCH = "stop";

    /* Keyword we are looking for to activate menu */
    private static String KEYPHRASE;

    private SpeechRecognizer recognizer;
    private View[] views;
    private Context context;
    private Intent newIntent;
    private Button finishedButton;

    //Constructor for starting activity
    public VoiceRecognition(Context context, View[] views, String KEYPHRASE, Intent newIntent) {
        this.KEYPHRASE = KEYPHRASE;
        this.context = context;
        this.views = views;
        this.newIntent = newIntent;
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.v("VOICE", "VoiceRecognition initiated at time: " + currentDateTimeString);
    }

    //Constructor for stopping activity
    public VoiceRecognition(Context context, View[] views, String KEYPHRASE, Button finishedButton) {
        this.KEYPHRASE = KEYPHRASE;
        this.context = context;
        this.views = views;
        this.finishedButton = finishedButton;
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.v("VOICE", "VoiceRecognition initiated at time: " + currentDateTimeString);
    }

    public void cancelVoiceDetection() {
        if (recognizer != null) {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Log.v("VOICE", "VoiceRecognition destroyed at time: " + currentDateTimeString);
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        Log.v("VOICE", "Captured partial: " + text);

        cancelVoiceDetection();

        ((TextView) views[0]).setText("");
        if (hypothesis != null) {

            if (KEYPHRASE.equals("cancel trip"))
                makeText(context, "You stopped EcoDriving", Toast.LENGTH_SHORT).show();
            else
                makeText(context, "You started Ecodriving", Toast.LENGTH_SHORT).show();

            // Starts new intent when on recognizer partial result

            if (text.equals(KEYPHRASE)) {
                cancelVoiceDetection();

                if (finishedButton != null) {
                    finishedButton.performClick();
                } else {
                    context.startActivity(newIntent);
                }
            }
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) views[0]).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();

            Log.v("VOICE", "Captured onResult: " + text);

            if (KEYPHRASE.equals("cancel trip"))
                makeText(context, "You stopped EcoDriving", Toast.LENGTH_SHORT).show();
            else
                makeText(context, "You started Ecodriving", Toast.LENGTH_SHORT).show();

            //Starts new intent when recognizer closes on result
            if (text.equals(KEYPHRASE)) {
                cancelVoiceDetection();

                if (finishedButton != null) {
                    finishedButton.performClick();
                } else {
                    context.startActivity(newIntent);
                }
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    public void switchSearch(String searchName) {

        if (recognizer != null) {
            recognizer.stop();

            // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
            if (searchName.equals(KWS_SEARCH))
                recognizer.startListening(searchName);
            else
                recognizer.startListening(searchName, 10000);
        }
    }

    public void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        /*
        // Create grammar-based search for digit recognition
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File startGrammar = new File(assetsDir, "driver.gram");
        recognizer.addGrammarSearch(START_SEARCH, startGrammar);

        // Create grammar-based search for digit recognition
        File stopGrammar = new File(assetsDir, "driver.gram");
        recognizer.addGrammarSearch(STOP_SEARCH, stopGrammar);*/
    }

    @Override
    public void onError(Exception error) {
        Log.v("VOICE", "Error: " + error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

}
