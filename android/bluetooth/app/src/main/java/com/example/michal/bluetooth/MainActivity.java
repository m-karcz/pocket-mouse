package com.example.michal.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    boolean isOpen=false,isConnected=false;
    boolean NeedToSend=false,allowToSend=true;
    volatile boolean stopWorker;
    private float deltaX,deltaY;
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private byte[] packet = {0,0,0};
    private Spinner devList;
    Set<BluetoothDevice> pairedDevices;
    private final int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button)findViewById(R.id.open);
        Button closeButton = (Button)findViewById(R.id.close);
        Button mLbutton= (Button)findViewById(R.id.mouseL);
        Button mRbutton= (Button)findViewById(R.id.mouseR);
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
            myLabel.setText("Gyroscope error");
        }
        //Picker initialize
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            //debugLabel.setText  ("Bluetooth is not supported on this device.");
            //TODO - add a popup to close the app
        }
        else
        {
            //debugLabel.setText("BT ok");
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
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            devList.setAdapter(dataAdapter);
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add("You don't have any paired devices");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
            devList.setAdapter(dataAdapter);

        }
        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try
                {
                    //findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(isConnected|| isOpen){
                try
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }}
        });
        //Mouse Left button
        mLbutton.setOnTouchListener(new View.OnTouchListener() {
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
        mRbutton.setOnTouchListener(new View.OnTouchListener() {
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
    public void onSensorChanged(SensorEvent event)
    {
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
                myLabel.setText("Data Sent");
            }
            catch (IOException ex)
            {
                myLabel.setText("Sending error!");
                isOpen=false;
            }
        }
    }

    /*public void addItemsOnSpinner(String newDev)
    {
        devList = (Spinner) findViewById(R.id.devList);
        List<String> list = new ArrayList<String>();
        list.add(newDev);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devList.setAdapter(dataAdapter);
    }*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

   /* void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                //if(device.getName().equals("MARIUSZ-LAPTOK"))
                if(device.getName().equals("DESKTOP-D5L3DLV"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
        //addItemsOnSpinner("Mariusz-LAPTOK");
        addItemsOnSpinner("DESKTOP-D5L3DLV");
        isConnected=true;
    }*/

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

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
        isOpen=true;
        isConnected=true;

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        isOpen=false;
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
        isConnected=false;
    }
}

  //  void sendData() throws IOException
  //  {
  //      String msg ="aa";// myTextbox.getText().toString();
  //      msg += "\n";
  //      mmOutputStream.write(new byte[]{(byte) deltaX, (byte) deltaY,0});
  //      myLabel.setText("Data Sent");
  //  }