package it.angelic.soulissclient;

import static it.angelic.soulissclient.Constants.TAG;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.net.UDPHelper;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ManualUDPTestActivity extends Activity {
	private SoulissPreferenceHelper opzioni;
	private SoulissDBHelper datasource;
	private Button refreshButton;
	private Button stateRequestButton;
	private Button typreqButton;
	private Button healthButton;
	private Button GoButt;
	private Handler timeoutHandler;
	private TextView errorText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// tema
		opzioni = SoulissClient.getOpzioni();
		if (opzioni.isLightThemeSelected())
			setTheme(R.style.LightThemeSelector);
		else
			setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_manualudptest);
		setTitle(getString(R.string.app_name) + " - " + getString(R.string.menu_test));

		// DB
		datasource = new SoulissDBHelper(this);
		datasource.open();
		timeoutHandler = new Handler();
		// getWindow().setFormat(PixelFormat.RGBA_8888);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		refreshButton = (Button) findViewById(R.id.refreshButton);
		stateRequestButton = (Button) findViewById(R.id.resetButton);
		typreqButton = (Button) findViewById(R.id.typreqButton);
		healthButton = (Button) findViewById(R.id.healthreqButton);
		GoButt = (Button) findViewById(R.id.buttonForce);
		errorText = (TextView) findViewById(R.id.textOutputError);

		final Spinner idspinner = (Spinner) findViewById(R.id.spinner1);
		final Spinner slotspinner = (Spinner) findViewById(R.id.spinner2);
		final EditText editCmd = (EditText) findViewById(R.id.editText1);

		SoulissClient.setBackground((LinearLayout) findViewById(R.id.container), getWindowManager());

		/**
		 * Aggiorna le tabelle dei tipici
		 */
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						String cached = opzioni.getAndSetCachedAddress();
						final StringBuilder ret = new StringBuilder();
						if (cached == null) {
							cached = opzioni.isSoulissPublicIpConfigured() ? opzioni.getIPPreferencePublic() : opzioni
									.getPrefIPAddress();
						}
						try {
							ret.append(UDPHelper.ping(opzioni.getPrefIPAddress(), cached, opzioni.getUserIndex(), opzioni.getNodeIndex()).getHostAddress());
						} catch (Exception e) {
							Log.e(Constants.TAG, "UDP test error:"+e.getMessage(),e);
						}

						refreshButton.post(new Runnable() {
							public void run() {
								errorText.setVisibility(View.GONE);
								refreshButton.setEnabled(true);
								// svuota la tabella e mette feedback
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								txt.setText(Constants.hourFormat.format(new Date()) + ": Ping sent to "+ret.toString());
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Manda RESET a Souliss
		 */
		stateRequestButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.pollRequest(opzioni, opzioni.getCustomPref().getInt("numNodi", 1), 0);

						stateRequestButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));

								txt.setText(Constants.hourFormat.format(new Date()) + ": poll request sent");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Manda TYPICAL Request a Souliss
		 */
		typreqButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				final int numof = opzioni.getCustomPref().getInt("numNodi", 1);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.typicalRequest(opzioni, numof, 0);

						stateRequestButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));

								txt.setText(Constants.hourFormat.format(new Date())
										+ ": typical Request sent, 1 node starting from 0");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});

		/**
		 * Manda HEALTH Request a Souliss
		 */
		healthButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);

				Thread t = new Thread() {
					public void run() {
						UDPHelper.healthRequest(opzioni, 1, 0);
						// final LinearLayout stat = (LinearLayout)
						// findViewById(R.id.linearLayoutOutput);

						healthButton.post(new Runnable() {
							public void run() {
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));
								errorText.setVisibility(View.GONE);
								txt.setText(Constants.hourFormat.format(new Date())
										+ ": Health Request sent, 1 node starting from 0");
								errorText.setVisibility(View.GONE);
							}
						});
					}
				};

				t.start();

			}
		});
		/**
		 * Invia un comando
		 */
		GoButt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// nascondi tastiera
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editCmd.getWindowToken(), 0);
				GoButt.setEnabled(false);
				if (idspinner.getSelectedItem().toString().length() < 1
						|| slotspinner.getSelectedItem().toString().length() < 1
						|| editCmd.getText().toString().length() < 1) {
					Toast it2 = Toast.makeText(ManualUDPTestActivity.this,
							getResources().getString(R.string.inserire_almeno), Toast.LENGTH_SHORT);
					it2.show();
					GoButt.setEnabled(true);
					return;
				}

				Thread t = new Thread() {
					public void run() {
						// ISSUE SOULISS UDP command
						final String cmdOutput = UDPHelper.issueSoulissCommand(idspinner.getSelectedItem().toString(),
								slotspinner.getSelectedItem().toString(), opzioni,  editCmd
										.getText().toString());
						//final LinearLayout OutputLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutOutput);

						GoButt.post(new Runnable() {
							public void run() {
								// Refresh Output area
								GoButt.setEnabled(true);
								TextView txt = (TextView) findViewById(R.id.textOutput1);
								if (opzioni.isLightThemeSelected())
									txt.setTextColor(getResources().getColor(R.color.black));
								errorText.setVisibility(View.GONE);
								txt.setText(Constants.hourFormat.format(new Date()) + "Command sent to: "
										+ opzioni.getPrefIPAddress() + " - " + cmdOutput);
							}
						});
					}
				};

				t.start();

			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		opzioni = SoulissClient.getOpzioni();
		// check se IP non settato
		if (!opzioni.isSoulissIpConfigured() && !opzioni.isSoulissReachable()) {
			refreshButton.setEnabled(false);
			stateRequestButton.setEnabled(false);
			GoButt.setEnabled(false);
			AlertDialog.Builder alert = AlertDialogHelper.sysNotInitedDialog(this);
			alert.show();
		}
		// check rete disponibile
		else if (!opzioni.isSoulissReachable()) {
			Toast it = Toast.makeText(ManualUDPTestActivity.this, getString(R.string.souliss_unavailable),
					Toast.LENGTH_LONG);
			it.show();
		} else {
			refreshButton.setEnabled(true);
			stateRequestButton.setEnabled(true);
			GoButt.setEnabled(true);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manual_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.Opzioni:
			Intent settingsActivity = new Intent(getBaseContext(), PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;

		case R.id.Esci:
			super.finish();
		}

		return super.onOptionsItemSelected(item);
	}

	// Aggiorna il feedback
	private BroadcastReceiver macacoRawDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
				Log.d(TAG, "Broadcast RAW DATA: " + vers);
				timeoutHandler.removeCallbacks(timeExpired);
				TextView ito = (TextView) findViewById(R.id.textOutputError);
				ito.setVisibility(View.GONE);
				// final LinearLayout OutputLinearLayout = (LinearLayout)
				// findViewById(R.id.linearLayoutOutput);
				final TextView OutputLinearT = (TextView) findViewById(R.id.textOutput1);
				// OutputLinearLayout.removeViewAt(0);
				// TextView ito = new TextView(getApplicationContext());

				StringBuilder dump = new StringBuilder();
				for (int ig = 0; ig < vers.size(); ig++) {
					// 0xFF & buf[index]
					dump.append("0x" + Long.toHexString(vers.get(ig)) + " ");
					// dump.append(":"+packet.getData()[ig]);
				}
				if (opzioni.isLightThemeSelected())
					OutputLinearT.setTextColor(getResources().getColor(R.color.black));
				// aggiorna feedback
				String faker = OutputLinearT.getText().toString();
				OutputLinearT.setText(faker
						+ "\n"
						+ Html.fromHtml(Constants.hourFormat.format(new Date())
								+ ": Reply <font color=\"#99CC00\">received</font> - " + dump.toString()));

				// OutputLinearLayout.addView(ito);
				GoButt.setEnabled(true);

			} else {
				Log.e(TAG, "EMPTY response!!");
			}
		}
	};
	Runnable timeExpired = new Runnable() {

		@Override
		public void run() {
			// final LinearLayout OutputLinearLayout = (LinearLayout)
			// findViewById(R.id.linearLayoutOutput);

			Log.e(TAG, "TIMEOUT!!!");
			if (opzioni.isLightThemeSelected())
				errorText.setTextColor(getResources().getColor(R.color.black));

			errorText.setText(Html.fromHtml(Constants.hourFormat.format(new Date())
					+ ": Command timeout <b><font color=\"#FF4444\">expired</font></b>, no reply received "));
			// OutputLinearLayout.addView(ito);
			// Toast.makeText(LauncherActivity.this, "Request failed" +
			// provider, Toast.LENGTH_SHORT).show();
			errorText.setVisibility(View.VISIBLE);
		}
	};
	// meccanismo per network detection
	private BroadcastReceiver timeoutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w(TAG, "Posting timeout from " + intent.toString());
			Bundle extras = intent.getExtras();
			int delay = extras.getInt("REQUEST_TIMEOUT_MSEC");
			timeoutHandler.postDelayed(timeExpired, delay);
		}
	};

	@Override
	protected void onResume() {
		// IDEM, serve solo per reporting
		IntentFilter filtere = new IntentFilter();
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		registerReceiver(macacoRawDataReceiver, filtere);

		// this is only used for refresh UI
		IntentFilter filtera = new IntentFilter();
		filtera.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_TIMEOUT);
		registerReceiver(timeoutReceiver, filtera);

		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		datasource.close();
		unregisterReceiver(timeoutReceiver);
		unregisterReceiver(macacoRawDataReceiver);
		super.onPause();
	}
}