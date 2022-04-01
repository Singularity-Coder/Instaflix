package com.singularitycoder.viewmodelstuff2.helpers.network

import android.content.Context
import android.net.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

// https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
// https://stackoverflow.com/questions/53532406/activenetworkinfo-type-is-deprecated-in-api-level-28

class NetworkState @Inject constructor(val context: Context) {

    private val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val oldActiveNet = conMan?.activeNetworkInfo
    private val oldWifi = conMan?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    private val oldMobile = conMan?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
    private val oldEthernet = conMan?.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
    private val hasOldWifi = null != oldWifi && oldWifi.isConnected
    private val hasOldCellular = null != oldMobile && oldMobile.isConnected
    private val hasOldEthernet = null != oldEthernet && oldEthernet.isConnected

    @RequiresApi(Build.VERSION_CODES.M) private val activeNet = conMan?.activeNetwork
    @RequiresApi(Build.VERSION_CODES.M) private val netCap = conMan?.getNetworkCapabilities(activeNet)
    @RequiresApi(Build.VERSION_CODES.M) private val hasWifi = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    @RequiresApi(Build.VERSION_CODES.M) private val hasCellular = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
    @RequiresApi(Build.VERSION_CODES.M) private val hasEthernet = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun listenToNetworkChangesAndDoWork(
        onlineWork: () -> Unit = {},
        onlineWifiWork: () -> Unit = {},
        onlineCellularWork: () -> Unit = {},
        onlineEthernetWork: () -> Unit = {},
        offlineWork: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (null == activeNet || null == netCap) {
                offlineWork.invoke()
                return
            }
        } else {
            if (null == oldActiveNet) {
                offlineWork.invoke()
                return
            }
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                CoroutineScope(IO).launch {
                    if (!hasActiveInternet()) {
                        withContext(Main) { offlineWork.invoke() }
                        return@launch
                    }

                    withContext(Main) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            when {
                                hasWifi -> onlineWifiWork.invoke()
                                hasCellular -> onlineCellularWork.invoke()
                                hasEthernet -> onlineEthernetWork.invoke()
                            }
                        } else {
                            when {
                                hasOldWifi -> onlineWifiWork.invoke()
                                hasOldCellular -> onlineCellularWork.invoke()
                                hasOldEthernet -> onlineEthernetWork.invoke()
                            }
                        }
                        onlineWork.invoke()
                    }
                }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
            }

            override fun onUnavailable() {
                super.onUnavailable()
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                super.onBlockedStatusChanged(network, blocked)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
            }

            override fun onLost(network: Network) {
                CoroutineScope(Main).launch { offlineWork.invoke() }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conMan?.registerDefaultNetworkCallback(networkCallback!!)
        } else {
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            conMan?.registerNetworkCallback(request, networkCallback!!)
        }
    }

    // Referred https://stackoverflow.com/ a long time ago - Checks active internet connection by pinging to Google servers
    // TODO Do in background
    private fun hasActiveInternet(): Boolean {
            if (isOffline()) return false
            try {
                val url = URL("https://clients3.google.com/generate_204")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "Android")
                    setRequestProperty("Connection", "close")
                    connectTimeout = 5E3.toInt()
                    connect()
                }
                return connection.responseCode == HttpURLConnection.HTTP_NO_CONTENT && connection.contentLength == 0
            } catch (e: IOException) {
                Timber.e(e, "Error checking internet connection")
            }
        return false
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    private fun hasInternet(): Boolean {
        @Suppress("DEPRECATION")
        fun checkOldWay(): Boolean {
            if (oldActiveNet?.isConnected == false) return false
            return hasOldWifi || hasOldCellular || hasOldEthernet
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWifi || hasCellular || hasEthernet
        } else checkOldWay()
    }

    fun isOnline() = hasInternet()

    fun isOffline() = !hasInternet()

    fun killNetworkCallback() {
        networkCallback ?: return
        conMan?.unregisterNetworkCallback(networkCallback!!)
    }
}
