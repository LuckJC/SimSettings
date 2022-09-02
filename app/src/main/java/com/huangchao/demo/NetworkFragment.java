package com.huangchao.demo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkFragment extends Fragment {

    private static final String TAG = "NetworkFragment";
    private static final int PERMISSON_REQUESTCODE = 0;
    private Context mContext;
    private MyItemRecyclerViewAdapter myAdapter;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private List<MyPhoneStateListener> myPhoneStateListener;
    private Map<MyPhoneStateListener, TelephonyManager> myPhoneStateListenerTelephonyManagerMap;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver action = " + action);
            if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_PHONE_STATE},
                            PERMISSON_REQUESTCODE);
                    return;
                }
                SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(subId);
                MainContent.ITEMS.get(0).details = subInfo.getDisplayName().toString();
                myAdapter.notifyItemChanged(0);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (mContext == null) return;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mSubscriptionManager = (SubscriptionManager) mContext.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        myPhoneStateListener = new ArrayList<>();
        myPhoneStateListenerTelephonyManagerMap = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            myAdapter = new MyItemRecyclerViewAdapter(MainContent.ITEMS);
            recyclerView.setAdapter(myAdapter);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainContent.ITEMS.clear();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSON_REQUESTCODE);
            return;
        }
        MainContent.addMainItem(MainContent.createMainItem(
                "Preferred SIM",
                ""));
        List<SubscriptionInfo> list = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (list != null && !list.isEmpty()) {
            for (SubscriptionInfo subInfo : list) {
                TelephonyManager telephonyManager = mTelephonyManager.createForSubscriptionId(subInfo.getSubscriptionId());
                //subInfo.getSubscriptionId();

                MainContent.addMainItem(MainContent.createMainItem(
                        subInfo.getDisplayName().toString(),
                        telephonyManager.getNetworkOperatorName()));

                MainContent.addMainItem(MainContent.createMainItem(
                        getString(R.string.signal_strength),
                        ""));

                if (isSubscriptionInService(subInfo.getSubscriptionId())) {
                    MyPhoneStateListener listener = new MyPhoneStateListener(MainContent.ITEMS.size() - 1);
                    myPhoneStateListener.add(listener);
                    myPhoneStateListenerTelephonyManagerMap.put(listener, telephonyManager);
                    MainContent.addMainItem(MainContent.createMainItem(
                            getString(R.string.network_registered),
                            getString(R.string.yes)));
                } else {
                    MainContent.addMainItem(MainContent.createMainItem(
                            getString(R.string.network_registered),
                            getString(R.string.no)));
                }

                MainContent.addMainItem(MainContent.createMainItem(
                        getString(R.string.network_type),
                        getNetworkTypeName(telephonyManager.getNetworkType())));

                MainContent.addMainItem(MainContent.createMainItem(
                        getString(R.string.status_imsi_id),
                        telephonyManager.getSubscriberId()));

                MainContent.addMainItem(MainContent.createMainItem(
                        getString(R.string.status_icc_id),
                        telephonyManager.getSimSerialNumber()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        for (MyPhoneStateListener listener : myPhoneStateListener) {
            TelephonyManager telephonyManager = myPhoneStateListenerTelephonyManagerMap.get(listener);
            if (telephonyManager != null) {
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        for (MyPhoneStateListener listener : myPhoneStateListener) {
            TelephonyManager telephonyManager = myPhoneStateListenerTelephonyManagerMap.get(listener);
            if (telephonyManager != null) {
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
            }
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD_SCDMA";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN";
            case /*TelephonyManager.NETWORK_TYPE_LTE_CA*/ 19:
                return "LTE_CA";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "NR";
            default:
                return "UNKNOWN";
        }
    }

    public ServiceState getServiceStateForSubscriber(int subId) {
        try {
            Method method = mTelephonyManager.getClass().getMethod("getServiceStateForSubscriber", int.class);
            return (ServiceState) method.invoke(mTelephonyManager, subId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isSubscriptionInService(int subId) {
        if (mTelephonyManager != null) {
            return getServiceStateForSubscriber(subId).getState()
                    == ServiceState.STATE_IN_SERVICE;
        }
        return false;
    }

    class MyPhoneStateListener extends PhoneStateListener {
        private final int position;
        private final MainContent.MainItem mainItem;

        public MyPhoneStateListener(int position) {
            super();
            this.position = position;
            mainItem = MainContent.ITEMS.get(position);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            try {
                Method method = signalStrength.getClass().getMethod("getDbm");
                int dbm = (int) method.invoke(signalStrength);
                if (mainItem != null) {
                    mainItem.details = dbm + " dmb";
                    myAdapter.notifyItemChanged(position);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] paramArrayOfInt) {
        super.onRequestPermissionsResult(requestCode, permissions, paramArrayOfInt);
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                Toast.makeText(getContext(), "Request Permission Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}