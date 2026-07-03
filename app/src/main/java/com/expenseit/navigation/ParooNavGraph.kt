package com.expenseit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expenseit.feature.tracker.ui.dashboard.DashboardScreen
import com.expenseit.feature.tracker.ui.transactions.TransactionsScreen
import com.expenseit.feature.splitter.ui.friends.FriendsScreen
import com.expenseit.feature.splitter.ui.groups.GroupsScreen
import com.expenseit.feature.splitter.ui.group_detail.GroupDetailScreen
import com.expenseit.feature.auth.ui.LoginScreen
import com.google.firebase.auth.FirebaseAuth

/**
 * Bottom navigation destinations.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        "dashboard", "Dashboard",
        Icons.Filled.Dashboard, Icons.Outlined.Dashboard
    )

    data object Transactions : BottomNavItem(
        "transactions", "Expenses",
        Icons.Filled.Receipt, Icons.Outlined.Receipt
    )

    data object Groups : BottomNavItem(
        "groups", "Split",
        Icons.Filled.CallSplit, Icons.Outlined.CallSplit
    )

    data object Friends : BottomNavItem(
        "friends", "Friends",
        Icons.Filled.People, Icons.Outlined.People
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Transactions,
    BottomNavItem.Groups,
    BottomNavItem.Friends
)

/**
 * Main app scaffold with bottom navigation and nav host.
 */
@Composable
fun ParooNavGraph(
    themeMode: Int,
    onThemeChange: (Int) -> Unit
) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDest = if (currentUser == null) "login" else BottomNavItem.Dashboard.route

    Scaffold(
        bottomBar = { ParooBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    themeMode = themeMode,
                    onThemeChange = onThemeChange
                )
            }

            composable(BottomNavItem.Transactions.route) {
                TransactionsScreen()
            }

            composable(BottomNavItem.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId ->
                        navController.navigate("group_detail/$groupId")
                    }
                )
            }

            composable(BottomNavItem.Friends.route) {
                FriendsScreen()
            }

            composable(
                route = "group_detail/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                GroupDetailScreen(
                    groupId = groupId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ParooBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on group detail screen or login screen
    val currentRoute = currentDestination?.route
    if (currentRoute?.startsWith("group_detail") == true || currentRoute == "login") return

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
