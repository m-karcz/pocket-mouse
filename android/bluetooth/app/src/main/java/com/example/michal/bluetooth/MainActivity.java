package com.example.michal.bluetooth;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private TextView myLabel;
    private Spinner devList;
    private BT bluetooth;
    private Gyro gyro;
    private Mouse mouse;
    private Dialogue dialogue;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GUI elements initialization
        Button mLeftButton= (Button)findViewById(R.id.mouseL);
        Button mRightButton= (Button)findViewById(R.id.mouseR);
        Button lockMove= (Button)findViewById(R.id.lockMove);
        Button openBtn = (Button) findViewById(R.id.open);
        devList= (Spinner)findViewById(R.id.devList);
        myLabel = (TextView)findViewById(R.id.label);

        appBar(R.string.disconnected);

        dialogue = new Dialogue(this);
        gyro = new Gyro();
        bluetooth = new BT();
        bluetooth.updateDevices();
        mouse = new Mouse();

        //action when left mouse button is touched
        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( bluetooth.isConnected() ){switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mouse.leftChanged(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mouse.leftChanged(false);
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
                        mouse.rightChanged(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mouse.rightChanged(false);
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
                        mouse.lockChanged(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mouse.lockChanged(false);
                        return true;
                }}
                return false;
            }
        });

        openBtn.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch( event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        appBar(R.string.connecting);
                        return true;
                    case MotionEvent.ACTION_UP:
                        bluetooth.close();
                        bluetooth.open( devList.getSelectedItem().toString() );
                        if(bluetooth.isConnected())
                        {
                            appBar(R.string.connected);
                        }
                        else
                        {
                            appBar(R.string.disconnected);
                        }
                        return true;
                }
                return false;
            }
        });
    }



    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {//function to determine if BT was enabled by user (wasn't active on startup)
        bluetooth.checkUserPermission(requestCode, resultCode);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {//action to do when position was changed
        gyro.changeHandle(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /*public void openBtnClicked(View view)
    {//called when connect btn is pressed
        bluetooth.close();
        appBar(R.string.connecting);
        bluetooth.open( devList.getSelectedItem().toString() );
        if(bluetooth.isConnected())
        {
            appBar(R.string.connected);
        }
        else
        {
            appBar(R.string.disconnected);
        }

    }*/

    public void closeBtnClicked(View view)
    { //called when disconnect btn is pressed
        bluetooth.close();
    }

    private void appBar(int resId)
    {
        try{
            getSupportActionBar().setTitle(resId );
        }catch(java.lang.NullPointerException e )
        {
            dialogue.exit_app(R.string.error);
        }
    }

    private class Mouse
    {
        private byte[] packet;
        private boolean moveLocked;

        private Mouse()
        {
            packet = new byte[]{0,0,0};
            moveLocked = false;
        }
        private void positionChanged(float deltaX, float deltaY)
        {
           if ( !moveLocked && (bluetooth.isConnected()  || mouse.isBtnPressed()) )
           {
               packet[0] = (byte) deltaX;
               packet[1] = (byte) deltaY;
               bluetooth.send(packet);
           }
        }
        private void leftChanged(boolean state)
        {
            if( state )
            {
                packet[2] = (byte) 1;
            }
            else
            {
                packet[2] = (byte) 0;
            }
        }
        private void rightChanged(boolean state)
        {
            if( state )
            {
                packet[2] = (byte) 2;
            }
            else
            {
                packet[2] = (byte) 0;
            }
        }
        private void lockChanged(boolean state )
        {
            moveLocked = state;
        }
        private boolean isBtnPressed()
        {
            return (0 < packet[2]);
        }
    }

    private class Gyro
    {
        SensorManager sensorManager;
        Sensor gyroscope;

        private Gyro()
        {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
            {
                // success! we have a gyroscope
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
            }
            else
            {
                // fail! we don't have a gyro!
                dialogue.exit_app( R.string.gyro_error );
            }
        }

        private void changeHandle(SensorEvent event)
        {
            float deltaX,deltaY;
            boolean changed = false;
            // get the change of the x,y values of the gyroscope
            if (Math.abs(event.values[0])>0.05) {
                deltaY = -35*event.values[0];
                changed = true;
            }
            else
            {
                deltaY=0;
            }
            if (Math.abs(event.values[2])>0.05) {
                deltaX = -35*event.values[2];
                changed = true;
            }
            else
            {
                deltaX=0;
            }
            if ( changed )
            {
                mouse.positionChanged(deltaX, deltaY);
            }
        }

    }

    private class BT
    {
        private final int REQUEST_ENABLE_BT = 1;
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
                dialogue.exit_app(R.string.BT_device_error);
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

        private void checkUserPermission(int requestCode, int resultCode)
        {
            if(requestCode == REQUEST_ENABLE_BT)
            {
                if(RESULT_CANCELED == resultCode)
                {
                    dialogue.exit_app(R.string.BT_enable_failed);
                }
                else if( RESULT_OK == resultCode)
                {
                    bluetooth.updateDevices();
                }
            }
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
                myLabel.setText(R.string.BT_opened);
                connected=true;
            }catch(IOException e)
            {
                dialogue.toast(R.string.BT_connection_error);
                connected = false;
            }
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
                    appBar(R.string.disconnected);
                } catch (IOException ex) {
                    dialogue.exit_app(R.string.BT_close_error);
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
                dialogue.popup(R.string.send_err);
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

