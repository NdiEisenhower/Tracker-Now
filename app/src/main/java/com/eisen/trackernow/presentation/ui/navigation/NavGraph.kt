package com.eisen.trackernow.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.eisen.trackernow.presentation.ui.screens.detail.ShipmentDetailScreen
import com.eisen.trackernow.presentation.ui.screens.list.ShipmentListScreen
import com.eisen.trackernow.presentation.util.Resource
import com.eisen.trackernow.presentation.viewmodel.ShipmentListViewModel
import kotlinx.coroutines.delay

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
fun NavGraph(modifier: Modifier = Modifier, initialShipmentId: String? = null,  onDataLoaded: () -> Unit = {}) {
    val navController = rememberNavController()
    val viewModel: ShipmentListViewModel = viewModel()
    var isDataLoaded by remember { mutableStateOf(false) }
    val shipmentsState by viewModel.shipments.collectAsStateWithLifecycle()

    LaunchedEffect(shipmentsState, isDataLoaded) {
        when (shipmentsState) {
            is Resource.Success -> {
                val shipments = (shipmentsState as Resource.Success).data
                if (shipments != null && !isDataLoaded) {
                    delay(300)
                    isDataLoaded = true
                    onDataLoaded()
                }
            }
            is Resource.Error -> {
                if (!isDataLoaded) {
                    delay(300)
                    isDataLoaded = true
                    onDataLoaded()
                }
            }
            else -> {}
        }
    }

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
        ) {
            ShipmentDetailScreen(
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
            navController.navigate(Screen.ShipmentDetail.passId(shipmentId)) {
                popUpTo(Screen.ShipmentList.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }
}