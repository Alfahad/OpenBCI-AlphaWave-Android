package mobi.foo.openbci;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		(new Handler()).postDelayed(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(Splash.this, MainActivity.class));
				finish();

			}
		}, 2000);
	}
}
