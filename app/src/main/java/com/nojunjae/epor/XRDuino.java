package com.nojunjae.epor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static android.app.Activity.RESULT_OK;

public class XRDuino implements RejectedExecutionHandler {

    private final int CIRCLED_QUEUE_SIZE = 100000;
    private final String RECENT_DEVICE = "recent_device";
    private final int REQUEST_SELECT_DEVICE = 0;
    private int RGBon_off_flag = 0;
    // BCam//
    private int MaxBuffer = 5000;
    private int MaxFIFO = 100000;
    private byte[] m_pBuf = new byte[MaxFIFO];
    private int frameCount = 0;
    private int m_receiveMode = 0;
    private int addr = 0;
    private int ImageSize = 0;
    private byte[] header = new byte[3];
    /*private byte[] adcValue = new byte[6];
    private byte[] RxValue = new byte[3];*/
    private int ServoHead = 90;
    private int ServoArm1 = 90;
    private int ServoArm2 = 90;
    private byte[] _CQ_Array = new byte[CIRCLED_QUEUE_SIZE];
    private int _s_array_ind = -1;
    private int _e_array_ind = 0;
    private byte[] _FIFO_Array = new byte[CIRCLED_QUEUE_SIZE];
    private int _s_fifo_ind = -1;
    private int _e_fifo_ind = 0;
    private int servodd = 50;
    private int OFFSET = 0;
    private Context mContext;
    private ViewGroup mLayout;
    // byte DValue = 0x00;
    private LayoutParams params;
    private Timer mTimer;
    private Handler handler;
    private int mValue = 0;

    private boolean isDance = true;
    private byte[] CMDbuffer = new byte[10];
    private BluetoothAdapter mBluetooth;
    private ConnectedTask mConnectedTask;
    private ExecutorService mExec;
    private ReentrantLock mLock = new ReentrantLock();
    private Activity activity;
    private byte MOTOR_SPEED = 0;
    private byte SEND_TXDATA = 1;
    private byte SERVO_ANGLE = 3;
    private byte ANALOG_WRITE = 4;
    private byte DIGITAL_WRITE = 5;
    private byte RGB_WRITE = 6;
    private byte LCD_WRITE = 7;

    private XRDuino(Activity activity) {
        this.activity = activity;
        mExec = Executors.newCachedThreadPool();
        ((ThreadPoolExecutor) mExec).setRejectedExecutionHandler(this);
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        // Initialize Global Array
        header[0] = (byte) 'R';
        header[1] = (byte) 'X';
        header[2] = (byte) '=';
    }

    public static XRDuino getInstance(Activity activity) {
        return new XRDuino(activity);
    }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // noop.
    }

    public void consumeRequestDeviceSelect(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        saveAsDefault(device);
        onDeviceSelected(device);
    }

    public void consumeRequestDeviceSelect(BluetoothDevice device) {
        saveAsDefault(device);
        onDeviceSelected(device);
    }

    private BluetoothDevice loadDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String addr = prefs.getString(RECENT_DEVICE, null);
        if (addr == null) {
            return null;
        }
        return mBluetooth.getRemoteDevice(addr);
    }

    private void onDeviceSelected(BluetoothDevice device) {
        mLock.lock();
        try {
            if (mConnectedTask != null) {
                mConnectedTask.cancel();
                mConnectedTask = null;
            }
        } finally {
            mLock.unlock();
        }
        // dumpMessage("connecting");
        ConnectTask task = new ConnectTask(device, Constants.SERIAL_PORT_PROFILE);
        Cancelable canceller = new CancellingTask(mExec, task, 10, TimeUnit.SECONDS);
        mExec.execute(canceller);
    }

    private void saveAsDefault(BluetoothDevice device) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Editor editor = prefs.edit();
        String address = device.getAddress();
        editor.putString(RECENT_DEVICE, address);
        editor.apply();
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.KOREA);
        return dateFormat.format(date) + ".jpg";
    }

    private void SendXBOTCommand(byte cmd, byte d0, byte d1, byte d2, byte d3, byte d4) {
        CMDbuffer[0] = (byte) 'X';
        CMDbuffer[1] = (byte) 'R';
        CMDbuffer[2] = cmd;
        CMDbuffer[3] = d0;
        CMDbuffer[4] = d1;
        CMDbuffer[5] = d2;
        CMDbuffer[6] = d3;
        CMDbuffer[7] = d4;
        CMDbuffer[8] = (byte) 'S';
    }

    public void setMotorSpeed(int speed1, int speed2) {
        if (mConnectedTask != null) {
            SendXBOTCommand(
                    MOTOR_SPEED,
                    (byte) ((speed1 & 0xFF00) >> 8),
                    (byte) (speed1 & 0xFF),
                    (byte) ((speed2 & 0xFF00) >> 8),
                    (byte) (speed2 & 0xFF),
                    (byte) 0);
            mConnectedTask.SendData(CMDbuffer);
        }
    }

    public void setRGBLed(int rColor, int gColor, int bColor) {
        if (mConnectedTask != null) {
            //	sleep(300);
            SendXBOTCommand(
                    RGB_WRITE,
                    (byte) (255),
                    (byte) (rColor),
                    (byte) (gColor),
                    (byte) (bColor),
                    (byte) 0);
            mConnectedTask.SendData(CMDbuffer);
        }
    }

    public void setServoAngle(int angle1, int angle2, int angle3) {
        if (mConnectedTask != null) {

            SendXBOTCommand(
                    SERVO_ANGLE,
                    (byte) (angle1),
                    (byte) (angle2),
                    (byte) (angle3),
                    (byte) 0,
                    (byte) 0);
            mConnectedTask.SendData(CMDbuffer);
        }
    }

    public void shutdownNow() {
        mExec.shutdownNow();
    }

    private int ByteIndexOf(byte[] searched, byte[] find, int end) {
        // Do standard error checking here.
        boolean matched;
        for (int index = 0; index <= end - find.length; ++index) {
            // Assume the values matched.
            matched = true;
            // Search in the values to be found.
            for (int subIndex = 0; subIndex < find.length; ++subIndex) {
                // Check the value in the searched array vs the value
                // in the find array.
                if (find[subIndex] != searched[index + subIndex]) {
                    // The values did not match.
                    matched = false;
                    // Break out of the loop.
                    break;
                }
            }
            // If the values matched, return the index.
            if (matched) {
                // Return the index.
                return index;
            }
        }
        // None of the values matched, return -1.
        return -1;
    }

    private byte[] BlockCopy(byte[] src, int start_ind, int length) {
        byte[] res = new byte[length];
        System.arraycopy(src, start_ind, res, 0, length);
        return res;
    }

    private void CQ_AddBytes(byte[] bytes, int count) {
        if (_s_array_ind < 0) _s_array_ind = 0;
        for (int i = 0; i < count; i++) {
            _CQ_Array[_e_array_ind] = bytes[i];
            _e_array_ind++;
            if (_e_array_ind >= CIRCLED_QUEUE_SIZE) _e_array_ind = 0;
        }
    }

    private int CQ_GetLength() {
        if (_s_array_ind < 0 || _e_array_ind < 0) return 0;
        else if (_e_array_ind >= _s_array_ind) return (_e_array_ind - _s_array_ind);
        else {
            int len1 = CIRCLED_QUEUE_SIZE - _s_array_ind;
            int len2 = _e_array_ind;
            return len1 + len2;
        }
    }

    private byte[] CQ_GetData(int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = _CQ_Array[_s_array_ind];
            _s_array_ind++;
            if (_s_array_ind >= CIRCLED_QUEUE_SIZE) _s_array_ind = 0;
        }
        return res;
    }

    private void FIFO_AddBytes(byte[] bytes, int count) {
        if (_s_fifo_ind < 0) _s_fifo_ind = 0;
        for (int i = 0; i < count; i++) {
            _FIFO_Array[_e_fifo_ind] = bytes[i];
            _e_fifo_ind++;
            if (_e_fifo_ind >= CIRCLED_QUEUE_SIZE) _e_fifo_ind = 0;
        }
    }

    private int FIFO_GetLength() {
        if (_s_fifo_ind < 0 || _e_fifo_ind < 0) return 0;
        else if (_e_fifo_ind >= _s_fifo_ind) return (_e_fifo_ind - _s_fifo_ind);
        else {
            int len1 = CIRCLED_QUEUE_SIZE - _s_fifo_ind;
            int len2 = _e_fifo_ind;
            return len1 + len2;
        }
    }

    private byte[] FIFO_GetData(int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = _FIFO_Array[_s_fifo_ind];
            _s_fifo_ind++;
            if (_s_fifo_ind >= CIRCLED_QUEUE_SIZE) _s_fifo_ind = 0;
        }
        return res;
    }

    private void serialPort_DataReceived(byte[] recv_buff, int recv_count) {
        try {
            CQ_AddBytes(recv_buff, recv_count);
            int fSize;
            if (m_receiveMode == 0) {
                if (CQ_GetLength() < 50) return;
                fSize = CQ_GetLength();
                byte[] buff = CQ_GetData(fSize);
                int index = ByteIndexOf(buff, header, fSize);
                if (index != -1) {
                    ImageSize = 0;
                    ImageSize = (buff[index + 3] & 0x00ff) << 8;
                    ImageSize = ImageSize | (buff[index + 4] & 0xff);
                    addr = 0;
                    int imageBase = index + 5;
                    byte[] temp_buff = BlockCopy(buff, imageBase, fSize - imageBase);
                    FIFO_AddBytes(temp_buff, fSize - imageBase);
                    addr += (fSize - imageBase);
                    m_receiveMode = 1;
                }
            } else if (m_receiveMode == 1) {
                if (addr < ImageSize) {
                    fSize = CQ_GetLength();
                    byte[] temp_buff = CQ_GetData(fSize);
                    FIFO_AddBytes(temp_buff, fSize);
                    addr += fSize;
                }
                if (addr >= ImageSize) {
                    m_pBuf = FIFO_GetData(FIFO_GetLength());
                    frameCount++;
                    m_receiveMode = 0;
                    /*System.arraycopy(m_pBuf, 0, adcValue, 0, 5);
                    adcValue[5] = m_pBuf[ImageSize - 5];
                    RxValue[0] = m_pBuf[ImageSize - 4];
                    RxValue[1] = m_pBuf[ImageSize - 3];
                    RxValue[2] = m_pBuf[ImageSize - 2];*/
                    /*String data_str = String
                    .format(Locale.KOREA, "ADC data [ %d %d %d %d %d %d ] \r\nRX serial data [ %d %d %d]",
                            (int) adcValue[0], (int) adcValue[1],
                            (int) adcValue[2], (int) adcValue[3],
                            (int) adcValue[4], (int) adcValue[5],
                            RxValue[0], RxValue[1], RxValue[2]);*/
                    // LogInfo(data_str);
                    getBitmapData();
                }
            }
        } catch (final Exception ex) {
            LogInfo(ex.toString());
        }
    }

    private void LogInfo(final String log) {
        Log.d(getClass().getSimpleName(), log);
    }

    private Bitmap getBitmapData() {
        try {
            byte[] PictureData;
            PictureData = BlockCopy(m_pBuf, 5, ImageSize - 10);

            return BitmapFactory.decodeByteArray(PictureData, 0, PictureData.length);
        } catch (Exception ex) {
            LogInfo(ex.toString());
        }
        return null;
    }

    private final class ConnectedTask implements Cancelable {
        private final AtomicBoolean mmClosed = new AtomicBoolean();
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        ConnectedTask(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(Constants.TAG, "sockets not created", e);
            }
            mmSocket = socket;
            mmInStream = in;
            mmOutStream = out;
        }

        public void cancel() {
            if (mmClosed.getAndSet(true)) {
                return;
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "close failed", e);
            }
        }

        void SendData(byte[] data) {
            if (mmSocket != null && mmOutStream != null) {
                try {

                    byte[] bytes_len = ShortToBytes((short) 9);
                    mmOutStream.write(bytes_len);
                    mmOutStream.flush();

                    mmOutStream.write(data, 0, 9);
                    mmOutStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] ShortsToBytes(short[] shorts) {
            int s_len = shorts.length;
            int b_len = s_len * 2;
            byte[] bytes = new byte[b_len];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
            return bytes;
        }

        byte[] ShortToBytes(short val) {
            return ShortsToBytes(new short[]{val});
        }

        public void run() {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int count;
            while (!mmClosed.get()) {
                try {
                    count = mmInStream.read(buffer);
                    received(buffer, count);
                } catch (IOException e) {
                    connectionLost(e);
                    cancel();
                    break;
                }
            }
        }

        /*
         * public void write(byte[] buffer) { try { mmOutStream.write(buffer); }
         * catch (IOException e) { Log.e(Constants.TAG, "write failed", e); } }
         */
        void connectionLost(IOException e) {
            Log.w(getClass().getSimpleName(), e);
            // dumpMessage(e.getLocalizedMessage());
        }

        void received(byte[] buffer, int count) {
            // String str = new String(buffer, 0, count);
            // dumpReceivedMessage(Integer.toString(str.length()));
            serialPort_DataReceived(buffer, count);
        }
    }

    private final class ConnectTask implements Cancelable {
        private final AtomicBoolean mmClosed = new AtomicBoolean();
        private final BluetoothSocket mmSocket;

        ConnectTask(BluetoothDevice device, UUID uuid) {
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(Constants.TAG, "create failed", e);
            }
            mmSocket = socket;
        }

        public void cancel() {
            if (mmClosed.getAndSet(true)) {
                return;
            }
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "close failed", e);
            }
        }

        public void run() {
            if (mBluetooth.isDiscovering()) {
                mBluetooth.cancelDiscovery();
            }
            try {
                mmSocket.connect();
                connected(mmSocket);
            } catch (IOException e) {
                connectionFailed(e);
                cancel();
            }
        }

        void connected(BluetoothSocket socket) {
            mLock.lock();
            try {
                // dumpMessage("connected");
                final ConnectedTask task = new ConnectedTask(socket);
                Cancelable canceller = new CancellingTask(mExec, task);
                mExec.execute(canceller);
                mConnectedTask = task;
            } finally {
                mLock.unlock();
            }
        }

        void connectionFailed(IOException e) {
            Log.w(getClass().getSimpleName(), e);
            // dumpMessage(e.getLocalizedMessage());
        }
    }
}
