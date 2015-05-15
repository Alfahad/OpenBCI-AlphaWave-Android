package mobi.foo.openbci;

import android.graphics.Typeface;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by wassim on 5/1/2015.
 */
public class Valuesholder {
    public int count = 0;
    public int alertCount = 0, vibrationCount = 0;
    public long summation = 0;
}
