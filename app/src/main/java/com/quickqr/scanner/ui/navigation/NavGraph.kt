package com.quickqr.scanner.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quickqr.scanner.ui.result.ResultScreen
import com.quickqr.scanner.ui.scan.ScanScreen

object Routes {
    const val SCAN = "scan"
    const val RESULT = "result/{payload}"

    fun result(raw: String): String =
        "result/${Uri.encode(raw)}"
}

@Composable
fun QuickQrNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SCAN) {
        composable(Routes.SCAN) {
            ScanScreen(
                onQrScanned = { raw ->
                    navController.navigate(Routes.result(raw)) {
                        launchSingleTop = true
                    }
                },
                onOpenHistoryItem = { raw ->
                    navController.navigate(Routes.result(raw)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("payload") { type = NavType.StringType })
        ) { entry ->
            val raw = Uri.decode(entry.arguments?.getString("payload").orEmpty())
            ResultScreen(
                rawValue = raw,
                onScanAgain = {
                    navController.popBackStack(Routes.SCAN, inclusive = false)
                }
            )
        }
    }
}
