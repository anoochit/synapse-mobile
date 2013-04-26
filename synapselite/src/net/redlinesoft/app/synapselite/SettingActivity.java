package net.redlinesoft.app.synapselite;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings.Secure;
import android.util.Log;

public class SettingActivity extends PreferenceActivity {
	
	String DATADIR = Environment.getExternalStorageDirectory().toString()+"/Synapse";
	
	SharedPreferences pref;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
			
		// set value to AppVersion
		Preference prefVersion = findPreference("AppVersion");
		String strVersion=prefVersion.getTitle().toString();
		PackageInfo packageInfo;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
			String strVersionName = packageInfo.versionName;
			strVersion =strVersion + " : " + strVersionName ;
			//prefVersion.setTitle(strVersion);
			prefVersion.setSummary(strVersion);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set DeviceID		
		String androidId = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID); 
		Preference prefDeviceID = findPreference("DeviceID");
		String strDeviceID=prefDeviceID.getTitle().toString();
		//prefDeviceID.setTitle(strDeviceID + androidId);
		prefDeviceID.setSummary(strDeviceID  + " : " +  androidId);
		
		// TODO block event, should find the best way than this
		prefDeviceID.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				return false;
			}
		});
		
		prefVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return false;
			}
		});
		
		
	}


}
