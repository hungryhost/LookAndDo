/* Copyright 2020 Yury Borodin. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.yuryborodin.lookanddo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
{

    /**
     * This constant sets height and width of images that are used by a particular TF model
     * For MobileNet_v2_1.0_224 parameters are as set here
     * @since 1.0
     */
    public static final int INPUT_SIZE = 224;

    /**
     * This constant is specific for the model used in this app
     * Used for data normalization (see docs on MobileNet for further information)
     * @since 1.0
     */
    public static final int IMAGE_MEAN = 128;

    /**
     * This constant is specific for the model used in this app
     * Used for data normalization (see docs on MobileNet for further information)
     * @since 1.0
     */
    public static final float IMAGE_STD = 128.0f;

    /**
     * This constant defines how many classes are in a particular TF model
     * MobileNet_v2_1.0_224 was retrained with 10 classes (30k images each)
     * @since 1.0
     */
    public static final int NUMBER_OF_CLASSES = 10;

    /**
     * This constant defines how many rounds there are
     * @since 1.0
     */
    public static final int NUMBER_OF_ROUNDS = 5;

    /**
     * Constant used in conjuncture with {@link MainActivity#mHelpCounter}
     * Provides the number of wrong answers until we show a tip to the user
     * @since 1.0
     */
    private static final int HELP_LIMIT = 2;

    /**
     * This constant defines the size of the array of images that we use
     * For each class we have as many images as there are rounds in the game
     * @since 1.0
     */
    public static final int ARRAY_OF_IMAGES_SIZE = NUMBER_OF_CLASSES * NUMBER_OF_ROUNDS;

    /**
     * This constant is also specific for the model we use. Represents the name of the input layer
     * Defined by Google (for MobileNet model)
     * @since 1.0
     */
    public static final String INPUT_NAME = "Placeholder";

    /**
     * This constant is specific for this retrained model, defined in the process of retraining
     * Name of the output layer
     * @since 1.0
     */
    public static final String OUTPUT_NAME = "final_result";

    /**
     * This constant defines the path to .pb file for TensorFlow
     * @since 1.0
     */
    public static final String MODEL_FILE = "file:///android_asset/graph_v8_by30k.pb";

    /**
     * This constant defines the path to .txt file with labels, corresponding with the model
     * @since 1.0
     */
    public static final String LABEL_FILE = "file:///android_asset/labels_v8_by30k.txt";

    /**
     * This is an object of Classifier class
     * {@link Classifier}
     * @since 1.0
     */
    public Classifier classifier;

    /**
     * Executor object
     * Will be used in {@link MainActivity#initTensorFlowAndLoadModel()}
     * @since 1.0
     */
    public Executor executor = Executors.newSingleThreadExecutor();


    /**
     * Array of images
     * @since 1.0
     */
    private int [] mImageArray = new int[ARRAY_OF_IMAGES_SIZE];

    /**
     * Array of scores for each image (will be wiped out after each round)
     * @since 1.0
     */
    private double [] mScoreArray = new double[NUMBER_OF_CLASSES];

    /**
     * This array stores current gameScore for each image
     * @since 1.0
     */
    private double[] mFinalScoreArray = new double[NUMBER_OF_ROUNDS*NUMBER_OF_CLASSES];

    /**
     * Variable used for determining when we need to show {@link MainActivity#helpMessage()}
     * @since 1.0
     */
    private int mHelpCounter = 0;



    /**
     * Variable used for determining current step within [0,45] boundaries
     * Each step is responsible for each class
     * Changes when user goes to next picture (with a step of 5)
     * @since 1.0
     */
    private int mCurrentStep = 0;

    /**
     * Variable used for determining current image withing current class
     * Changes when user starts new round
     * @since 1.0
     */
    private int mCurrentSubStep = 0;

    /**
     * Variable used for finding right image (represents index of {@link MainActivity#mImageArray})
     * Within the program calculated as mCurrentSubStep + mCurrentStep
     * @since 1.0
     */
    private int mCurrentImage = 0;

    /**
     * Variable used for comparing current class with the results of classification
     * Lies within [0, NUMBER_OF_CLASSES - 1]
     * @since 1.0
     */
    private int mCurrentTag = 0;

    /**
     * Variable used for storing score throughout the game
     * Lies within [0, NUMBER_OF_CLASSES * NUMBER_OF_ROUNDS]
     * @since 1.0
     */
    private double mGameScore = 0.0;

    /**
     * Variable used for storing mGameScore in String type
     * @since 1.0
     */
    private String mStringGameScore = "0";

    /**
     * Variable used for storing score of a current round
     * Lies within [0, NUMBER_OF_CLASSES]
     * @since 1.0
     */
    private double mRoundScore = 0;

    /**
     * This variable stores index of current iteration, lies within [0, 49]
     * @since 1.0
     */
    private int mCurrentIteration = 0;

    /**
     * Variable used for storing mRoundScore in String type
     * @since 1.0
     */
    private String mStringRoundScore = "0";

    /**
     * DrawView object,
     * @see DrawView
     * @since 1.0
     */
    private DrawView mDrawView;


    /**
     * TextView object
     * Used for showing messages to the user
     * @since 1.0
     */
    private TextView mMainTextView;

    /**
     * TextView object
     * Used for showing current score
     * @since 1.0
     */
    private TextView mScoreView;

    /**
     * TextView object
     * Used for showing mGameScore and current round
     * @since 1.0
     */
    private TextView mRoundView;

    /**
     * ImageView object
     * Used for showing images to user
     * @since 1.0
     */
    private ImageView mImageView;

    /**
     * LoadingDialog object
     * Used for showing loading animation and putting text in textViews
     * @see LoadingDialog
     * @see MainActivity#showMessageAfterLoading(String)
     * @since 1.0
     */
    private LoadingDialog loadingDialog;

    /**
     * Button object, refers to updateButton (see activity_main.xml)
     * @since 1.0
     */
    private Button mUpdateButton;

    /**
     * Button object, refers to proceedButton (see activity_main.xml)
     * @since 1.0
     */
    private Button mProceedButton;


    /**
     * Object of DecimalFormat
     * Used for formatting double values
     * @since 1.0
     */
    DecimalFormat decimalFormat = new DecimalFormat("#.##");


    /**
     * Overridden onCreate method (see Google documentation for further information)
     * @param savedInstanceState object of Bundle (used for saving state)
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // shared preferences of score
        SharedPreferences scorePreferences = getSharedPreferences("SCORE",
                Context.MODE_PRIVATE);
        mStringRoundScore = scorePreferences.getString("totalScore", "0");
        mStringGameScore = scorePreferences.getString("gameScore","0");
        this.mGameScore = Double.parseDouble(mStringGameScore);
        mCurrentIteration = scorePreferences.getInt("n",1);
        // shared preferences of initial message (do not show again)
        SharedPreferences appPreferences = getSharedPreferences("PREFS",0);
        boolean ifShowDialog = appPreferences.getBoolean("showDialog", true);
        if(ifShowDialog){showInitialMessage();}

        // shared preferences of what image is now
        SharedPreferences imagePreferences = getSharedPreferences("IMAGE",0);
        mCurrentImage = imagePreferences.getInt("iterator",0);
        mCurrentTag = imagePreferences.getInt("currentTag", 0);
        mCurrentSubStep = imagePreferences.getInt("currentSubStep",0);
        mCurrentStep = imagePreferences.getInt("currentStep",0);

        SharedPreferences button = getSharedPreferences("BUTTONS",0);
        boolean updateButtonOn = button.getBoolean("update",false);
        SharedPreferences finalScorePreference = getSharedPreferences("GAME_SCORE",
                Context.MODE_PRIVATE);

        mUpdateButton = (Button) findViewById(R.id.updateButton);
        mProceedButton = (Button) findViewById(R.id.proceedButton);
        mDrawView = (DrawView) findViewById(R.id.drawView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics.heightPixels);
        mDrawView.init(metrics);
        mUpdateButton.setEnabled(updateButtonOn);
        mProceedButton.setEnabled(true);
        mDrawView.enablePaint(true);
        
        mImageArray[0] = R.drawable.i00;
        mImageArray[1] = R.drawable.i01;
        mImageArray[2] = R.drawable.i02;
        mImageArray[3] = R.drawable.i03;
        mImageArray[4] = R.drawable.i04;

        mImageArray[5] = R.drawable.i10;
        mImageArray[6] = R.drawable.i11;
        mImageArray[7] = R.drawable.i12;
        mImageArray[8] = R.drawable.i13;
        mImageArray[9] = R.drawable.i14;

        mImageArray[10] = R.drawable.i20;
        mImageArray[11] = R.drawable.i21;
        mImageArray[12] = R.drawable.i22;
        mImageArray[13] = R.drawable.i23;
        mImageArray[14] = R.drawable.i24;

        mImageArray[15] = R.drawable.i30;
        mImageArray[16] = R.drawable.i31;
        mImageArray[17] = R.drawable.i32;
        mImageArray[18] = R.drawable.i33;
        mImageArray[19] = R.drawable.i34;

        mImageArray[20] = R.drawable.i40;
        mImageArray[21] = R.drawable.i41;
        mImageArray[22] = R.drawable.i42;
        mImageArray[23] = R.drawable.i43;
        mImageArray[24] = R.drawable.i44;

        mImageArray[25] = R.drawable.i50;
        mImageArray[26] = R.drawable.i51;
        mImageArray[27] = R.drawable.i52;
        mImageArray[28] = R.drawable.i53;
        mImageArray[29] = R.drawable.i54;

        mImageArray[30] = R.drawable.i60;
        mImageArray[31] = R.drawable.i61;
        mImageArray[32] = R.drawable.i62;
        mImageArray[33] = R.drawable.i63;
        mImageArray[34] = R.drawable.i64;

        mImageArray[35] = R.drawable.i70;
        mImageArray[36] = R.drawable.i71;
        mImageArray[37] = R.drawable.i72;
        mImageArray[38] = R.drawable.i73;
        mImageArray[39] = R.drawable.i74;

        mImageArray[40] = R.drawable.i80;
        mImageArray[41] = R.drawable.i81;
        mImageArray[42] = R.drawable.i82;
        mImageArray[43] = R.drawable.i83;
        mImageArray[44] = R.drawable.i84;

        mImageArray[45] = R.drawable.i90;
        mImageArray[46] = R.drawable.i91;
        mImageArray[47] = R.drawable.i92;
        mImageArray[48] = R.drawable.i93;
        mImageArray[49] = R.drawable.i94;
        
        // retrieving score from saved preferences
        for(int i = 0; i<= mCurrentTag; i++){
            mScoreArray[i] = Double.parseDouble(imagePreferences.getString(
                    String.valueOf(i),"0"));
        }
        for (int i = 0; i<= mCurrentIteration; i++){
            mFinalScoreArray[i] = Double.parseDouble(finalScorePreference.getString(
                    String.valueOf(i),"0"
            ));
        }
        mMainTextView = (TextView) findViewById(R.id.mainTextView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mScoreView = (TextView) findViewById(R.id.scoreView);
        mScoreView.setText(mStringGameScore);
        initTensorFlowAndLoadModel();
        mRoundView = (TextView) findViewById(R.id.roundView);
        mRoundView.setText(String.valueOf((mCurrentSubStep+1)));
        mImageView.setImageResource(mImageArray[mCurrentImage]);
        mImageView.setTag(mCurrentTag);


    }

    /**
     * This method initializes TensorFLow by passing parameters defined in MainActivity class
     * @since 1.0
     */
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    /**
     * @param bitmap bitmap object, retrieved from DrawView object and passed into TF classifier
     * @return results list of classification results
     * @since 1.0
     */
    public List<Classifier.Recognition> analyse(Bitmap bitmap)
    {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        return results;
    }

    /**
     * This method handles "loading" box and fills textView1 and textView2 after the proceedButton
     * was clicked.
     * @param text String with a message we want to show to a user
     * @since 1.0
     */
    public void showMessageAfterLoading(String text){
        mMainTextView.setText("");
        final String finalMessage = text;
        loadingDialog = new LoadingDialog(MainActivity.this);
        loadingDialog.startLoadingDialog();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
                mMainTextView.setText(finalMessage);
                mScoreView.setText(mStringGameScore);
            }
        }, 500);
    }

    /**
     * This method updates the scores of current round and game
     * @param score double value of a score that user got from completing the task
     * @since 1.0
     */
    public void updateScore(double score){
        double intermScore = 0;
        mGameScore = 0;
        SharedPreferences scorePreference = getSharedPreferences("SCORE",
                Context.MODE_PRIVATE);
        SharedPreferences finalScorePreference = getSharedPreferences("GAME_SCORE",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor scoreEdit = finalScorePreference.edit();
        SharedPreferences.Editor editor = scorePreference.edit();
        SharedPreferences imagePreference = getSharedPreferences("IMAGE",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor imageEditor = imagePreference.edit();
        // redo the score with new value
        mScoreArray[mCurrentTag] = score;
        for(int i = 0; i<= mCurrentTag; i++){
            intermScore = intermScore + mScoreArray[i];
            imageEditor.putString(String.valueOf(i), String.valueOf(mScoreArray[i]));
        }
        mFinalScoreArray[mCurrentIteration] = intermScore;

        for(int i = 0; i<= mCurrentIteration; i++){
            mGameScore += mFinalScoreArray[i];
            System.out.println(mGameScore);
            scoreEdit.putString(String.valueOf(i), String.valueOf(mFinalScoreArray[i]));
        }
        scoreEdit.apply();
        editor.putInt("n", mCurrentIteration);
        mRoundScore = intermScore;
        mRoundScore = Double.parseDouble(decimalFormat.format(mRoundScore));
        mGameScore = Double.parseDouble(decimalFormat.format(mGameScore));
        mStringRoundScore = String.valueOf(mRoundScore);
        mStringGameScore = String.valueOf(mGameScore);
        // saving score
        editor.putString("totalScore", mStringRoundScore);
        editor.putString("gameScore", mStringGameScore);
        editor.apply();
        imageEditor.putInt("iterator", mCurrentImage);
        imageEditor.putInt("currentTag", mCurrentTag);
        imageEditor.putInt("currentStep", mCurrentStep);
        imageEditor.putInt("currentSubStep", mCurrentSubStep);
        imageEditor.apply();
    }
    /**
     * This method is connected to proceedButton
     * Takes bitmap image from DrawView object, invokes {@link MainActivity#analyse(Bitmap bitmap)}
     * method and interprets the results taken from {@link MainActivity#analyse(Bitmap bitmap)}
     * @param v View object
     * @since 1.0
     */
    public void onProceedButtonClicked(View v)
    {

        // method takes bitmap created by user and initiates classification
        Bitmap final_image = mDrawView.proceed();
        List<Classifier.Recognition> results = analyse(final_image);

        //int numb = mCurrentTag;
        //int numb1 = numb + 1;
        int id = Integer.parseInt(results.get(0).getId());
       // System.out.println("id TS: " + id + "\nNumb in labels: " + numb1 + "\n Expected: " +
           //     results.get(0).getTitle());
        final String textOk = getString(R.string.goodJobString);
        final String textNotOk = getString(R.string.tryAgainString);
        final String textNotPainted = getString(R.string.notPaintedString);

        SharedPreferences btnPreferences = getSharedPreferences("BUTTONS",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor btnEditor = btnPreferences.edit();
        mRoundScore = mGameScore;
        mStringRoundScore = String.valueOf(mRoundScore);
        if (mDrawView.checkIfPainted() && mCurrentTag == id){
            // managing buttons
            mProceedButton.setEnabled(false);
            mDrawView.enablePaint(false);
            double Score = Math.floor(results.get(0).getConfidence()*100)/100;
            String validationScore = (String.valueOf(Score));
            String dispText = textOk + " " + "Вы получили: " + validationScore + " из 1.";
            updateScore(Score);
            showMessageAfterLoading(dispText);
            results.clear();
            mUpdateButton.setEnabled(true);
            btnEditor.putBoolean("update", true);
            btnEditor.apply();
        } else if (!mDrawView.checkIfPainted()){
            showMessageAfterLoading(textNotPainted);
            results.clear();
        } else {
            mProceedButton.setEnabled(false);
            mDrawView.enablePaint(false);
            mHelpCounter++;
            showMessageAfterLoading(textNotOk);
            SharedPreferences scorePreferences = getSharedPreferences("PREFS",
                    Context.MODE_PRIVATE);
            boolean show = scorePreferences.getBoolean("helpDialog", true);
            if(mHelpCounter > HELP_LIMIT && show){
                helpMessage(); // used more than twice got it wrong, time to help
                mHelpCounter = 0;
            }
            results.clear();
        }


    }

    /**
     * This method is invoked when user opens the app for the first time
     * See values/strings.xml for string resources used in this method
     * @since 1.0
     */
    // this method is called when user first opens the app or call "rules" in menu
    public void showInitialMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.initialDialogString)
                .setMessage(getString(R.string.initialDialogTextString))
                .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPreferences preferences = getSharedPreferences("PREFS",
                                0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("showDialog", false);
                        editor.apply();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button pos =  alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveLLP = (LinearLayout.LayoutParams) pos.getLayoutParams();
        positiveLLP.gravity = Gravity.CENTER;
        pos.setLayoutParams(positiveLLP);
    }

    /**
     * This method is called whenever we need to show a help message with a tip
     * @since 1.0
     */
    public void helpMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.helpBoxTitleString)
                .setMessage(R.string.helpBoxMessageString)
                .setPositiveButton(R.string.helpBoxOkString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.helpBoxDoNotShowString,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPreferences preferences = getSharedPreferences("PREFS",
                                0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("helpDialog", false);
                        editor.apply();
                    }
                });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(14);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(14);

        Button pos =  alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button neg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams positiveLLP = (LinearLayout.LayoutParams) pos.getLayoutParams();
        LinearLayout.LayoutParams negativeLLP = (LinearLayout.LayoutParams) neg.getLayoutParams();
        positiveLLP.gravity = Gravity.CENTER;
        negativeLLP.gravity = Gravity.CENTER;
        pos.setLayoutParams(positiveLLP);
        neg.setLayoutParams(negativeLLP);
    }

    /**
     * @param menu Menu object
     * @return boolean parameter (whether to show the menu (true) or not (false))
     * @since 1.0
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @param item MenuItem object
     * @return boolean variable (whether user chose an item (true) or closed it (false))
     * @since 1.0
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case (R.id.rules):
                showInitialMessage();
                return true;
            case(R.id.information):
                showInfoMessage();
                return true;
            case(R.id.newGame):
                newGame(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * OnClick method for clearButton (clears mDrawView and sets empty mTextView1)
     * @param v View object
     * @since 1.0
     */
    public void onClearButtonClicked(View v){
        mDrawView.clear();
        mMainTextView.setText(" ");
        mDrawView.enablePaint(true);
        mProceedButton.setEnabled(true);
        SharedPreferences btnPreferences = getSharedPreferences("BUTTONS",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor btnEditor = btnPreferences.edit();
        btnEditor.putBoolean("proceed", true);
        btnEditor.putBoolean("paint", true);
        btnEditor.apply();
    }

    /**
     * OnClick method is called when user clicked the updateButton (see activity_main.xml file)
     * @param v View object
     * @since 1.0
     */
    public void onUpdateButtonClicked(View v){
        // this method is called when user wants to proceed to the next picture
        SharedPreferences scorePreference = getSharedPreferences("IMAGE",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = scorePreference.edit();
        mUpdateButton.setEnabled(false);

        if(mCurrentTag < 9 && mCurrentStep < 45){
            mCurrentTag++;
            mCurrentStep = mCurrentStep + 5;
            mCurrentImage = mCurrentStep + mCurrentSubStep;
            mDrawView.enablePaint(true);
            mDrawView.clear();
            mImageView.setImageResource(mImageArray[mCurrentImage]);
            mImageView.setTag(mCurrentTag);
            mMainTextView.setText(" ");
            editor.putInt("iterator", mCurrentImage);
            editor.putInt("currentTag", mCurrentTag);
            editor.putInt("currentStep", mCurrentStep);
            editor.putInt("currentSubStep", mCurrentSubStep);
            editor.apply();
            mProceedButton.setEnabled(true);
            SharedPreferences btnPreferences = getSharedPreferences("BUTTONS",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor btnEditor = btnPreferences.edit();

            btnEditor.putBoolean("update",false);

            btnEditor.apply();
        } else {
            mCurrentTag = 0;
            mCurrentStep = 0;
            mCurrentImage = mCurrentStep + mCurrentSubStep;
            editor.putInt("iterator", mCurrentImage);
            editor.putInt("currentTag", mCurrentTag);
            editor.putInt("currentStep", mCurrentStep);
            editor.putInt("currentSubStep", mCurrentSubStep);
            editor.apply();
            newGame(false);
        }
    }

    /**
     * This method is called when user call "information" menu-option
     * @since 1.0
     */
    public void showInfoMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.infoMessageTitileString)
                .setMessage(getString(R.string.infoMessageTextSting))
                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when we need to inform user that the round is over
     * @since 1.0
     */
    public void finalMessageRound(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.endRoundString)
                .setMessage(getString(R.string.finalMessageString) +
                        getString(R.string.finalScoreString) + " " + mStringRoundScore)
                .setPositiveButton("Продолжить!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called whenever we need to inform the user that the game is over
     * Called either automatically or from menu bar
     * @since 1.0
     */
    public void finalMessageGame(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(R.string.endGameString)
                .setMessage(getString(R.string.finalGameMessageString) +
                        getString(R.string.finalScoreString) + " " + mStringGameScore)
                .setPositiveButton("Начать заново!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * This method is called whenever the new game needs to be started
     * May be called automatically (see {@link MainActivity#onUpdateButtonClicked(View)})
     * Or may be called from menu bar (see {@link MainActivity#onOptionsItemSelected(MenuItem)})
     * @param byUser boolean variable, true if method was called from menu bar, false if not;
     * @since 1.0
     */
    public void newGame(boolean byUser){
        SharedPreferences scorePreference = getSharedPreferences("SCORE",
                Context.MODE_PRIVATE);
        SharedPreferences imagePreference = getSharedPreferences("IMAGE",
                Context.MODE_PRIVATE);
        SharedPreferences btnPreferences = getSharedPreferences("BUTTONS",
                Context.MODE_PRIVATE);
        SharedPreferences finalScorePreference = getSharedPreferences("GAME_SCORE",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor scoreEdit = finalScorePreference.edit();
        SharedPreferences.Editor btnEditor = btnPreferences.edit();
        SharedPreferences.Editor imageEditor = imagePreference.edit();
        SharedPreferences.Editor editor = scorePreference.edit();
        if(mCurrentSubStep < (NUMBER_OF_ROUNDS-1) && !byUser){
            mCurrentIteration++;
            System.out.println(mCurrentIteration);
            scoreEdit.putInt("n", mCurrentIteration);
            scoreEdit.apply();
            // we have 5 different images for each class
            mCurrentSubStep = mCurrentSubStep + 1;
            mRoundScore = 0;
            finalMessageRound();
        } else {
            mCurrentSubStep = 0;
            finalMessageGame();
            mCurrentStep = 0;
            mGameScore = 0;
            Arrays.fill(mFinalScoreArray,0.0);
            mCurrentIteration = 0;
            for(int i = 0; i<= mCurrentTag; i++){
                mScoreArray[i] = 0;
                imageEditor.putString(String.valueOf(i), String.valueOf(mScoreArray[i]));
            }
            for(int i = 0; i<= mCurrentIteration; i++){
                mFinalScoreArray[i] = 0;
                scoreEdit.putString(String.valueOf(i), String.valueOf(mFinalScoreArray[i]));
            }
            mStringGameScore = "0";

        }
        mStringGameScore = String.valueOf(mGameScore);
        mStringRoundScore = String.valueOf(mRoundScore);
        // managing buttons
        mProceedButton.setEnabled(true);
        mUpdateButton.setEnabled(false);
        // managing values
        Arrays.fill(mScoreArray,0.0);
        // loading values into SharedPreference objects
        btnEditor.putBoolean("update", false);
        btnEditor.apply();
        this.mStringRoundScore = String.valueOf(this.mRoundScore);
        this.mStringGameScore = String.valueOf(this.mGameScore);
        mCurrentTag = 0;
        mCurrentImage = mCurrentSubStep + mCurrentStep;
        mStringRoundScore = String.valueOf(mRoundScore);
        editor.putString("totalScore", this.mStringRoundScore);
        editor.putString("gameScore", this.mStringGameScore);
        editor.apply();
        scoreEdit.apply();
        imageEditor.putInt("iterator", mCurrentImage);
        imageEditor.putInt("currentTag", mCurrentTag);
        imageEditor.putInt("currentSubStep", mCurrentSubStep);
        imageEditor.putInt("currentStep", mCurrentStep);
        imageEditor.apply();
        // setting objects to new values
        mRoundView.setText(String.valueOf((mCurrentSubStep+1)));
        mDrawView.enablePaint(true);
        mDrawView.clear();
        mImageView.setImageResource(mImageArray[mCurrentImage]);
        mImageView.setTag(mCurrentTag);
        mScoreView.setText(String.valueOf(mGameScore));
        mMainTextView.setText(" ");
    }


}
