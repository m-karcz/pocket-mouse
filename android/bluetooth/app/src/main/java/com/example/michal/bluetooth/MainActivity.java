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
    TextView myLabel;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    boolean isOpen=false,isConnected=false;
    boolean NeedToSend=false,allowToSend=true;
    volatile boolean stopWorker;



    private byte[] packet = {0,0,0};
    private Spinner devList;
    Set<BluetoothDevice> pairedDevices;
    private final int REQUEST_ENABLE_BT = 1;

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
            myLabel.setText(R.string.gyro_error);
        }
        //Picker initialize
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            exit_app();
        }

        if( !mBluetoothAdapter.isEnabled() )
        {
            Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
        }
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if( pairedDevices.size() > 0 )
        {
            List<String> list = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices)
            {
                list.add( device.getName() );
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            devList.setAdapter(dataAdapter);
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add("You don't have any paired devices");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,list);
            devList.setAdapter(dataAdapter);

        }


        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isConnected || isOpen){switch(event.getAction()) {
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
                if(isConnected || isOpen){switch(event.getAction()) {
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
                if(isConnected || isOpen){switch(event.getAction()) {
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
    {//function to determine if BT was enabled
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(RESULT_CANCELED == resultCode)
            {
                exit_app();
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
        if (allowToSend && ((isOpen && NeedToSend) || packet[2]!=0))
        {
            try
            {
                packet[0]=(byte) deltaX;
                packet[1]=(byte) deltaY;
                mmOutputStream.write(packet);
                myLabel.setText(R.string.data_sent);
            }
            catch (IOException ex)
            {
                myLabel.setText(R.string.send_err);
                isOpen=false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void exit_app()
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Close")
                .setMessage("Close the app")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .show();
    }

    public void openBtnClicked(View view)
    {
        try
        {
            openBT();
        }
        catch (IOException ex)
        {
            exit_app();
        }
    }

    public void closeBtnClicked(View view)
    {
        if(isConnected || isOpen)
        {
            try {
                closeBT();
            } catch (IOException ex) {
                exit_app();
            }
        }
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        String devName = devList.getSelectedItem().toString();
        for( BluetoothDevice dev : pairedDevices )
        {
            if( devName.equals(dev.getName()) )
            {
                mmDevice =  dev;
            }
        }
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();



        myLabel.setText(R.string.BT_opened);
        isOpen=true;
        isConnected=true;

    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        isOpen=false;
        mmSocket.close();
        myLabel.setText(R.string.BT_closed);
        isConnected=false;
    }
}

