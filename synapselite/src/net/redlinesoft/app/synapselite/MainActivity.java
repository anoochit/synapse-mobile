package net.redlinesoft.app.synapselite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DigitalClock;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	static final int REQUEST_PREFERENCE_CODE = 0;
	File[] listContent;
	
	int CURRENT_ITEM=0;
	int delay = 1000; 
    int period = 10000;
	
    Timer timer;
    
    FrameLayout overlayInstruction;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// init widget
		initWidget();
				
		// check datadir
		checkDataDir();
		
		// first run
		firstRun();
		
		// load default and set properties
		loadSharePreference();

		// set appearance
		setAppeareance();

		// load data from external storage
		listContent=loadContent(DEFAULT_DATASTORE);
		
		// play content
		playContent(DEFAULT_DATASTORE);
		
		// setting listener		
		digiClock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// load setting preference
				Intent intent = new Intent(getApplicationContext(),	SettingActivity.class);
				startActivityForResult(intent, REQUEST_PREFERENCE_CODE);
			}
		});
	}
	
	private void firstRun() {		

        boolean firstrun = pref.getBoolean("firstrun", true);
        if (firstrun) {
        	Log.d("LOG","first run...");
    		// check first run 
            SharedPreferences.Editor e = pref.edit();
            e.putBoolean("firstrun", false);
            e.commit();            
            // copy asset
            copyAsset();
            // show overlay
            overlayInstruction = (FrameLayout) findViewById(R.id.overlayInstruction);
            overlayInstruction.setVisibility(View.VISIBLE);
            
            overlayInstruction.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					 overlayInstruction.setVisibility(View.GONE);
				}
			});
        }
	}

	private void copyAsset() { 
		AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(files[i]);
                out = new FileOutputStream(DATADIR + "/" + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
	}

	private void copyFile(InputStream in, OutputStream out) {
		byte[] buffer = new byte[1024];
		int read;
		try {
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}	
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}
	}

	private void checkDataDir() { 
		File folder = new File(DATADIR);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	private File[] loadContent(String ds) {
		File[] files = null;
		if (ds.contentEquals("image")) {
			Log.d("LOG","load image list...");
			File imagePath = new File(DEFAULT_IMAGEPATH);
			files = imagePath.listFiles(new FilenameFilter() {				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg");
				}
			});
			
		}
		return files;
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
			
			 if ((listContent !=null)) {
				// start play image			
				final Handler mHandler = new Handler();
				final Runnable mUpdateResults = new Runnable() {
		            public void run() {		            	
		            	Log.d("LOG","Play image item = " + String.valueOf(CURRENT_ITEM) + "/" + String.valueOf(listContent.length));
		            	File imgFile = new  File(DEFAULT_IMAGEPATH + "/" + listContent[CURRENT_ITEM].getName().toString());
		            	if(imgFile.exists()){
		            	    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		            	    imageView.setImageBitmap(myBitmap);
		            	    CURRENT_ITEM++; 
		            	}		            	 
		            	if (listContent.length<=CURRENT_ITEM) {
		            		CURRENT_ITEM=0;
		            	}
		            }
		        };
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						mHandler.post(mUpdateResults);
					}
				}, delay, period);
			} else {
				Toast.makeText(getApplicationContext(),R.string.alert_no_file_in_playlist, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
 
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
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
		TEXT_BACKGROUND = pref.getString("TextColorBackground", "#00000000");
		TEXT_MARQUEE = pref.getString("TextMarquee",getString(R.string.detault_text_marquee));
		// clock
		CLOCK_ENABLE = pref.getBoolean("EnableClock", true);
		CLOCK_COLOR = pref.getString("ClockTextColor", "#FFFFFF");
		CLOCK_BACKGROUND = pref.getString("ClockTextColorBackground", "#00000000");
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// Log.d("LOG", "on activity result");
		if (requestCode == REQUEST_PREFERENCE_CODE) {
			// Log.d("LOG", "get result");
			if (timer != null) {
				timer.cancel();
			}
			// load default and set properties
			loadSharePreference();
			// set appearance
			setAppeareance();
			// load data from external storage
			listContent=loadContent(DEFAULT_DATASTORE);
			// play content
			playContent(DEFAULT_DATASTORE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
