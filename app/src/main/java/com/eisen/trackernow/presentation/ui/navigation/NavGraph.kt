package com.eisen.trackernow.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.eisen.trackernow.presentation.ui.ThemeManager
import com.eisen.trackernow.presentation.ui.screens.detail.ShipmentDetailScreen
import com.eisen.trackernow.presentation.ui.screens.list.ShipmentListScreen
import com.eisen.trackernow.presentation.viewmodel.ShipmentListViewModel

sealed class Screen(val route: String) {
    object ShipmentList : Screen("shipment_list")
    object ShipmentDetail : Screen("shipment_detail/{shipmentId}") {
        fun passId(id: String): String = "shipment_detail/$id"
    }
    object DeepLink : Screen("shipment/{shipmentId}") {
        fun passId(shipmentId: String): String = "shipment/$shipmentId"
    }
}

@Composable
fun NavGraph(modifier: Modifier = Modifier, initialShipmentId: String? = null) {
    val navController = rememberNavController()
    val viewModel: ShipmentListViewModel = viewModel()

    LaunchedEffect(initialShipmentId) {
        initialShipmentId?.let { shipmentId ->
            navController.navigate(Screen.ShipmentDetail.passId(shipmentId)) {
                launchSingleTop = true
                popUpTo(Screen.ShipmentList.route) {
                    inclusive = false
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.ShipmentList.route,
    ) {
        composable(Screen.ShipmentList.route) {
            ShipmentListScreen(
                viewModel = viewModel,
                onShipmentClick = { shipmentId ->
                    navController.navigate(Screen.ShipmentDetail.passId(shipmentId))
                }
            )
        }

        composable(
            route = Screen.ShipmentDetail.route,
            arguments = listOf(
                navArgument("shipmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shipmentId = backStackEntry.arguments?.getString("shipmentId") ?: return@composable
            ShipmentDetailScreen(
                shipmentId = shipmentId,
                onBackClick = {
                    viewModel.checkForPendingUpdates()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DeepLink.route,
            arguments = listOf(
                navArgument("shipmentId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "tracknow://shipment/{shipmentId}"
                }
            )
        ) { backStackEntry ->
            val shipmentId = backStackEntry.arguments?.getString("shipmentId") ?: return@composable
            // Navigate to detail screen
            navController.navigate(Screen.ShipmentDetail.passId(shipmentId)) {
                popUpTo(Screen.ShipmentList.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }
}