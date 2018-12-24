package com.hitsquadtechnologies.sifyconnect.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hitsquadtechnologies.sifyconnect.Model.AntennaSignal;
import com.hitsquadtechnologies.sifyconnect.R;

import java.util.List;

public class AntennaAdapter extends ArrayAdapter<AntennaSignal> {
    private List<AntennaSignal> mAntennaSignals;
    private Activity mActivity;

    public AntennaAdapter(Activity a, List<AntennaSignal> antennaSignals) {
        super(a, R.layout.antenna_view, antennaSignals);
        this.mAntennaSignals = antennaSignals;
        this.mActivity = a;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AntennaSignal antennaSignalData = mAntennaSignals.get(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.antenna_view, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.antennaNameView = convertView.findViewById(R.id.antenna_name_view);
            viewHolder.antennaSignalView = convertView.findViewById(R.id.antenna_signal_view);
            viewHolder.signalScale = convertView.findViewById(R.id.signal_diff_scale);
            viewHolder.signalScaleMax = convertView.findViewById(R.id.signal_diff_scale_max);
            viewHolder.antennaNameLabel = convertView.findViewById(R.id.antenna_name);
            viewHolder.radioNameLabel = convertView.findViewById(R.id.radio_name);
            viewHolder.bestSignalValue = convertView.findViewById(R.id.antenna_best_signal);
            viewHolder.currentSignalValue = convertView.findViewById(R.id.antenna_current_signal);
            viewHolder.signalDiffSeekbar = convertView.findViewById(R.id.antenna_signal_diff);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.antennaNameView.setBackgroundResource(antennaSignalData.getBg1Resource());
        viewHolder.antennaSignalView.setBackgroundResource(antennaSignalData.getBg2Resource());
        viewHolder.antennaNameLabel.setText(antennaSignalData.getName());
        viewHolder.radioNameLabel.setText(antennaSignalData.getDeviceRadioMode());
        viewHolder.bestSignalValue.setText("" + antennaSignalData.getBest());
        viewHolder.currentSignalValue.setText("" + antennaSignalData.getCurrent());
        int max = (int) Math.ceil(antennaSignalData.getDiff() / 10.0d);
        max = (max > 0 ? max : 1) * 10;
        renderScale(mActivity, viewHolder.signalScale, max);
        viewHolder.signalScaleMax.setText("" + max);
        viewHolder.signalDiffSeekbar.setMax(max);
        viewHolder.signalDiffSeekbar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        viewHolder.signalDiffSeekbar.setProgress(antennaSignalData.getDiff());
        return convertView;
    }

    private void renderScale(Activity a, LinearLayout v, int max) {
        v.removeAllViews();
        for(int i = 0; i < 5; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            TextView textView = new TextView(a);
            textView.setText(""+ (i * max / 5));
            textView.setLayoutParams(layoutParams);
            v.addView(textView);
        }
    }

    static class ViewHolder{
        View antennaNameView;
        View antennaSignalView;
        LinearLayout signalScale;
        TextView signalScaleMax;
        TextView antennaNameLabel;
        TextView radioNameLabel;
        TextView bestSignalValue;
        TextView currentSignalValue;
        SeekBar signalDiffSeekbar;
    }
}
