package com.example.michal.bluetooth;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class MainActivity extends Activity implements SensorEventListener
{
    private TextView myLabel;
    boolean NeedToSend=false,allowToSend=true;
    private byte[] packet = {0,0,0};
    private Spinner devList;
    private final int REQUEST_ENABLE_BT = 1;
    private BT bluetooth;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SensorManager sensorManager;
        Sensor gyroscope;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mLeftButton= (Button)findViewById(R.id.mouseL);
        Button mRightButton= (Button)findViewById(R.id.mouseR);
        Button lockMove= (Button)findViewById(R.id.lockMove);
        devList= (Spinner)findViewById(R.id.devList);
        myLabel = (TextView)findViewById(R.id.label);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
        {
            // success! we have a gyroscope
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        } 
        else 
        {
            // fail! we don't have a gyro!
            exit_app( R.string.gyro_error );
        }

        bluetooth = new BT();
        bluetooth.updateDevices();


        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( bluetooth.isConnected() ){switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        packet[2]=(byte) 1;
                        return true;
                    case MotionEvent.ACTION_UP:
                        packet[2]=(byte) 0;
                        return true;
                }}
                return false;
            }
        });
        //Mouse Right button
        mRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( bluetooth.isConnected() ){switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        packet[2]=(byte) 2;
                        return true;
                    case MotionEvent.ACTION_UP:
                        packet[2]=(byte) 0;
                        return true;
                }}
                return false;
            }
        });
        //Mouse movement locker
        lockMove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( bluetooth.isConnected() ){switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        allowToSend=false;
                        return true;
                    case MotionEvent.ACTION_UP:
                        allowToSend=true;
                        return true;
                }}
                return false;
            }
        });
    }



    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {//function to determine if BT was enabled by user
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(RESULT_CANCELED == resultCode)
            {
                exit_app(R.string.BT_enable_failed);
            }
            else if( RESULT_OK == resultCode)
            {
                bluetooth.updateDevices();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float deltaX,deltaY;
        // get the change of the x,y,z values of the gyroscope
        if (Math.abs(event.values[0])>0.05) {
            deltaY = -35*event.values[0];
            NeedToSend=true;
        }
        else
        {
            deltaY=0;
            NeedToSend=false;
        }
        if (Math.abs(event.values[2])>0.05) {
            deltaX = -35*event.values[2];
            NeedToSend=true;
        }
        else
        {
            NeedToSend=false;
            deltaX=0;
        }
        if (allowToSend && ((bluetooth.isConnected() && NeedToSend) || packet[2]!=0))
        {
            packet[0]=(byte) deltaX;
            packet[1]=(byte) deltaY;
            bluetooth.send(packet);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void exit_app(int resId)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Error!")
                .setMessage(getString(resId)+", press OK to close the application.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .show();
    }

    public void popup(int resId)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning!")
                .setMessage(getString(resId))
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    public void openBtnClicked(View view)
    {
        bluetooth.close();
        bluetooth.open( devList.getSelectedItem().toString() );

    }

    public void closeBtnClicked(View view)
    {
        bluetooth.close();
    }

    private class BT
    {
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private OutputStream mmOutputStream;
        private InputStream mmInputStream;
        private Set<BluetoothDevice> pairedDevices;
        private boolean connected;

        private BT()
        {
            connected = false;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null)
            {
                exit_app(R.string.BT_device_error);
            }

            if( !mBluetoothAdapter.isEnabled() )
            {
                Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
            }
        }
        private boolean isConnected()
        {
            return connected;
        }
        private void open(String devName)
        {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
            for( BluetoothDevice dev : pairedDevices )
            {
                if( devName.equals(dev.getName()) )
                {
                    mmDevice =  dev;
                }
            }
            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                mmSocket.connect();
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();
            }catch(IOException e)
            {
                popup(R.string.BT_connection_error);
                connected = false;
            }



            myLabel.setText(R.string.BT_opened);
            connected=true;
        }

        private void close()
        {
            if(connected)
            {
                try {
                    mmOutputStream.close();
                    mmInputStream.close();
                    mmSocket.close();
                    myLabel.setText(R.string.BT_closed);
                    connected=false;
                } catch (IOException ex) {
                    exit_app(R.string.BT_close_error);
                }
            }

        }

        private void send(byte[] packet)
        {
            try
            {
                mmOutputStream.write(packet);
                myLabel.setText(R.string.data_sent);
            }
            catch (IOException ex)
            {
                popup(R.string.send_err);
                connected=false;
            }

        }

        private void updateDevices()
        {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if( pairedDevices.size() > 0 )
            {
                List<String> list = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices)
                {
                    list.add( device.getName() );
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                devList.setAdapter(dataAdapter);
            }
            else
            {
                List<String> list = new ArrayList<>();
                list.add("You don't have any paired devices");
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_item,list);
                devList.setAdapter(dataAdapter);
            }
        }


    }


}

