package com.nojunjae.epor;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nojunjae.epor.activity.MainActivity;

import java.util.ArrayList;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.bluetooth.BluetoothDevice.ACTION_NAME_CHANGED;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

public class DeviceListFragment extends Fragment {

    private BluetoothAdapter mBluetooth;
    private DeviceDiscoveryHandler mDeviceDiscoveryHandler;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ProgressBar progressBar;
    private BluetoothDeviceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.device_list, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progressBar);
        adapter = new BluetoothDeviceAdapter(devices);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    private boolean isDeviceBonded(BluetoothDevice device) {
        return device.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    private boolean isNameRetreived(BluetoothDevice device) {
        return device.getName() != null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceDiscoveryHandler = new DeviceDiscoveryHandler();
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        startDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mDeviceDiscoveryHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeviceDiscoveryHandler.registerFor(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        devices.clear();
        ensurePairedDevices();
    }

    void ensurePairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetooth.getBondedDevices();
        devices.addAll(pairedDevices);
    }

    void startDiscovery() {
        if (mBluetooth.isDiscovering()) {
            mBluetooth.cancelDiscovery();
        }
        mBluetooth.startDiscovery();
    }

    private void setProgressBarVisibility(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        TextView macAddress;

        BluetoothDeviceViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            macAddress = itemView.findViewById(R.id.mac_address);
        }
    }

    private final class BluetoothDeviceAdapter
            extends RecyclerView.Adapter<BluetoothDeviceViewHolder> {

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private int mTextViewResourceId;
        private ArrayList<BluetoothDevice> devices;

        BluetoothDeviceAdapter(ArrayList<BluetoothDevice> devices) {
            this.devices = devices;
        }

        @Override
        public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.device_list_item, parent, false);
            return new BluetoothDeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
            BluetoothDevice device = devices.get(position);
            holder.deviceName.setText(device.getName());
            holder.macAddress.setText(device.getAddress());
            holder.itemView.setOnClickListener(
                    v -> {
                        BluetoothDevice remDev = devices.get(position);
                        ((MainActivity) getActivity()).exitSelectFragment(remDev);
                    });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }
    }

    private final class DeviceDiscoveryHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                    if (isDeviceBonded(device)) {
                        return;
                    }
                    if (!isNameRetreived(device)) {
                        return;
                    }
                    devices.add(device);
                    return;
                }
                if (action.equals(ACTION_NAME_CHANGED)) {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                    if (isDeviceBonded(device)) {
                        return;
                    }
                    devices.add(device);
                    return;
                }
                if (action.equals(ACTION_DISCOVERY_STARTED)) {
                    setProgressBarVisibility(true);
                    return;
                }
                if (action.equals(ACTION_DISCOVERY_FINISHED)) {
                    setProgressBarVisibility(false);
                }
            }
        }

        void registerFor(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_FOUND);
            filter.addAction(ACTION_NAME_CHANGED);
            filter.addAction(ACTION_DISCOVERY_STARTED);
            filter.addAction(ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(this, filter);
        }
    }
}
