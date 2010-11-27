package pl.tinyproj.easytogo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author tinyproj@google.com
 * 
 */
public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

}
