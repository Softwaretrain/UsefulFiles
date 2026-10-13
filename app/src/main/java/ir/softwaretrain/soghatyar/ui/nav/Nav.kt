package ir.softwaretrain.soghatyar.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

object Routes {
    const val HOME = "home"
    const val CUSTOMERS = "customers"
    const val BROADCAST = "broadcast"
    const val PRODUCTS = "products"
    const val CUSTOMER_DETAIL = "customer/{id}"
    const val SALE = "sale?customerId={customerId}"

    fun customer(id: Long) = "customer/$id"
    fun sale(customerId: Long = 0L) = "sale?customerId=$customerId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "خانه", Icons.Filled.Home),
    Tab(Routes.CUSTOMERS, "مشتری‌ها", Icons.Filled.Groups),
    Tab(Routes.SALE, "فروش", Icons.Filled.PointOfSale),
    Tab(Routes.BROADCAST, "اعلام", Icons.Filled.Campaign),
    Tab(Routes.PRODUCTS, "کالاها", Icons.Filled.Inventory2)
)

@Composable
fun BottomBar(nav: NavController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar {
        tabs.forEach { tab ->
            val selected = when (tab.route) {
                Routes.SALE -> current?.startsWith("sale") == true
                else -> current == tab.route ||
                        (tab.route == Routes.CUSTOMERS && current == Routes.CUSTOMER_DETAIL)
            }
            NavigationBarItem(
                selected = selected,
                onClick = {
                    val dest = if (tab.route == Routes.SALE) Routes.sale(0L) else tab.route
                    nav.navigate(dest) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}
