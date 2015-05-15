/* Copyright (c) 2014 OpenBCI
 * See the file license.txt for copying permission.
 * */

package mobi.foo.openbci.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.BLE.BluetoothHelper;
import com.BLE.RFduinoService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import mobi.foo.openbci.R;
import mobi.foo.openbci.Valuesholder;
import mobi.foo.openbci.utils.Helper;

@SuppressLint("NewApi")
public class ConnectionFragment extends BaseFragment implements LeScanCallback, View.OnClickListener {

    public static final int threshold_danger = 40;
    public static final int threshold_normal = 70;
    private final String TAG = ConnectionFragment.class.getSimpleName();

    // Bluetooth State
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;
    final private static byte[] START = {'b'};
    final private static byte[] STOP = {'s'};

    private int mBluetoothState;
    private boolean mScanStarted;
    private boolean mScanning;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    private RFduinoService mRFduinoService;
    private TextView mEnableBluetoothButton;
    private TextView mScanStatusText;
    private TextView mScanButton;
    private TextView mDeviceInfoText;
    private TextView mConnectionStatusText;
    private TextView mConnectButton;
    private EditText mCommandText;

    private ProgressBar mProgressBar;
    private TextView mReceiving;
    private TextView mViewFileButton;
    private ViewSwitcher switcher;
    /////////////////////////////////////////////////////////////////////////////
    int lastValue = 50;
    private LineChart mChart;
    private Typeface tf;
    ArrayList<Entry> entries = new ArrayList<Entry>();
    TextView valueLabel, finish, resume;
    boolean running = true;
    Valuesholder values = new Valuesholder();
    ////////////////////////////////////////////////////////////////////////////
    // Broadcast receiver to monitor BLE connection
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);

            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }

        }
    };

    // BroadcastReceiver to receive Bluetooth scan intents
    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mScanning = (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            mScanStarted &= mScanning;
            updateUi();
        }
    };

    // BroadcastReceiver to receive data from RFduino intenets
    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent
                        .getByteArrayExtra(RFduinoService.EXTRA_DATA);
                if (data[0] == (byte)111) {
                    replace(new TutorialConnectFragment(), false);
                } else if(running){
                    Helper.addEntry(getActivity(), entries, valueLabel, mChart, data[0], values, true);
                }
            }

        }
    };

    // ServiceConnection to monitor connections with the RFduino
    final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRFduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRFduinoService = ((RFduinoService.LocalBinder) service)
                    .getService();
            if (mRFduinoService.initialize()) {
                if (mRFduinoService.connect(mBluetoothDevice.getAddress())) {
                    upgradeState(STATE_CONNECTING);
                }
            }
        }
    };


    @Override
    public void GUI(Bundle state) {
        switcher = (ViewSwitcher) findViewById(R.id.switcher);
        valueLabel = (TextView) findViewById(R.id.txtleverdra);
        finish = (TextView) findViewById(R.id.txtfinish);
        resume = (TextView) findViewById(R.id.txtresume);
        mChart = (LineChart) findViewById(R.id.chart1);
        mEnableBluetoothButton = (TextView) findViewById(R.id.enableBluetooth);
        Helper.prepareChat(mChart, getActivity());
        Helper.addEntry(getActivity(), entries, valueLabel, mChart, 105, values, false);
mEnableBluetoothButton.setEnabled(true);
        mEnableBluetoothButton.setOnClickListener(this);

         mScanStatusText = (TextView) findViewById(R.id.scanStatus);

if(running)
        Helper.addEntry(getActivity(), entries, valueLabel, mChart, -5, values, false);


        start();
    }
    private  void findButtons(ViewGroup viewGroup) {
        for (int i = 0, N = viewGroup.getChildCount(); i < N; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                findButtons((ViewGroup) child);
            } else {
child.setOnClickListener(ConnectionFragment.this);
                child.setEnabled(true);
            }
        }
    }
    @Override
    public int getContentView(Bundle state) {
        return R.layout.main_graph_connection;
    }


    @Override
    public void onStop() {
        super.onStop();

        mBluetoothAdapter.stopLeScan(this);
        stop();
    }

    private void stop() {

        getActivity().unregisterReceiver(scanModeReceiver);
        getActivity().unregisterReceiver(bluetoothStateReceiver);
        getActivity().unregisterReceiver(rfduinoReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void upgradeState(int newState) {
        if (newState > mBluetoothState) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < mBluetoothState) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        mBluetoothState = newState;
        updateUi();
    }

    private void updateUi() {
        // Enable Bluetooth
        boolean on = mBluetoothState > STATE_BLUETOOTH_OFF;
        mEnableBluetoothButton.setText(on ? "Disable Bluetooth"

                : "Enable Bluetooth");
        mScanButton.setEnabled(on);

        // Scan
        if (mScanStarted && mScanning) {
            mScanStatusText.setText("Scanning...");
            mScanButton.setText("Stop Scanning");
            mScanButton.setEnabled(true);
        } else if (mScanStarted) {
            mScanStatusText.setText("Scan started...");
            mScanButton.setEnabled(false);
        } else {
            mScanStatusText.setText("");
            mScanButton.setText("Scan");
            mScanButton.setEnabled(on);
        }

        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (mBluetoothState == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (mBluetoothState == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        mConnectionStatusText.setText(connectionText);
        mConnectButton.setEnabled(mBluetoothDevice != null
                && mBluetoothState == STATE_DISCONNECTED);
        mCommandText.setEnabled(connected);
        findViewById(R.id.sendButton).setEnabled(connected);
        findViewById(R.id.startButton).setEnabled(connected);
       findViewById(R.id.stopButton).setEnabled(connected);
    }

    @Override
    public void onLeScan(BluetoothDevice device, final int rssi,
                         final byte[] scanRecord) {
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothDevice = device;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceInfoText.setText(BluetoothHelper.getDeviceInfoText(
                        mBluetoothDevice, rssi, scanRecord));
                updateUi();
            }
        });
    }

    public void start() {
        super.onStart();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanStatusText = (TextView) findViewById(R.id.scanStatus);
        mDeviceInfoText = (TextView) findViewById(R.id.deviceInfo);
        mConnectionStatusText = (TextView) findViewById(R.id.connectionStatus);
        mCommandText = (EditText) findViewById(R.id.sendValue);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mReceiving = (TextView) findViewById(R.id.receivingLabel);
        mViewFileButton = (TextView) findViewById(R.id.viewFileButton);
        mScanButton = (TextView) findViewById(R.id.scan);
        mConnectButton = (TextView) findViewById(R.id.connect);

findButtons((ViewGroup) getView());

        getActivity().registerReceiver(scanModeReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        getActivity().registerReceiver(bluetoothStateReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED));
        getActivity().registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());
        updateState(mBluetoothAdapter.isEnabled() ? STATE_DISCONNECTED
                : STATE_BLUETOOTH_OFF);
        finish.setOnClickListener(this);
        resume.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enableBluetooth:

                if (mBluetoothState == STATE_BLUETOOTH_OFF) {
                    Builder confirmOnBluetooth = new Builder(
                            getActivity());
                    confirmOnBluetooth.setTitle("Turn on Bluetooth?");
                    confirmOnBluetooth.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mEnableBluetoothButton.setText(mBluetoothAdapter
                                            .enable() ? "Enabling Bluetooth"
                                            : "Enable Failed!");
                                }
                            });
                    confirmOnBluetooth.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });
                    confirmOnBluetooth.show();
                } else {

                    Builder confirmOffBluetooth = new Builder(
                            getActivity());
                    String title = mBluetoothState > 2 ? "Bluetooth is being accessed by a device. Sure you want to turn it off?"
                            : "Turn Off Bluetooth?";
                    confirmOffBluetooth.setTitle(title);
                    confirmOffBluetooth.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mEnableBluetoothButton.setText(mBluetoothAdapter
                                            .disable() ? "Disabling Bluetooth"
                                            : "Disable Failed!");
                                }
                            });
                    confirmOffBluetooth.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            });
                    confirmOffBluetooth.show();
                }


                break;
            case R.id.scan:
                mBluetoothAdapter.startLeScan(
                        new UUID[]{RFduinoService.UUID_SERVICE},
                        ConnectionFragment.this);
                break;
            case R.id.stopButton:
                mReceiving.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mRFduinoService.send(STOP);
                mViewFileButton.setEnabled(true);
            case R.id.connectionStatus:
                break;
            case R.id.txtconnection:
                switcher.setDisplayedChild(0);
                break;
            case R.id.graph:
                switcher.setDisplayedChild(1);
                break;
            case R.id.startButton:
                mRFduinoService.send(START);
                mReceiving.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case R.id.connect:
                 v.setEnabled(false);
                mConnectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(getActivity(),
                        RFduinoService.class);
                getActivity().bindService(rfduinoIntent, rfduinoServiceConnection,
                        Activity.BIND_AUTO_CREATE);

                break;
            case R.id.sendButton:
                                mRFduinoService.send(mCommandText.getText().toString()
                        .getBytes());
                break;
            case R.id.txtfinish:
                replace(new FinalResultsFragment(values), false);
                break;
            case R.id.txtresume:
                     if (running) {
                    resume.setText("Resume");
                    running = false;
                } else {
                    resume.setText("Pause");
                    running=true;
                }
                break;
            default:
                break;

        }
    }
}
