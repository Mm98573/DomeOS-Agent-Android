package com.domeos.agent.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domeos.agent.R
import com.domeos.agent.ui.settings.SettingsScreen
import com.domeos.agent.ui.splash.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {

    private var isDrawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 设置系统栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    MainNavHost()
                }
            }
        }

        // 处理快捷方式启动
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        when (action) {
            Intent.ACTION_MAIN -> {
                // 正常启动
            }
            "com.domeos.agent.NEW_CHAT" -> {
                // 新建会话
            }
            "com.domeos.agent.QUICK_ACCESS" -> {
                // 快速访问
                showQuickAccess()
            }
            "com.domeos.agent.SETTINGS" -> {
                // 打开设置
            }
        }
    }

    private fun showQuickAccess() {
        // 显示快速访问底部弹窗
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // 显示设置
                true
            }
            R.id.action_new_chat -> {
                // 新建会话
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val mainViewModel = hiltViewModel<MainViewModel>()
    val sessionUseCases = hiltViewModel<SessionUseCases>()
    val settingsUseCases = hiltViewModel<SettingsUseCases>()
    val providerUseCases = hiltViewModel<ProviderUseCases>()

    var showSessions by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    NavHost(navController, "main") {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { showSettings = true },
                onNavigateToSessions = { showSessions = true },
                onNavigateToQuickAccess = { /* 显示快速访问 */ }
            )
        }
        composable("settings") {
            SettingsScreen(
                settingsUseCases = settingsUseCases,
                providerUseCases = providerUseCases,
                onCloseClick = { showSettings = false; navController.popBackStack() }
            )
        }
    }

    // 侧边抽屉：会话列表
    if (showSessions) {
        androidx.compose.material3.ModalDrawer(
            drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed),
            drawerContent = {
                SessionListScreen(
                    sessionUseCases = sessionUseCases,
                    viewModel = mainViewModel,
                    onSessionClick = { session ->
                        mainViewModel.switchSession(session)
                        showSessions = false
                    },
                    onCloseClick = { showSessions = false }
                )
            },
            gesturesEnabled = true
        ) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { showSettings = true },
                onNavigateToSessions = { showSessions = true },
                onNavigateToQuickAccess = { /* 显示快速访问 */ }
            )
        }
    }

    // 设置底部弹窗
    if (showSettings) {
        androidx.compose.material3.ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(androidx.compose.material3.BottomSheetValue.Hidden),
            sheetContent = {
                SettingsScreen(
                    settingsUseCases = settingsUseCases,
                    providerUseCases = providerUseCases,
                    onCloseClick = { showSettings = false }
                )
            }
        ) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { showSettings = true },
                onNavigateToSessions = { showSessions = true },
                onNavigateToQuickAccess = { /* 显示快速访问 */ }
            )
        }
    }
}