package pl.tinyproj.easytogo;

import java.net.URLDecoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Caller extends Activity implements OnClickListener {
	private static final String TAG = "EasyToGo";
	private static final String FOREIGN_NO_CHOICE = "2";
	private static final String COMMA = ",";
	private static final String CALL_ACTION = "android.intent.action.CALL_PRIVILEGED";
	private static final String HASH_CODE = "%23";
	private static final int PICK_CONTACT = 0;

	private View call;
	private String callingInfoLabel;
	private String noSkypeToGoPhoneLabel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		callingInfoLabel = getResources().getText(R.string.common_calling_info_label).toString();
		noSkypeToGoPhoneLabel = getResources().getText(R.string.common_no_skype_to_go_phone_label).toString();

		Intent intent = getIntent();
		if (null != intent) {
			if (CALL_ACTION.equals(intent.getAction())) {
				Uri uri = intent.getData();

				String phoneNumber = uri.toString().substring(4);
				Log.d(TAG, "Phone number before decoding " + phoneNumber);
				phoneNumber = URLDecoder.decode(phoneNumber);
				Log.d(TAG, "Phone number after decoding " + phoneNumber);
				call(phoneNumber);
				finish();
			}
		}

		setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		call = findViewById(R.id.call);
		call.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return applyMenuChoice(item) || super.onOptionsItemSelected(item);
	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PICK_CONTACT:
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String phoneNumber = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
					Log.d(TAG, "Choosen phone number " + phoneNumber);
					call(phoneNumber);

				}
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (call.equals(v)) {
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		}
	}

	private void call(String number) {
		String url = createTelUrl(number);
		Log.d(TAG, "Created URL " + url);
		if (null != url) {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(intent);
		}
	}

	private String createTelUrl(String number) {
		String result = null;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String skypeToGoPhone = prefs.getString("skype_to_go_phone", null);

		if (TextUtils.isEmpty(skypeToGoPhone)) {
			startActivity(new Intent(this, Preferences.class));
			Toast.makeText(this, noSkypeToGoPhoneLabel, Toast.LENGTH_LONG).show();
		} else {
			String phoneNumber = formatPhoneNumber(number);

			StringBuilder buf = new StringBuilder("tel:");
			buf.append(skypeToGoPhone);
			buf.append(COMMA);
			buf.append(FOREIGN_NO_CHOICE);
			buf.append(COMMA);
			buf.append(phoneNumber);
			buf.append(HASH_CODE);

			Toast.makeText(this, String.format(callingInfoLabel, phoneNumber, skypeToGoPhone), Toast.LENGTH_LONG).show();

			result = buf.toString();
		}

		return result;
	}

	private String formatPhoneNumber(String phone) {
		phone = PhoneNumberUtils.stripSeparators(phone);
		return phone;
	}
}