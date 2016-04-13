package mobi.foo.openbci.fragments;

import mobi.foo.openbci.R;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TutorialConnectFragment extends BaseFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void GUI(Bundle state) {
		findViewById(R.id.txtnext).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getMainActivity().replace(new ConnectionFragment(),false);
			}
		});

	}

	@Override
	public int getContentView(Bundle state) {
		return R.layout.fragment_connection_error;
	}

}
