package cz.smycka.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.getSystemService
import cz.smycka.example.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ConnectionObserver"
private const val CONNECTIVITY_CHECK_TIMEOUT_IN_MS = 2000

@Singleton
class ConnectionObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val capableNetworks: MutableSet<Network> = HashSet()

    private val googleDnsAddress: InetSocketAddress = InetSocketAddress("8.8.8.8", 53)

    private val networkRequest =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

    private val networkCallback: ConnectivityManager.NetworkCallback = createNetworkCallback()

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isConnected: Flow<Boolean> = _isConnected.asStateFlow()

    val isCurrentlyConnected: Boolean
        get() = _isConnected.value

    init {
        val manager = context.getSystemService<ConnectivityManager>()
        if (manager != null) {
            manager.registerNetworkCallback(networkRequest, networkCallback)
        } else {
            Log.e(TAG, "Could not create ConnectivityManager.")
        }
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(TAG, "#onAvailable()")

            applicationScope.launch {
                verifyNetwork(network)
            }
        }

        override fun onLost(network: Network) {
            Log.i(TAG, "#onLost()")
            super.onLost(network)
            capableNetworks.remove(network)
            _isConnected.value = capableNetworks.isNotEmpty()
        }
    }

    private suspend fun verifyNetwork(network: Network): Unit = withContext(Dispatchers.IO) {
        if (checkConnectivity(network)) {
            capableNetworks.add(network)
            _isConnected.value = capableNetworks.isNotEmpty()
        } else {
            Log.w(TAG, "Could not verify connection for the given network.")
        }
    }

    private suspend fun checkConnectivity(network: Network): Boolean = withContext(Dispatchers.IO) {
        var socketCreated = false
        var successfulPing = false
        try {
            val socket: Socket = network.socketFactory.createSocket()
            socketCreated = true
            socket.connect(googleDnsAddress, CONNECTIVITY_CHECK_TIMEOUT_IN_MS)
            successfulPing = true
            socket.close()
            Log.v(TAG, "Successful check for the connectivity.")
            true
        } catch (e: IOException) {
            when {
                successfulPing -> Log.e(TAG, "IO Exception while trying to close the socket.")
                socketCreated -> Log.e(TAG, "Could not reach internet.")
                else -> Log.e(TAG, "Unspecified IO Exception($e).")
            }
            false
        }
    }
}
