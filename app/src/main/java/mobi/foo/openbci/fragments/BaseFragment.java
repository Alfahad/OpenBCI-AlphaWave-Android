package mobi.foo.openbci.fragments;

import mobi.foo.openbci.MainActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {
	protected Context mContext;
	protected View mView;
	protected boolean changeActionBar = false;
	protected Activity mActivity;

	protected final View findViewById(int viewId) {
		return mView.findViewById(viewId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		mActivity = activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext = null;
	}

	/**
	 * Initialize your GUI in this function
	 * 
	 * @param state
	 *            Bundle passed to the {@link Activity#onCreate(Bundle)}
	 */
	public abstract void GUI(Bundle state);

	/**
	 * Returns the layout resource for this Activity
	 * 
	 * @param state
	 *            Bundle passed to the {@link Activity#onCreate(Bundle)}
	 */
	public abstract int getContentView(Bundle state);

	public void preGUI(Bundle state) {
	}

	public void postGUI(Bundle state) {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		preGUI(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mContext == null)
			mContext = inflater.getContext();
		mView = inflater.inflate(getContentView(savedInstanceState), container,
				false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		postGUI(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GUI(null);
	}

	public MainActivity getMainActivity() {
		return (MainActivity) mContext;
	}

	public void replace(Fragment f, boolean addToBackStack) {
		if (getMainActivity() != null)
			getMainActivity().replace(f, addToBackStack);
		// if (addToBackStack) {
		// FragmentTransaction ft = getFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		// ft.replace(R.id.container, f,
		// "").addToBackStack("").commitAllowingStateLoss();
		// } else {
		// FragmentTransaction ft = getFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		// ft.replace(R.id.container, f, "").commitAllowingStateLoss();
		// }
	}

	public void replace(Fragment f) {
		replace(f, true);
	}

	public void listen(int i, OnClickListener ocl) {
		findViewById(i).setOnClickListener(ocl);
	}
}