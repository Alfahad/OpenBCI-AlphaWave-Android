package mobi.foo.openbci.fragments;

import mobi.foo.openbci.R;
import mobi.foo.openbci.Valuesholder;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FinalResultsFragment extends BaseFragment implements
		OnClickListener {

	Valuesholder values;

	public FinalResultsFragment(Valuesholder values) {
		this.values = values;
	}

	@Override
	public void GUI(Bundle state) {
		// TODO Auto-generated method stub
		findViewById(R.id.txtstartagain).setOnClickListener(this);
		findViewById(R.id.txtexit).setOnClickListener(this);
		fT(R.id.txtalarmcount).setText(values.alertCount + "");
		fT(R.id.txtdrawaverage).setText(values.summation/values.count + "%");
		fT(R.id.txtrecordscount).setText(values.count + "");
		fT(R.id.txtvibrationcount).setText(values.vibrationCount + "");
	}

	private TextView fT(int id) {
		return (TextView) findViewById(id);
	}

	@Override
	public int getContentView(Bundle state) {
		// TODO Auto-generated method stub
		return R.layout.fragment_finalresults;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.txtstartagain:
			replace(new ConnectionFragment(), false);
			break;
		case R.id.txtexit:
			getMainActivity().finish();
			break;

		default:
			break;
		}
	}

}
