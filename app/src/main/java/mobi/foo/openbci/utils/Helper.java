package mobi.foo.openbci.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import mobi.foo.openbci.R;
import mobi.foo.openbci.Valuesholder;
import mobi.foo.openbci.fragments.ConnectionFragment;

/**
 * Created by wassim on 4/30/2015.
 */
public class Helper {

    public static LineDataSet createSet(ArrayList<Entry> entries, String title) {

        LineDataSet set1 = new LineDataSet(entries,
                title);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setDrawCircles(true);
        set1.setLineWidth(6f);
        set1.setCircleSize(8f);
        set1.setDrawValues(true);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.rgb(104, 241, 175));
        set1.setFillColor(ColorTemplate.getHoloBlue());
        return set1;


    }

    public static void alarm(Activity activity) {
        if (activity != null) {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(activity
                    .getApplicationContext(), notification);
            r.play();
        }

    }


    public static void vibrate(Activity activity) {
        if (activity != null) {
            Vibrator v = (Vibrator) activity.getSystemService(
                    Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        }
    }

    public static int getColor(int level) {
        int value = (int) ((float) level * 2.55);
        return Color.rgb(255 - value, value, 0);
    }

    public static void prepareChat(LineChart mChart, Activity activity) {
        Typeface tf = Typeface.createFromAsset(activity.getAssets(),
                "OpenSans-Regular.ttf");

        mChart.setDescription("time");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        XAxis x = mChart.getXAxis();
        x.setTypeface(tf);
        x.setEnabled(false);

        YAxis y = mChart.getAxisLeft();
        y.setTypeface(tf);
        y.setLabelCount(5);
        y.setEnabled(false);

        mChart.getAxisRight().setEnabled(false);

        mChart.animateXY(1000, 1000);

        mChart.invalidate();

        LineData data = new LineData();

        // set data
        mChart.setData(data);
        mChart.notifyDataSetChanged();


    }

    public static void addEntry(Activity activity, ArrayList<Entry> entries, TextView valueText, LineChart mChart, int value, Valuesholder values, boolean history) {
//        switcher.setDisplayedChild(1);
        LineData line = mChart.getLineData();
        if (line != null) {

            LineDataSet set = line.getDataSetByIndex(0);
            if (set == null) {
                set = Helper.createSet(entries, activity.getString(R.string.drawsiness));
                line.addDataSet(set);
            }
            line.addXValue((1990 + values.count) + "");
            values.summation += value;
            line.addEntry(new Entry(value, values.count), 0);
            mChart.notifyDataSetChanged();

            mChart.setVisibleXRange(5);
            mChart.moveViewToX(line.getXValCount());
            set.setColor(Helper.getColor((int) value));
            if(!history)
                return;
            valueText.setText(":  " + value + "%"
                    + (value < 25 ? " !!" : ""));
            valueText.setTextColor(Helper.getColor((int) value));
            Helper.manageAlert(activity, (int) value, values);


            values.count++;
        }
    }

    public static void manageAlert(Activity activity, int value, Valuesholder values) {
        if (value > ConnectionFragment.threshold_normal)
            return;

        if (value < ConnectionFragment.threshold_danger) {

            Helper.vibrate(activity);
            values.vibrationCount++;
        }
        Helper.alarm(activity);
        values.alertCount++;
    }


}
