package com.singularitycoder.viewmodelstuff2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.scottyab.rootbeer.RootBeer
import com.singularitycoder.viewmodelstuff2.anime.model.AnimeData
import com.singularitycoder.viewmodelstuff2.anime.view.AnimeDetailFragment
import com.singularitycoder.viewmodelstuff2.anime.view.HomeFragment
import com.singularitycoder.viewmodelstuff2.databinding.ActivityMainBinding
import com.singularitycoder.viewmodelstuff2.favorites.FavoritesFragment
import com.singularitycoder.viewmodelstuff2.helpers.ShakeDetector
import com.singularitycoder.viewmodelstuff2.helpers.constants.IntentKey
import com.singularitycoder.viewmodelstuff2.helpers.constants.Tab
import com.singularitycoder.viewmodelstuff2.helpers.constants.mainActivityPermissions
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.network.NetworkState
import com.singularitycoder.viewmodelstuff2.helpers.utils.GeneralUtils
import com.singularitycoder.viewmodelstuff2.helpers.utils.doAfter
import com.singularitycoder.viewmodelstuff2.helpers.utils.isEmulator
import com.singularitycoder.viewmodelstuff2.more.view.MoreFragment
import com.singularitycoder.viewmodelstuff2.more.viewmodel.MoreViewModel
import com.singularitycoder.viewmodelstuff2.notifications.dao.NotificationsDao
import com.singularitycoder.viewmodelstuff2.notifications.view.NotificationsFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import javax.inject.Inject


// Today
// Fix Custom Rating
// Work Manager with result
// Foreground Service with large notif
// Tests

// WakeLock
// Alarm
// Job

// Lazy intialisation in Hilt - @Inject lateinit var gson: Lazy<Gson> - and the access it using gson.value. But its not working - https://stackoverflow.com/questions/51127524/dagger-lazy-during-constructor-injection

// TODO Download episodes here

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Its good to just create a single instance of Gson rather than creating multiple objects. Performance thing.
    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var networkState: NetworkState

    @Inject
    lateinit var utils: GeneralUtils

    @Inject
    lateinit var notificationsDao: NotificationsDao

    // The following are used for the shake detection
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null

    private val moreViewModel: MoreViewModel by viewModels()
//    val sharedViewModel: SharedViewModel by activityViewModels()  // Works only in Fragments

    private lateinit var binding: ActivityMainBinding

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(IntentKey.DATA_SHOW_NOTIFICATION_BADGE, false)) {
                setUpNotificationsCountBadge()
            }
        }
    }

    // Port this to a separate screen
    private val mainActivityPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: MutableMap<String, Boolean>? ->
        permissions ?: return@registerForActivityResult
        permissions.entries.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            Timber.i("Permission status: ", "${it.key} = ${it.value}")
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> {
                    // disable blocking layout and proceed
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                    // permission permanently denied. Show settings dialog
                    // enable blocking layout and show popup to go to settings
                    showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setTheme(R.style.Theme_ViewModelStuff2) // If u dont set this to default theme then all screens will inherit splashscreen theme set in manifest
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        grantPermissions()
        checkIfDeviceIsRooted()
        setUpBottomNav()
        showScreen(HomeFragment(), Tab.HOME.tag)
        getIntentDataAndNavigateAccordingly()
        setUpShakeDetectionSensor()
        startAnimeForegroundService()
        setUpBlurEffect()
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.fragments.lastOrNull() is AnimeDetailFragment) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.statusBarColor = Color.TRANSPARENT
            } else {
                window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
                window.statusBarColor = color(R.color.purple_700)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the Session Manager Listener onResume
        sensorManager!!.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        // Register local broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(IntentKey.ACTION_NOTIFICATION_BADGE))
        showNetworkStateWhenNecessary()
    }

    override fun onPause() {
        // Unregister the Sensor Manager onPause
        sensorManager!!.unregisterListener(shakeDetector)
        // Unregister local broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        networkState.killNetworkCallback()
        super.onPause()
    }

    private fun checkIfDeviceIsRooted() = CoroutineScope(IO).launch {
        // https://github.com/scottyab/rootbeer
        val isRooted = RootBeer(this@MainActivity).isRooted
        if (isRooted && !isEmulator()) {
            withContext(Main) { utils.showToast("Your device is rooted! Uninstalling Fav Anime...", this@MainActivity) }
            withTimeout(2000L) {
                finish()
                uninstall(app = BuildConfig.APPLICATION_ID)
            }
        }
    }

    private fun setUpBottomNav() {
        fun show(fragment: Fragment, tag: String): Boolean {
            // Home or Base fragments should not contain addToBackStack. But if u want to navigate to home frag then add HomeFrag
            supportFragmentManager.beginTransaction().replace(binding.bottomNavViewContainer.id, fragment, tag).commit()
            return true
        }

        binding.bottomNav.setOnItemSelectedListener { it: MenuItem ->
            return@setOnItemSelectedListener when (it.itemId) {
                R.id.nav_home -> show(HomeFragment(), Tab.HOME.tag)
                R.id.nav_favorites -> show(FavoritesFragment(), Tab.FAVORITES.tag)
                R.id.nav_notifications -> {
                    binding.bottomNav.removeBadge(R.id.nav_notifications)
                    show(NotificationsFragment(), Tab.NOTIFICATIONS.tag)
                }
                R.id.nav_more -> show(MoreFragment(), Tab.MORE.tag)
                else -> false
            }
        }
    }

    private fun getIntentDataAndNavigateAccordingly() {
        val animeDataFromWorkerNotif = intent.getParcelableArrayListExtra<AnimeData>(IntentKey.NOTIF_WORKER_RANDOM_ANIME)?.firstOrNull() ?: return
        showAnimeDetailsOfThis(animeId = animeDataFromWorkerNotif.id.toString())
    }

    private fun showScreen(fragment: Fragment, tag: String) {
        // Home or Base fragments should not contain addToBackStack. But if u want to navigate to home frag then add HomeFrag
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(binding.bottomNavViewContainer.id, fragment, tag)
            .commit()
    }

    private fun setUpShakeDetectionSensor() {
        // ShakeDetector initialization
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector()
        shakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                /** The following method, "onShake(count):" is a stub // method you would use to setup whatever you want done once the device has been shook. */
                // Send local broadcast to notifications activity and then load random anime
                val shakeIntent = Intent(IntentKey.ACTION_SHAKE).putExtra(IntentKey.DATA_LOAD_RANDOM_ANIME, true)
                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(shakeIntent)
                utils.showToast(count.toString(), this@MainActivity)
            }
        })
    }

    // https://stackoverflow.com/questions/31641973/how-to-blur-background-images-in-android
    // https://stackoverflow.com/questions/6795483/create-blurry-transparent-background-effect
    // https://github.com/wasabeef/Blurry
    private fun setUpBlurEffect() {
        // Blur effect not working
        Blurry.with(this)
            .radius(10)
            .sampling(8)
            .color(Color.argb(66, 255, 255, 0))
            .async()
            .onto(binding.bottomNav)
    }

    // https://stackoverflow.com/questions/42682855/display-badge-on-top-of-bottom-navigation-bars-icon
    private fun setUpNotificationsCountBadge() = CoroutineScope(Default).launch {
        binding.bottomNav.apply {
            val notificationsCount = getOrCreateBadge(R.id.nav_notifications).number
            removeBadge(R.id.nav_notifications) // to avoid adding duplicate views
            getOrCreateBadge(R.id.nav_notifications).number = notificationsCount + 1
        }
    }

    private fun showNetworkStateWhenNecessary() {
        networkState.listenToNetworkChangesAndDoWork(
            onlineWork = {
                binding.tvNetworkState.showOnlineStrip()
            },
            offlineWork = {
                binding.tvNetworkState.showOfflineStrip()
            }
        )
    }

    private fun grantPermissions() {
        // https://stackoverflow.com/questions/62202471/how-to-get-a-permission-request-in-new-activityresult-api-1-3-0-alpha05
        // https://stackoverflow.com/questions/29657948/get-the-current-location-fast-and-once-in-android/66051728#66051728
        mainActivityPermissionsResult.launch(mainActivityPermissions)
    }

    private fun TextView.showOfflineStrip() {
        this.apply {
            text = getString(R.string.offline)
            visible()
            setBackgroundColor(color(android.R.color.holo_red_dark))
            setTextColor(color(R.color.white))
        }
    }

    private fun TextView.showOnlineStrip() {
        this.apply {
            if (text != getString(R.string.offline)) return // This is to not show online evey time its online
            text = getString(R.string.online)
            visible()
            setBackgroundColor(color(android.R.color.holo_green_dark))
            setTextColor(color(R.color.white))
        }
        this.hideNetworkStripAfter5Sec()
    }

    private fun TextView.hideNetworkStripAfter5Sec() = doAfter(5.seconds()) { this.gone() }
}
