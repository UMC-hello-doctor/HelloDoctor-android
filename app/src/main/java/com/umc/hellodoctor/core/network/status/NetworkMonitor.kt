package com.umc.hellodoctor.core.network.status

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkMonitor(
    private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val tag = "NetworkMonitor"

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val callback = object : NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.i(tag, "Network available (id=${network.hashCode()})")
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                Log.i(tag, "Network lost (id=${network.hashCode()})")
                trySend(NetworkStatus.Lost)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                if (hasInternet) {
                    Log.i(
                        tag,
                        "Network capabilities changed: INTERNET=ON (id=${network.hashCode()})"
                    )
                    trySend(NetworkStatus.Available)
                } else {
                    Log.i(
                        tag,
                        "Network capabilities changed: INTERNET=OFF (id=${network.hashCode()})"
                    )
                    trySend(NetworkStatus.Lost)
                }
            }
        }

        Log.i(tag, "Register default network callback")
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            Log.i(tag, "Unregister network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
