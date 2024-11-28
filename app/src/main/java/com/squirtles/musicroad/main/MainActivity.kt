package com.squirtles.musicroad.main

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.core.content.PermissionChecker
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.squirtles.musicroad.R
import com.squirtles.musicroad.main.navigations.MainNavGraph
import com.squirtles.musicroad.main.navigations.MainNavigationActions
import com.squirtles.musicroad.ui.theme.MusicRoadTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel by viewModels<MainViewModel>()
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (!allPermissionsGranted) {
            Toast.makeText(
                this,
                getString(R.string.main_permission_deny_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setKeepOnScreenCondition(splashScreen)
        enableEdgeToEdge()
        setContent {
            MusicRoadTheme {
                val navController = rememberNavController()
                val navigationActions = remember(navController) {
                    MainNavigationActions(navController)
                }
                MainNavGraph(
                    navController = navController,
                    navigationActions = navigationActions
                )
            }
        }

        if (!checkSelfPermission()) {
            permissionLauncher.launch(PERMISSIONS)
        }
    }

    private fun setKeepOnScreenCondition(splashScreen: SplashScreen) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    withTimeout(30_000L) {
                        mainViewModel.loadingState.collect { state ->
                            when (state) {
                                is LoadingState.Loading -> {
                                    splashScreen.setKeepOnScreenCondition { true }
                                }

                                is LoadingState.Success -> {
                                    Log.d("MainActivity", "Success: ${state.userId}")
                                    splashScreen.setKeepOnScreenCondition { false }
                                    cancel()
                                }

                                is LoadingState.NetworkError -> {
                                    Toast.makeText(this@MainActivity, getString(R.string.main_network_error_message), Toast.LENGTH_LONG).show()
                                    finish()
                                }

                                is LoadingState.UserNotFoundError -> {
                                    Toast.makeText(this@MainActivity, getString(R.string.main_user_not_found_message), Toast.LENGTH_LONG).show()
                                    finish()
                                }

                                is LoadingState.CreatedUserError -> {
                                    Toast.makeText(this@MainActivity, getString(R.string.main_create_user_fail_message), Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    Toast.makeText(this@MainActivity, getString(R.string.main_network_error_message), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun checkSelfPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(this, PERMISSIONS[0]) ==
                PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(this, PERMISSIONS[1]) ==
                PermissionChecker.PERMISSION_GRANTED
    }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
