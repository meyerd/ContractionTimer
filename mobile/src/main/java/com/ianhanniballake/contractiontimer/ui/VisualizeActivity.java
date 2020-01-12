package com.ianhanniballake.contractiontimer.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ianhanniballake.contractiontimer.R;
import com.ianhanniballake.contractiontimer.provider.ContractionContract;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static java.lang.StrictMath.max;

public class VisualizeActivity extends AppCompatActivity {

    private ScatterChart scatterChart;

    private final String TAG = "VisualizeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize);

        scatterChart = (ScatterChart)findViewById(R.id.scatter_chart);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new SetupScatterCharAsyncTask(getApplicationContext(), scatterChart).execute();
    }

    public static class Converters {
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        public static long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }


    public class DateValueFormatter extends ValueFormatter {
        private final SimpleDateFormat start_end_format = new SimpleDateFormat("H:m:s",
                Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return start_end_format.format(Converters.fromTimestamp((long)value));
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
                while (cursor.moveToNext()) {
                    if(!cursor.isNull(endColumnIndex) && !cursor.isNull(startColumnIndex)) {
                        final long startTime = cursor.getLong(startColumnIndex);
                        final long endTime = cursor.getLong(endColumnIndex);
                        final long duration = max((endTime - startTime) / 1000 , 0);
                        Entry e = new Entry(startTime, duration);
                        Log.d(TAG, "Entry: " + startTime + " " + duration);
                        entries.add(e);
                    }
                }

                Comparator<Entry> sortByStartTime = new Comparator<Entry>() {
                    @Override
                    public int compare(Entry entry, Entry other) {
                        final float ex = entry.getX();
                        final float ox = other.getX();
                        if (ex > ox) {
                            return 1;
                        } else if (ex == ox) {
                            return 0;
                        }
                        return -1;
                    }
                };
                Collections.sort(entries, sortByStartTime);

                ArrayList<Entry> averages = new ArrayList<>();
                ArrayList<Entry> std_deviations = new ArrayList<>();
                double avg = Double.NaN;
                double Sn = 0f;
                long ctr = 1;
                for(Entry e : entries) {
                    if(Double.isNaN(avg)) {
                        avg = e.getY();
                    } else {
                        float v = e.getY();
                        double new_avg = avg + (v - avg) / (double)ctr;
                        Sn += (v - avg) * (v - new_avg);
                        averages.add(new Entry(e.getX(), (float)new_avg));
                        double new_dev = Math.sqrt(Sn/ctr);
                        std_deviations.add(new Entry(e.getX(), (float)(new_avg - (new_dev / 2.0))));
                        std_deviations.add(new Entry(e.getX(), (float)(new_avg + (new_dev / 2.0))));
                        avg = new_avg;
                        ctr += 1;
                    }
                }

                ScatterDataSet scatterDataSetEntries = new ScatterDataSet(entries, "Contractions");
                ScatterDataSet scatterDataSetAverages= new ScatterDataSet(averages, "Average");
                ScatterDataSet scatterDataSetStdDeviations = new ScatterDataSet(std_deviations, "Std. Deviations");
//            scatterDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                scatterDataSetEntries.setColor(ColorTemplate.rgb("#2ecc71"));
                scatterDataSetAverages.setColor(ColorTemplate.rgb("#f1c40f"));
                scatterDataSetStdDeviations.setColor(ColorTemplate.rgb("#e74c3c"));
//                ScatterData scatterData = new ScatterData(scatterDataSetEntries, scatterDataSetAverages, scatterDataSetStdDeviations);
                ScatterData scatterData = new ScatterData(scatterDataSetEntries);

                DateValueFormatter formatter = new DateValueFormatter();

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
