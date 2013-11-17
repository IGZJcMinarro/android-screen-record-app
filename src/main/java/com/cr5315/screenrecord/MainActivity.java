package com.cr5315.screenrecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    // Gaze upon thee and despair

    private Button bitRateButton, rotateButton, saveLocationButton, timeLimitButton,
        videoSizeButton;
    private TextView summaryTextView;

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Tools tools;

    private static int REQUEST_CODE = 5315;

    protected int timeLimit;
    private String saveLocation;
    protected int bitRate;
    protected boolean rotate;
    protected VideoSize videoSize;

    // TODO fix bit rate bug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        tools = new Tools(this);

        if (preferences.getBoolean("firstrun", true)) {
            // Run a dummy command once to get the
            // superuser request out of the way
            new SuTask("").execute();
            editor.putBoolean("firstrun", false);
            editor.commit();
        }

        initObjects();
        loadPrefs();
        updateSummary();
    }

    private void initObjects() {
        bitRateButton = (Button) findViewById(R.id.bit_rate);
        bitRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = bitRateDialog((bitRate / 1000000) - 1);
                dialog.show();
            }
        });
        rotateButton = (Button) findViewById(R.id.rotate);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRotate(!rotate);
            }
        });
        saveLocationButton = (Button) findViewById(R.id.save_location);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDirectoryPicker();
            }
        });
        timeLimitButton = (Button) findViewById(R.id.time_limit);
        timeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = timeLimitDialog(tools.getMinutes(timeLimit),
                        tools.getSeconds(timeLimit));
                dialog.show();
            }
        });
        videoSizeButton = (Button) findViewById(R.id.video_size);
        videoSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = videoSizeDialog();
                dialog.show();
            }
        });

        summaryTextView = (TextView) findViewById(R.id.summary_text);
    }

    private void loadPrefs() {
        timeLimit = preferences.getInt("timeLimit", 120);
        saveLocation = preferences.getString("saveLocation", Environment.getExternalStorageDirectory().toString() + "/");
        bitRate = preferences.getInt("bitRate", 4000000);
        rotate = preferences.getBoolean("rotate", false);

        // Video Size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        videoSize = new VideoSize(preferences.getString("videoSize", String.valueOf(width) + VideoSize.divider + String.valueOf(height)));
    }

    private void updateSummary() {
        String newline = "\n";

        // Minutes
        String result = getString(R.string.recording_time) + ": " + tools.formatTime(
                tools.getMinutes(timeLimit), tools.getSeconds(timeLimit)) + newline;

        // Save location
        result += getString(R.string.save_location) + ": " + saveLocation + newline;

        // Bit Rate
        result += getString(R.string.bit_rate) + ": " + tools.bitRateToString(bitRate) + newline;

        // Rotate
        result += getString(R.string.rotate) + ": ";
        if (rotate) {
            result += getString(R.string.yes);
        } else {
            result += getString(R.string.no);
        }
        result += newline;

        // Video size
        result += getString(R.string.video_size) + ": " + videoSize.asString();

        summaryTextView.setText(result);
    }

    private void openDirectoryPicker() {
        Intent intent = new Intent(MainActivity.this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "Screen Record");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                setSaveLocation(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR) + "/");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_start:
                String command = tools.formatCommand(timeLimit, saveLocation, bitRate, rotate, videoSize);
                //tools.runAsRoot(command);
                new SuTask(command).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class SuTask extends AsyncTask<String, Void, Boolean> {
        private final String command;

        public SuTask(String command) {
            super();
            this.command = command;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                OutputStream os = process.getOutputStream();
                Log.i("Screen Record", "Running command " + command);
                os.write(command.getBytes("ASCII"));
                Log.i("Screen Record", "Command complete");
                os.flush();
                os.close();

                Log.i("Screen Record", "Begin waitFor");
                process.waitFor();
                Log.i("Screen Record", "End waitFor");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    protected void setTimeLimit(int totalSeconds) {
        timeLimit = totalSeconds;
        editor.putInt("timeLimit", timeLimit);
        editor.commit();
        updateSummary();
    }

    protected void setSaveLocation(String location) {
        saveLocation = location;
        editor.putString("saveLocation", saveLocation);
        editor.commit();
        updateSummary();
    }

    public void setBitRate(int rate) {
        bitRate = (rate + 1) * 1000000;
        editor.putInt("bitRate", bitRate);
        editor.commit();
        updateSummary();
    }

    protected void setRotate(boolean doRotate) {
        rotate = doRotate;
        editor.putBoolean("rotate", rotate);
        editor.commit();
        updateSummary();
    }

    protected void setVideoSize(VideoSize size) {
        videoSize = size;
        editor.putString("videoSize", videoSize.toString());
        editor.commit();
        updateSummary();
    }

    /**
     * ListDialog of bit rates
     * @param selected The item in the list to be selected by default
     */
    private Dialog bitRateDialog(int selected) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String[] choices = {"1Mbps", "2Mbps", "3Mbps", "4Mbps", "5Mbps", "6Mbps", "7Mbps"};

        builder.setTitle(context.getString(R.string.bit_rate));
        builder.setSingleChoiceItems(choices, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setBitRate(((AlertDialog) dialog).getListView().getCheckedItemPosition());
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    private Dialog timeLimitDialog(int totalMinutes, int totalSeconds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.recording_time));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_time_limit, null, false);
        final NumberPicker minutes = (NumberPicker) view.findViewById(R.id.minutes);
        final NumberPicker seconds = (NumberPicker) view.findViewById(R.id.seconds);

        minutes.setMaxValue(3);
        minutes.setMinValue(0);
        minutes.setValue(totalMinutes);
        minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 3) {
                    seconds.setMaxValue(0);
                    seconds.setMinValue(0);
                } else {
                    seconds.setMaxValue(60);
                    seconds.setMinValue(0);
                }
            }
        });

        seconds.setMaxValue(60);
        seconds.setMinValue(0);
        seconds.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return tools.formatSeconds(String.valueOf(value));
            }
        });
        seconds.setValue(totalSeconds);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int chosenMinutes = minutes.getValue();
                int chosenSeconds = seconds.getValue();
                setTimeLimit(tools.getTotalSeconds(chosenMinutes, chosenSeconds));
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        return builder.create();
    }

    private Dialog videoSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.video_size));

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        final int fullWidth = size.x;
        final int fullHeight = size.y;
        String fullSize = String.valueOf(fullWidth) + "x" + String.valueOf(fullHeight);

        final int threeFourthsWidth = (int) ((double) fullWidth * (0.75));
        final int threeFourthHeight = (int) ((double) fullHeight * (0.75));
        String threeFourthSize = String.valueOf(threeFourthsWidth) + "x" + String.valueOf(threeFourthHeight);

        final int halfWidth = (int) (fullWidth / 2);
        final int halfHeight = (int) (fullHeight / 2);
        String halfSize = String.valueOf(halfWidth) + "x" + String.valueOf(halfHeight);

        final int fourthWidth = (int) ((double) fullWidth * (0.25));
        final int fourthHeight = (int) ((double) fullHeight * (0.25));
        String fourthSize = String.valueOf(fourthWidth) + "x" + String.valueOf(fourthHeight);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_video_size, null, false);

        final RadioButton full, threeFourths, half, fourth;
        full = (RadioButton) view.findViewById(R.id.full);
        threeFourths = (RadioButton) view.findViewById(R.id.three_quarters);
        half = (RadioButton) view.findViewById(R.id.half);
        fourth = (RadioButton) view.findViewById(R.id.quarter);

        String savedSize = videoSize.asString();
        if (savedSize.matches(fullSize)) full.setChecked(true);
        else if (savedSize.matches(threeFourthSize)) threeFourths.setChecked(true);
        else if (savedSize.matches(halfSize)) half.setChecked(true);
        else if (savedSize.matches(fourthSize)) fourth.setChecked(true);

        full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threeFourths.setChecked(false);
                half.setChecked(false);
                fourth.setChecked(false);
            }
        });
        threeFourths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                full.setChecked(false);
                half.setChecked(false);
                fourth.setChecked(false);
            }
        });
        half.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threeFourths.setChecked(false);
                full.setChecked(false);
                fourth.setChecked(false);
            }
        });
        fourth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threeFourths.setChecked(false);
                half.setChecked(false);
                full.setChecked(false);
            }
        });

        full.setText(fullSize);
        threeFourths.setText(threeFourthSize);
        half.setText(halfSize);
        fourth.setText(fourthSize);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VideoSize newSize = new VideoSize(fullWidth, fullHeight);
                if (full.isChecked()) {
                    // It's already set a full size
                } else if (threeFourths.isChecked()) {
                    newSize.setWidth(threeFourthsWidth);
                    newSize.setHeight(threeFourthHeight);
                } else if (half.isChecked()) {
                    newSize.setWidth(halfWidth);
                    newSize.setHeight(halfHeight);
                } else if (fourth.isChecked()) {
                    newSize.setWidth(fourthWidth);
                    newSize.setHeight(fourthHeight);
                }

                setVideoSize(newSize);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        return builder.create();
    }
}