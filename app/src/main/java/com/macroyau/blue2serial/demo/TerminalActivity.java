package com.macroyau.blue2serial.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

/**
 * This is an example Bluetooth terminal application built using the Blue2Serial library.
 *
 * @author Macro Yau
 */
public class TerminalActivity extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;

    private ScrollView svTerminal;
    private TextView tvTerminal;
//    private EditText etSend;

    private RadioGroup radioGroup;
    private RadioButton rbBCGood, rbBCError;
    private Button btSend;

    private MenuItem actionConnect, actionDisconnect;

    private boolean crlf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
//                if(checkedId == R.id.BCgood) {
//                    Toast.makeText(getApplicationContext(), "choice: Good",
//                            Toast.LENGTH_SHORT).show();
//                } else if(checkedId == R.id.BCerror) {
//                    Toast.makeText(getApplicationContext(), "choice: Error",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//
//        });
        rbBCGood = (RadioButton) findViewById(R.id.BCgood);
        rbBCError = (RadioButton) findViewById(R.id.BCerror);

        // Find UI views and set listeners
        svTerminal = (ScrollView) findViewById(R.id.terminal);
        tvTerminal = (TextView) findViewById(R.id.tv_terminal);
/*
        etSend = (EditText) findViewById(R.id.et_send);
        etSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String send = etSend.getText().toString().trim();
//                    send = Integer.toString(0x16,16)+"M"+Integer.toString(0x0D,16)+"REVINF."+Integer.toString(0x0D,16);
                    send = "\026"+"M"+"\015"+"REVINF."+"\015";

                    if (send.length() > 0) {
//                        bluetoothSerial.write(send, crlf);
                        bluetoothSerial.write(send);
                        etSend.setText("");
                    }
                }
                return false;
            }
        });
*/
        btSend = (Button)findViewById(R.id.chooseBtn);
        btSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String send;
                // find which radioButton is checked by id
                if(selectedId == rbBCGood.getId()) {
//                    Toast.makeText(getApplicationContext(), "choice: Good",
//                            Toast.LENGTH_SHORT).show();
                    send = "\0337,";
                    bluetoothSerial.write(send);


                } else if(selectedId == rbBCError.getId()) {
//                    Toast.makeText(getApplicationContext(), "choice: Error",
//                            Toast.LENGTH_SHORT).show();
                    send = "\0338,";
                    bluetoothSerial.write(send);
                }
            }
        });

        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terminal, menu);

        actionConnect = menu.findItem(R.id.action_connect);
        actionDisconnect = menu.findItem(R.id.action_disconnect);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            showDeviceListDialog();
            return true;
        } else if (id == R.id.action_disconnect) {
            // Habilito Host ACK
            String send;
            send = "\026"+"M"+"\015"+"HSTACK0."+"\015";
            bluetoothSerial.write(send);
            bluetoothSerial.stop();
            return true;
        } else if (id == R.id.action_crlf) {
            crlf = !item.isChecked();
            item.setChecked(crlf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        if (bluetoothSerial == null)
            return;

        // Show or hide the "Connect" and "Disconnect" buttons on the app bar
        if (bluetoothSerial.isConnected()) {
            if (actionConnect != null)
                actionConnect.setVisible(false);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(true);
        } else {
            if (actionConnect != null)
                actionConnect.setVisible(true);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = getString(R.string.status_connecting);
                break;
            case BluetoothSerial.STATE_CONNECTED:
                String device=bluetoothSerial.getConnectedDeviceName();
                subtitle = getString(R.string.status_connected, bluetoothSerial.getConnectedDeviceName());
                subtitle = "Connected to "+device.substring(18);
                break;
            default:
                subtitle = getString(R.string.status_disconnected);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle(R.string.paired_devices);
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    /* Implementation of BluetoothSerialListener */

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.no_bluetooth)
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
        String send;
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
        String send;
        // Habilito Host ACK
        send = "\026"+"M"+"\015"+"HSTACK1."+"\015";
        bluetoothSerial.write(send);
//        send = "\026"+"M"+"\015"+"REVINF."+"\015";
//        bluetoothSerial.write(send);
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        // Print the incoming message on the terminal screen
        tvTerminal.append(" > " + message+ "\n");
        svTerminal.post(scrollTerminalToBottom);
    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        // Print the outgoing message on the terminal screen
//        tvTerminal.append(getString(R.string.terminal_message_template,
//                bluetoothSerial.getLocalAdapterName(),
//                message));
//        svTerminal.post(scrollTerminalToBottom);
    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

    /* End of the implementation of listeners */

    private final Runnable scrollTerminalToBottom = new Runnable() {
        @Override
        public void run() {
            // Scroll the terminal screen to the bottom
            svTerminal.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };

}
