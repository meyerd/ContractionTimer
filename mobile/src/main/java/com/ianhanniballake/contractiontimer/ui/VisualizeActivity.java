package com.ianhanniballake.contractiontimer.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ianhanniballake.contractiontimer.R;
import com.ianhanniballake.contractiontimer.curvefit.ExpTrendLine;
import com.ianhanniballake.contractiontimer.curvefit.LogTrendLine;
import com.ianhanniballake.contractiontimer.curvefit.PolyTrendLine;
import com.ianhanniballake.contractiontimer.curvefit.PowerTrendLine;
import com.ianhanniballake.contractiontimer.curvefit.TrendLine;
import com.ianhanniballake.contractiontimer.provider.ContractionContract;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.StrictMath.max;

public class VisualizeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String CONTRACTIONS_ENABLED_PREFERENCES = "vis_contractions_enabled";
    private static final String AVERAGESS_ENABLED_PREFERENCES = "vis_averages_enabled";
    private static final String STDDEVIATIONSS_ENABLED_PREFERENCES = "vis_stddeviations_enabled";
    private static final String PREDICTIONS_ENABLED_PREFERENCES = "vis_predictionss_enabled";
    private static final String FITONLAST_NUMBER_PREFERENCES = "vis_fit_on_last";
    private static final String PREDICTIONEXTRA_NUMBER_PREFERENCES = "vis_prediction_extra";
    private static final String NAVERAGE_NUMBER_PREFERENCES = "vis_n_average";
    private static final String NSTDDEV_NUMBER_PREFERENCES = "vis_n_std_dev";
    private static final String FIT_SOURCE_PREFERENCES = "vis_fit_source";
    private static final String FIT_ALGORITHM_PREFERENCES = "vis_fit_algorithm";
    private static final String RESTRICT_DATA_PREFERENCES = "vis_restrict_data";

    private SharedPreferences preferences;

    private ScatterChart scatterChart;

    private ToggleButton toggleContractions;
    private ToggleButton toggleAverages;
    private ToggleButton toggleStdDeviations;
    private ToggleButton togglePredictions;

    private EditText fitOnLastEdit;
    private EditText predictionExtraEdit;
    private EditText nAverageEdit;
    private EditText nStdDevEdit;
    private EditText restrictEdit;
    private Button updatebutton;
    private Button allAvgButton;
    private Button allStdDevButton;
    private Button allDataButton;

    private Spinner fitSourceSpinner;
    private Spinner fitAlgorithmSpinner;

    private boolean contrationcsEnabled = true;
    private boolean averagesEnabled = true;
    private boolean stdDeviationsEnabled = true;
    private boolean predictionsEnabled = true;
    private int fitOnLast;
    private int predictionExtra;
    private int nAverage;
    private int nStdDev;
    private int fitSource;
    private int fitAlgorithm;
    private int restrict;

    private final String TAG = "VisualizeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize);

        scatterChart = (ScatterChart)findViewById(R.id.scatter_chart);
        toggleContractions = (ToggleButton)findViewById(R.id.toggleContraction);
        toggleAverages = (ToggleButton)findViewById(R.id.toggleAverage);
        toggleStdDeviations = (ToggleButton)findViewById(R.id.toggleStdDeviation);
        togglePredictions = (ToggleButton)findViewById(R.id.togglePrediction);

        fitOnLastEdit = (EditText)findViewById(R.id.fitOnLastEdit);
        predictionExtraEdit = (EditText)findViewById(R.id.predictionsExtraEdit);
        nAverageEdit = (EditText)findViewById(R.id.nAverageEdit);
        nStdDevEdit = (EditText)findViewById(R.id.nStdDevEdit);
        restrictEdit = (EditText)findViewById(R.id.restrictEdit);
        allAvgButton = (Button)findViewById(R.id.buttonAvgAll);
        allStdDevButton = (Button)findViewById(R.id.buttonAllStdDev);
        allDataButton = (Button)findViewById(R.id.buttonAllData);

        updatebutton = (Button)findViewById(R.id.buttonUpdate);

        fitSourceSpinner = (Spinner)findViewById(R.id.spinner_fit_source);
        fitAlgorithmSpinner = (Spinner)findViewById(R.id.spinner_fit_algorithm);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        contrationcsEnabled = preferences.getBoolean(CONTRACTIONS_ENABLED_PREFERENCES, true);
        averagesEnabled = preferences.getBoolean(AVERAGESS_ENABLED_PREFERENCES, true);
        stdDeviationsEnabled = preferences.getBoolean(STDDEVIATIONSS_ENABLED_PREFERENCES, true);
        predictionsEnabled = preferences.getBoolean(PREDICTIONS_ENABLED_PREFERENCES, true);

        fitOnLast = preferences.getInt(FITONLAST_NUMBER_PREFERENCES, 20);
        predictionExtra = preferences.getInt(PREDICTIONEXTRA_NUMBER_PREFERENCES, 10);

        nAverage = preferences.getInt(NAVERAGE_NUMBER_PREFERENCES, -1);
        nStdDev = preferences.getInt(NSTDDEV_NUMBER_PREFERENCES, -1);
        restrict = preferences.getInt(RESTRICT_DATA_PREFERENCES, -1);

        fitSource = preferences.getInt(FIT_SOURCE_PREFERENCES, 2);
        fitAlgorithm = preferences.getInt(FIT_ALGORITHM_PREFERENCES, 0);

        fitOnLastEdit.setText(Integer.toString(fitOnLast));
        predictionExtraEdit.setText(Integer.toString(predictionExtra));
        nAverageEdit.setText(Integer.toString(nAverage));
        nStdDevEdit.setText(Integer.toString(nStdDev));
        restrictEdit.setText(Integer.toString(restrict));

        toggleContractions.setChecked(contrationcsEnabled);
        toggleAverages.setChecked(averagesEnabled);
        toggleStdDeviations.setChecked(stdDeviationsEnabled);
        togglePredictions.setChecked(predictionsEnabled);

        ArrayAdapter<CharSequence> fitSourceAdapter = ArrayAdapter.createFromResource(this,
                R.array.fit_sources_str, android.R.layout.simple_spinner_item);
        fitSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitSourceSpinner.setAdapter(fitSourceAdapter);
        if (fitSource >= 0 && fitSource <= 2) {
            fitSourceSpinner.setSelection(fitSource);
        } else {
            fitSourceSpinner.setSelection(2);
        }
        fitSourceSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> fitAlgorithmAdapter = ArrayAdapter.createFromResource(this,
                R.array.fit_algorithm_str, android.R.layout.simple_spinner_item);
        fitAlgorithmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitAlgorithmSpinner.setAdapter(fitAlgorithmAdapter);
        if(fitAlgorithm >= 0 && fitAlgorithm <= 12) {
            fitAlgorithmSpinner.setSelection(fitAlgorithm);
        } else {
            fitAlgorithmSpinner.setSelection(0);
        }
        fitAlgorithmSpinner.setOnItemSelectedListener(this);

        allAvgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nAverage = -1;
                SharedPreferences.Editor ed = preferences.edit();
                ed.putInt(NAVERAGE_NUMBER_PREFERENCES, nAverage);
                ed.apply();

                nAverageEdit.setText(Integer.toString(nAverage));
            }
        });

        allStdDevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nStdDev = -1;
                SharedPreferences.Editor ed = preferences.edit();
                ed.putInt(NSTDDEV_NUMBER_PREFERENCES, nStdDev);
                ed.apply();

                nStdDevEdit.setText(Integer.toString(nStdDev));
            }
        });

        allDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restrict = -1;
                SharedPreferences.Editor ed = preferences.edit();
                ed.putInt(RESTRICT_DATA_PREFERENCES, restrict);
                ed.apply();

                restrictEdit.setText(Integer.toString(restrict));
            }
        });

        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newFitOnLast = fitOnLast;
                int newPredictionExtra = predictionExtra;
                int newNAverage = nAverage;
                int newNStdDev = nStdDev;
                int newRestrict = restrict;
                try {
                    newFitOnLast = Integer.parseInt(fitOnLastEdit.getText().toString());
                    fitOnLast = newFitOnLast;
                } catch (Exception e) {
                    Log.i(TAG, "error parse fitOnLast");
                }
                try {
                    newPredictionExtra = Integer.parseInt(predictionExtraEdit.getText().toString());
                    predictionExtra = newPredictionExtra;
                } catch (Exception e) {
                    Log.i(TAG, "error parse predictionExtra");
                }
                try {
                    newNAverage = Integer.parseInt(nAverageEdit.getText().toString());
                    nAverage = newNAverage;
                } catch (Exception e) {
                    Log.i(TAG, "error parse nAverage");
                }
                try {
                    newNStdDev = Integer.parseInt(nStdDevEdit.getText().toString());
                    nStdDev = newNStdDev;
                } catch (Exception e) {
                    Log.i(TAG, "error parse nStdDev");
                }
                try {
                    newRestrict = Integer.parseInt(restrictEdit.getText().toString());
                    restrict = newRestrict;
                } catch (Exception e) {
                    Log.i(TAG, "error parse restrict");
                }

                SharedPreferences.Editor ed = preferences.edit();
                ed.putInt(FITONLAST_NUMBER_PREFERENCES, fitOnLast);
                ed.putInt(PREDICTIONEXTRA_NUMBER_PREFERENCES, predictionExtra);
                ed.putInt(NAVERAGE_NUMBER_PREFERENCES, nAverage);
                ed.putInt(NSTDDEV_NUMBER_PREFERENCES, nStdDev);
                ed.putInt(RESTRICT_DATA_PREFERENCES, restrict);
                ed.apply();
                new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
            }
        });

        toggleContractions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putBoolean(CONTRACTIONS_ENABLED_PREFERENCES, isChecked);
                ed.apply();
                contrationcsEnabled = isChecked;
                new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
            }
        });

        toggleAverages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putBoolean(AVERAGESS_ENABLED_PREFERENCES, isChecked);
                ed.apply();
                averagesEnabled = isChecked;
                new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
            }
        });

        toggleStdDeviations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putBoolean(STDDEVIATIONSS_ENABLED_PREFERENCES, isChecked);
                ed.apply();
                stdDeviationsEnabled = isChecked;
                new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
            }
        });

        togglePredictions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putBoolean(PREDICTIONS_ENABLED_PREFERENCES, isChecked);
                ed.apply();
                predictionsEnabled = isChecked;
                new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i(TAG, "selected: " + i + " " + adapterView.getItemAtPosition(i));
        String selected = adapterView.getItemAtPosition(i).toString();
        SharedPreferences.Editor ed = preferences.edit();
        boolean update = false;
        switch (selected) {
            case "Co":
                fitSource = 0;
                update = true;
                break;
            case "Avg":
                fitSource = 1;
                update = true;
                break;
            case "S.Dev.":
                fitSource = 2;
                update = true;
                break;
            case "poly(1)":
                fitAlgorithm = 0;
                update = true;
                break;
            case "poly(2)":
                fitAlgorithm = 1;
                update = true;
                break;
            case "poly(3)":
                fitAlgorithm = 2;
                update = true;
                break;
            case "poly(4)":
                fitAlgorithm = 3;
                update = true;
                break;
            case "poly(5)":
                fitAlgorithm = 4;
                update = true;
                break;
            case "poly(6)":
                fitAlgorithm = 5;
                update = true;
                break;
            case "poly(7)":
                fitAlgorithm = 6;
                update = true;
                break;
            case "poly(8)":
                fitAlgorithm = 7;
                update = true;
                break;
            case "poly(9)":
                fitAlgorithm = 8;
                update = true;
                break;
            case "poly(10)":
                fitAlgorithm = 9;
                update = true;
                break;
            case "Exp":
                fitAlgorithm = 10;
                update = true;
                break;
            case "Log":
                fitAlgorithm = 11;
                update = true;
                break;
            case "Power":
                fitAlgorithm = 12;
                update = true;
                break;
            default:
                fitSource = 0;
                fitAlgorithm = 0;

        }
        ed.putInt(FIT_SOURCE_PREFERENCES, fitSource);
        ed.putInt(FIT_ALGORITHM_PREFERENCES, fitAlgorithm);
        ed.apply();
        if(update) {
            new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public static class Converters {
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        public static long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }


    private static class DateValueFormatter extends ValueFormatter {
        private static final SimpleDateFormat start_end_format = new SimpleDateFormat("H:m:s",
                Locale.getDefault());
        private final long startOffset;

        public DateValueFormatter(long startOffset) {
            super();
            this.startOffset = startOffset;
        }

        public DateValueFormatter() {
            super();
            this.startOffset = 0;
        }

        @Override
        public  String getAxisLabel(float value, AxisBase axis) {
            return start_end_format.format(Converters.fromTimestamp((long)value + startOffset));
        }

        public static String formatDateValue(float value) {
            return start_end_format.format(Converters.fromTimestamp((long)value));
        }
    }

    private class DBEntry {
        public final long startTime;
        public final long endTime;

        DBEntry(long start, long end) {
            this.startTime = start;
            this.endTime = end;
        }
    }


    private class SetupScatterCharAsyncTask extends AsyncTask<ScatterChart, Integer, Integer> {
        private WeakReference<Context> weakContext;
        private ScatterChart c;

        public SetupScatterCharAsyncTask(Context context, ScatterChart c) {
            this.weakContext = new WeakReference<>(context);
            this.c = c;
        }

        @Override
        protected Integer doInBackground(ScatterChart... params) {
            Context context = weakContext.get();
            if(context == null) {
                return 0;
            }

            Cursor cursor = context.getContentResolver().query(
                    ContractionContract.Contractions.Companion.getCONTENT_URI(),
                    null, null, null, null);

            if (cursor != null) {
                ArrayList<Entry> entries = new ArrayList<>();
                final int startColumnIndex = cursor.getColumnIndex(ContractionContract.Contractions.COLUMN_NAME_START_TIME);
                final int endColumnIndex = cursor.getColumnIndex(ContractionContract.Contractions.COLUMN_NAME_END_TIME);



                ArrayList<DBEntry> times = new ArrayList<>();
                while (cursor.moveToNext()) {
                    if(!cursor.isNull(endColumnIndex) && !cursor.isNull(startColumnIndex)) {
                        times.add(new DBEntry(cursor.getLong(startColumnIndex),
                                cursor.getLong(endColumnIndex)));
                    }
                }

                Comparator<DBEntry> sortByDBStart = new Comparator<DBEntry>() {
                    @Override
                    public int compare(DBEntry entry, DBEntry other) {
                        final long ex = entry.startTime;
                        final long ox = other.startTime;
                        if (ex > ox) {
                            return 1;
                        } else if (ex == ox) {
                            return 0;
                        }
                        return -1;
                    }
                };

                Collections.sort(times, sortByDBStart);

                List<DBEntry> tselect;
                if(restrict > 0) {
                    tselect = times.subList(Math.max(0, times.size() - restrict), times.size());
                } else {
                    tselect = times.subList(0, times.size());
                }

                Log.i(TAG, "subselection length: " + tselect.size());

                long firstentry = -101;
                for(DBEntry e : tselect) {
                    long startTime = e.startTime;
                    if (firstentry < -100) {
                        firstentry = startTime;
                        Log.i(TAG, "set firstentry to: " + firstentry);
                    }
                    long originalStart = startTime;
                    startTime = startTime - firstentry;
                    long endTime = e.endTime;
                    endTime = endTime - firstentry;
                    final long duration = max((endTime - startTime) / 1000, 0);
                    Entry ent = new Entry(startTime / 1000, duration);
                    Log.d(TAG, "Entry: (" + originalStart + ") " + ent.getX() + " " + ent.getY());
                    entries.add(ent);
                }

//                Comparator<Entry> sortByStartTime = new Comparator<Entry>() {
//                    @Override
//                    public int compare(Entry entry, Entry other) {
//                        final float ex = entry.getX();
//                        final float ox = other.getX();
//                        if (ex > ox) {
//                            return 1;
//                        } else if (ex == ox) {
//                            return 0;
//                        }
//                        return -1;
//                    }
//                };
//                Collections.sort(entries, sortByStartTime);

                ArrayList<Entry> averages = new ArrayList<>();
                ArrayList<Entry> std_deviations = new ArrayList<>();
                double avg = Double.NaN;
                double out_avg = Double.NaN;
                double Sn = 0f;
                long ctr_avg = 1;
                long ctr_std = 1;
                int i = 0;
                int nEntries = entries.size();
                int nAvgStart = 0;
                if (nAverage > 0) {
                    nAvgStart = nEntries - Math.min(nEntries, nAverage) - 1;
                }
                int nStdDevStart = 0;
                if (nStdDev > 0) {
                    nStdDevStart = nEntries - Math.min(nEntries, nStdDev) - 1;
                }
                for(Entry e : entries) {
                    if (i >= nAvgStart) {
                        if (Double.isNaN(avg)) {
                            avg = e.getY();
                        } else {
                            float v = e.getY();
                            double new_avg = avg + (v - avg) / (double) ctr_avg;
                            float xval = e.getX();
                            averages.add(new Entry(xval, (float) new_avg));
                            avg = new_avg;
                            ctr_avg += 1;
                            Log.i(TAG, "Avg: " + xval + " " + new_avg);
                        }
                    }
                    if (i >= nStdDevStart) {
                        if (Double.isNaN(out_avg)) {
                            out_avg = e.getY();
                        } else {
                            float v_std = e.getY();
                            double new_avg_std = out_avg + (v_std - out_avg) / (double) ctr_std;
                            Sn += (v_std - out_avg) * (v_std - new_avg_std);
                            float xval_std = e.getX();
                            double new_dev = Math.sqrt(Sn / (double) ctr_std);
                            std_deviations.add(new Entry(xval_std, (float) new_dev));
                            //                        std_deviations.add(new Entry(e.getX(), (float)(new_avg - (new_dev / 2.0))));
                            //                        std_deviations.add(new Entry(e.getX(), (float)(new_avg + (new_dev / 2.0))));
                            out_avg = new_avg_std;
                            ctr_std += 1;
                            Log.i(TAG, "Std. Dev: " + xval_std + " " + new_dev);
                            Log.i(TAG, "Std. Dev. Avg: " + xval_std + " " + new_avg_std);
                        }
                    }

                    i++;
                }

                ArrayList<Entry> predictions = new ArrayList<>();

                ArrayList<Entry> prediction_source;
                if(fitSource == 0) {
                    prediction_source = entries;
                } else if(fitSource == 1) {
                    prediction_source = averages;
                } else {
                    prediction_source = std_deviations;
                }

                int total_source = prediction_source.size();
                Log.i(TAG, "total source: " + total_source);

                double[] x = new double[Math.min(fitOnLast, total_source)];
                double[] y = new double[Math.min(fitOnLast, total_source)];

                int dataPoints = 0;
                for(int i_dat = 0; i_dat < prediction_source.size(); i_dat++) {
                    if (i_dat >= total_source - Math.min(fitOnLast, total_source)) {
                        x[dataPoints] = prediction_source.get(i_dat).getX();
                        y[dataPoints] = prediction_source.get(i_dat).getY();
                        Log.i(TAG, "Fit Point: " + x[dataPoints] + " " + y[dataPoints]);
                        dataPoints++;
                    }
                }

                Log.i(TAG, "# points for fit: " + dataPoints);
                TrendLine pred;
                switch(fitAlgorithm) {
                    case 1:
                        pred = new PolyTrendLine(2);
                        break;
                    case 2:
                        pred = new PolyTrendLine(3);
                        break;
                    case 3:
                        pred = new PolyTrendLine(4);
                        break;
                    case 4:
                        pred = new PolyTrendLine(5);
                        break;
                    case 5:
                        pred = new PolyTrendLine(6);
                        break;
                    case 6:
                        pred = new PolyTrendLine(7);
                        break;
                    case 7:
                        pred = new PolyTrendLine(8);
                        break;
                    case 8:
                        pred = new PolyTrendLine(9);
                        break;
                    case 9:
                        pred = new PolyTrendLine(10);
                        break;
                    case 10:
                        pred = new ExpTrendLine();
                        break;
                    case 11:
                        pred = new LogTrendLine();
                        break;
                    case 12:
                        pred = new PowerTrendLine();
                        break;
                    case 0:
                    default:
                        pred = new PolyTrendLine(1);
                        break;
                }

//                TrendLine pred = new LogTrendLine();

                if(dataPoints > 4 && predictionsEnabled) {
                    pred.setValues(y, x);

                    int lastx = 0;
                    for (int i_pred = 0; i_pred < prediction_source.size(); i_pred++) {
                        float xval = prediction_source.get(i_pred).getX();
                        float yval = (float) pred.predict((double) xval);
                        predictions.add(new Entry(xval, yval));

                        lastx = i_pred;
                        Log.i(TAG, "Point: " + xval + " " + yval);
                    }
                    float xlast_std_dev = prediction_source.get(prediction_source.size() - 1).getX();
                    final float interval = 60;
                    for (int i_fut = 0; i_fut < predictionExtra; i_fut++) {
                        float xval = xlast_std_dev + i_fut * interval;
                        float yval = (float) pred.predict((double)xval);
                        predictions.add(new Entry(xval, yval));

                        Log.i(TAG, "Extra Point: " + xval + " " + yval);
                    }
                } else {
                    try {
                        Toast.makeText(weakContext.get(), getResources().getString(R.string.notEnough), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }
                    if (averages.size() >= 1) {
                        predictions.add(new Entry(averages.get(0).getX(), averages.get(0).getY()));
                    } else {
                        predictions.add(new Entry(0, 0));
                    }
                }


                ScatterDataSet scatterDataSetEntries = new ScatterDataSet(entries, "Contractions");
                ScatterDataSet scatterDataSetAverages= new ScatterDataSet(averages, "Average");
                ScatterDataSet scatterDataSetStdDeviations = new ScatterDataSet(std_deviations, "Std. Deviations");
                ScatterDataSet scatterDataSetPredictions = new ScatterDataSet(predictions, "Prediction");
//            scatterDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                scatterDataSetEntries.setColor(ColorTemplate.rgb("#2ecc71"));
                scatterDataSetAverages.setColor(ColorTemplate.rgb("#f1c40f"));
                scatterDataSetStdDeviations.setColor(ColorTemplate.rgb("#e74c3c"));
                ScatterData scatterData = new ScatterData();
                if(contrationcsEnabled) {
                    scatterData.addDataSet(scatterDataSetEntries);
                }
                if(averagesEnabled) {
                    scatterData.addDataSet(scatterDataSetAverages);
                }
                if(stdDeviationsEnabled) {
                    scatterData.addDataSet(scatterDataSetStdDeviations);
                }
                if(predictionsEnabled) {
                    scatterData.addDataSet(scatterDataSetPredictions);
                }

                DateValueFormatter formatter = new DateValueFormatter(max(firstentry, 0l));

                XAxis xAxis = scatterChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(formatter);
                xAxis.setGranularity(1f);

                scatterChart.setData(scatterData);
//            scatterChart.animateXY(500, 500);
                scatterChart.invalidate();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer agentsCount) {
            Context context = weakContext.get();
            if(context == null) {
                return;
            }
        }
    }
}
