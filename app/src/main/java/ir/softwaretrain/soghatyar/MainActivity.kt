package ir.softwaretrain.soghatyar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.ui.nav.BottomBar
import ir.softwaretrain.soghatyar.ui.nav.Routes
import ir.softwaretrain.soghatyar.ui.screens.BroadcastScreen
import ir.softwaretrain.soghatyar.ui.screens.CustomerDetailScreen
import ir.softwaretrain.soghatyar.ui.screens.CustomersScreen
import ir.softwaretrain.soghatyar.ui.screens.HomeScreen
import ir.softwaretrain.soghatyar.ui.screens.ProductsScreen
import ir.softwaretrain.soghatyar.ui.screens.SaleScreen
import ir.softwaretrain.soghatyar.ui.theme.SoghatyarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoghatyarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppRoot()
                    }
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val vm: AppViewModel = viewModel(factory = AppViewModel.Factory)

    Scaffold(
        bottomBar = { BottomBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen(vm, nav) }
            composable(Routes.CUSTOMERS) { CustomersScreen(vm, nav) }
            composable(Routes.BROADCAST) { BroadcastScreen(vm) }
            composable(Routes.PRODUCTS) { ProductsScreen(vm) }
            composable(
                route = Routes.CUSTOMER_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                CustomerDetailScreen(vm, nav, id)
            }
            composable(
                route = Routes.SALE,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.LongType; defaultValue = 0L
                })
            ) { entry ->
                val cid = entry.arguments?.getLong("customerId") ?: 0L
                SaleScreen(vm, nav, cid)
            }
        }
    }
}
