package mobi.foo.openbci;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View.OnClickListener;

public abstract class BaseActivity extends ActionBarActivity {
	protected Context mContext = this;

	public void listen(int i, OnClickListener ocl) {
		findViewById(i).setOnClickListener(ocl);
	}

	public void clearBackStack() {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		int count = fm.getBackStackEntryCount();
		for (int i = 0; i < count; ++i) {
			fm.popBackStackImmediate();
		}
	}

	public void add(Fragment f, boolean addToBackStack) {
		clearBackStack();
		FragmentTransaction ft = getFragmentTransaction().add(R.id.container,
				f, f.getClass().getName());
		if (addToBackStack) {
			ft.addToBackStack("");
		}
		ft.commitAllowingStateLoss();
	}

	public boolean remove(Class<? extends Fragment> fragmentClass) {
		FragmentManager ft = getSupportFragmentManager();
		Fragment f = ft.findFragmentByTag(fragmentClass.getClass().getName());
		if (f == null)
			return true;
		ft.beginTransaction().remove(f).commit();
		return ft.findFragmentByTag(fragmentClass.getClass().getName()) == null;
	}

	public boolean backTo(Class<? extends Fragment> fragmentClass) {
		FragmentManager ft = getSupportFragmentManager();
		Fragment f = ft.findFragmentByTag(fragmentClass.getClass().getName());
		if (f == null)
			return true;
		ft.beginTransaction().remove(f).commit();
		return ft.findFragmentByTag(fragmentClass.getClass().getName()) == null;

	}

	public void add(Fragment f) {
		add(f, true);
	}

	public void replace(Fragment f, boolean addToBackStack) {
		FragmentTransaction ft = getFragmentTransaction().replace(
				R.id.container, f, f.getClass().getName());
		if (addToBackStack) {
			ft.addToBackStack("");
		}
		ft.commitAllowingStateLoss();
	}

	public void replace(Fragment f) {
		replace(f, true);
	}

	private FragmentTransaction getFragmentTransaction() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		return ft;
	}
}
