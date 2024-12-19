package com.example.ironwill;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Service;
import android.os.IBinder;

//public class MDNSDiscovery extends Service{
public class MDNSDiscovery{
    private OnServiceResolvedListener listener;


    public void setOnServiceResolvedListener(OnServiceResolvedListener listener) {
        this.listener = listener;
    }

    private static final String TAG = "MDNSDiscovery";
    private static final String SERVICE_TYPE = "_GTXX-server._tcp";

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    public void discoverMDNSService(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        if (nsdManager == null) {
            Log.e(TAG, "NsdManager is null");
            return;
        }

        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "onStartDiscoveryFailed: " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "onStopDiscoveryFailed: " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "onDiscoveryStarted: " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "onDiscoveryStopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceFound: " + serviceInfo);
                nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "onResolveFailed: " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "onServiceResolved: " + serviceInfo);
                        // 最好采取读取自定义信息
                        // 读取自定义信息
                        String serverPort = new String(serviceInfo.getAttributes().get("server_port"));
                        String serverIP = new String(serviceInfo.getAttributes().get("server_IP"));
                        String version = new String(serviceInfo.getAttributes().get("version"));
                        Log.d(TAG, "Server Port: " + serverPort);
                        Log.d(TAG, "Server IP: " + serverIP);
                        Log.d(TAG, "Version: " + version);
                        //String ipAddress = serviceInfo.getHost().getHostAddress();
                        String ipAddress = serverIP;
                        int port = serviceInfo.getPort();

                        // 通知接口的实现类
                        if (listener != null) {
                            listener.onServiceResolved(ipAddress, port);
                        }

                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceLost: " + serviceInfo);
            }
        };

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        if (nsdManager != null && discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }
}
