package net.redlinesoft.app.synapselite;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	SharedPreferences pref;
	TextView txtMarquee;
	DigitalClock digiClock;
	WebView webView;
	ImageView imageView;

	String TEXT_COLOR, TEXT_BACKGROUND, CLOCK_COLOR, CLOCK_BACKGROUND,TEXT_MARQUEE;
	String DEFAULT_DATASTORE, DEFAULT_WEBURL, DEFAULT_IMAGEPATH;
	Boolean TEXT_ENABLE, CLOCK_ENABLE;
	String DATADIR = Environment.getExternalStorageDirectory().toString()+"/Synapse";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// init widget
		initWidget();
		
		// check datadir
		checkDataDir();
		
		// load default and set properties
		loadSharePreference();

		// set appearance
		setAppeareance();

		// load data from external storage
		loadContent(DEFAULT_DATASTORE);
		
		// play content
		playContent(DEFAULT_DATASTORE);

		// setting listener		
		digiClock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// load setting preference
				Intent intent = new Intent(getApplicationContext(),	SettingActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void checkDataDir() { 
		File folder = new File(DATADIR);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	private void loadContent(String ds) {
		if (ds.contentEquals("image")) {
			
		}
	}
	
	private void playContent(String ds) {	
		// play web content
		if (ds.contentEquals("web")) {
			if (isNetworkAvailable()==true) {
				Log.d("LOG", DEFAULT_WEBURL);
				webView.setVisibility(View.VISIBLE);
				imageView.setVisibility(View.INVISIBLE);
				webView.loadUrl(DEFAULT_WEBURL);
				WebSettings webSettings = webView.getSettings();
				webView.getSettings().setJavaScriptEnabled(true);
				webView.getSettings().setPluginsEnabled(true);
				webView.getSettings().setAllowFileAccess(true);
				
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return true;
					}
				});
			} else {
				Log.d("LOG", "cannot load web content");
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.alert);
				alert.setMessage(R.string.alert_no_network_connetion);
				alert.setPositiveButton(R.string.bnt_setting, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						startActivity(intent);
					}
				});
				alert.setNegativeButton(R.string.bnt_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				alert.show();
			}
		}
		
		// play image
		if (ds.contentEquals("image")){			
			imageView.setVisibility(View.VISIBLE);
			webView.setVisibility(View.INVISIBLE);
		}
		
	}
 
	private void initWidget() {
		// view mapping
		txtMarquee = (TextView) findViewById(R.id.MarqueeText);
		digiClock = (DigitalClock) findViewById(R.id.digitalClock);
		webView = (WebView) findViewById(R.id.webView);	
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageResource(R.drawable.synapse);
		// preference mapping
		pref = PreferenceManager.getDefaultSharedPreferences(this);		
	}

	private void setAppeareance() {
		// set text marquee
		if (TEXT_ENABLE == true) {
			txtMarquee.setTextColor(Color.parseColor(TEXT_COLOR));
			txtMarquee.setBackgroundColor(Color.parseColor(TEXT_BACKGROUND));
			txtMarquee.setText(TEXT_MARQUEE);
			txtMarquee.setVisibility(View.VISIBLE);
		} else {
			txtMarquee.setVisibility(View.INVISIBLE);
		}
		// set clock
		if (CLOCK_ENABLE == true) {
			digiClock.setTextColor(Color.parseColor(CLOCK_COLOR));
			digiClock.setBackgroundColor(Color.parseColor(CLOCK_BACKGROUND));
			digiClock.setVisibility(View.VISIBLE);
		} else {
			digiClock.setVisibility(View.INVISIBLE);
		}
	}

	private void loadSharePreference() {
		// text marquee		
		TEXT_ENABLE = pref.getBoolean("EnableTextMarquee", true);
		TEXT_COLOR = pref.getString("TextColor", "#FFFFFF");
		TEXT_BACKGROUND = pref.getString("TextColorBackground", "#0000FF");
		TEXT_MARQUEE = pref.getString("TextMarquee","*** This is a example text message, click at clock to set your own text ***");
		
		// clock
		CLOCK_ENABLE = pref.getBoolean("EnableClock", true);
		CLOCK_COLOR = pref.getString("ClockTextColor", "#000000");
		CLOCK_BACKGROUND = pref.getString("ClockTextColorBackground", "#FFFFFF");
		
		// default data store
		DEFAULT_DATASTORE = pref.getString("DataSource", "image");
		
		// web url
		DEFAULT_WEBURL = pref.getString("WebSiteURL", "https://demo.geckoboard.com/dashboard/D66E964F5F044B85");
		
		// image slideshow path
		DEFAULT_IMAGEPATH = pref.getString("ImagePath", DATADIR);

	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
 
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// load default and set properties
		loadSharePreference();
		// set appearance
		setAppeareance();
		// load data from external storage
		loadContent(DEFAULT_DATASTORE);
		// play content
		playContent(DEFAULT_DATASTORE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
