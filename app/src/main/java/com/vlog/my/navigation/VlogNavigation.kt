package com.vlog.my.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vlog.my.data.LocalDataHelper
import com.vlog.my.screens.bazaar.BazaarScriptsScreen
import com.vlog.my.screens.scripts.articles.ItemDetailScreen
import com.vlog.my.screens.home.HomeScreen
import com.vlog.my.screens.profile.ProfileScreen
import com.vlog.my.screens.scripts.articles.ItemListScreen
import com.vlog.my.screens.subscripts.workers.WorkersScreen
import com.vlog.my.screens.subscripts.AddSubScriptsScreen
import com.vlog.my.screens.subscripts.EditSubScriptsScreen
import com.vlog.my.screens.subscripts.SubScriptsScreen
import com.vlog.my.screens.users.LoginScreen
import com.vlog.my.screens.users.RegisterScreen
import com.vlog.my.screens.users.UserViewModel
import com.vlog.my.screens.subscripts.articles.AddArticleScreen
import com.vlog.my.screens.subscripts.music.AddMusicScreen
import com.vlog.my.screens.subscripts.videos.AddVideoScriptScreen
import com.vlog.my.screens.subscripts.ebooks.AddEbookScreen
import com.vlog.my.screens.subscripts.articles.EditArticleScreen
import com.vlog.my.screens.subscripts.music.EditMusicScreen
import com.vlog.my.screens.subscripts.videos.EditVideoScriptScreen
import java.net.URLDecoder

sealed class Screen(val route: String) {
    // 认证相关页面
    object Login : Screen("login")
    object Register : Screen("register")


    object Settings : Screen("settings") {
        fun createRoute(): String = "settings"
    }
    
    object Workers : Screen("workers/{scriptId}") {
        fun createRoute(scriptId: String): String = "workers/$scriptId"
    }


    object ScriptList : Screen("script_list") {
        fun createRoute(): String = "script_list"
    }

    object ScriptAdd : Screen("script_add") {
        fun createRoute(): String = "script_add"
    }

    object ScriptEdit : Screen("script_edit/{scriptId}") {
        fun createRoute(scriptId: String): String = "script_edit/$scriptId"
    }

    object ItemList : Screen("item_list/{scriptId}") {
        fun createRoute(scriptId: String = ""): String = "item_list/$scriptId"
    }

    object ItemDetail : Screen("item_detail/{scriptId}/{articlesId}") {
        fun createRoute(scriptId: String, articlesId: String): String = "item_detail/$scriptId/$articlesId"
    }




    object ArticlesList : Screen("articles_list/{scriptId}") {
        fun createRoute(scriptId: String = ""): String = "articles_list/$scriptId"
    }

    object ArticlesDetail : Screen("articles_detail/{scriptId}/{articlesId}") {
        fun createRoute(scriptId: String, articlesId: String): String = "articles_detail/$scriptId/$articlesId"
    }

    
    object MappingConfig : Screen("mapping_config/{scriptId}?initialConfig={initialConfig}") {
        fun createRoute(scriptId: String, initialConfig: String = ""): String = "mapping_config/$scriptId?initialConfig=$initialConfig"
    }
    
    object ShareApp : Screen("share_app?dbName={dbName}") {
        fun createRoute(dbName: String = ""): String = "share_app?dbName=$dbName"
    }
    
    object DownloadApp : Screen("download_app") {
        fun createRoute(): String = "download_app"
    }



    object MusicTracks : Screen("music_tracks/{subScriptId}/{musicDatabaseName}") {
        fun createRoute(subScriptId: String, musicDatabaseName: String): String = "music_tracks/$subScriptId/$musicDatabaseName"
    }


    // Video Feature Screens
    object VideoList : Screen("video_list/{scriptId}/{databaseName}") {
        fun createRoute(scriptId: String, databaseName: String): String = "video_list/$scriptId/$databaseName"
    }

    object AddVideo : Screen("add_video/{databaseName}") {
        fun createRoute(databaseName: String): String = "add_video/$databaseName"
    }

    // Route for VideoPlayerScreen, supporting either local DB video or direct URL
    // Example Nav: video_player?databaseName=myDb.db&videoId=123
    // Example Nav: video_player?videoUrl=http://example.com/video.mp4
    object VideoPlayer : Screen("video_player?databaseName={databaseName}&videoId={videoId}&videoUrl={videoUrl}") {
        fun createRouteForLocal(databaseName: String, videoId: String): String =
            "video_player?databaseName=$databaseName&videoId=$videoId"

        fun createRouteForUrl(videoUrl: String): String {
            // Ensure URL is properly encoded for navigation
            val encodedUrl = java.net.URLEncoder.encode(videoUrl, "UTF-8")
            return "video_player?videoUrl=$encodedUrl"
        }
    }

    object AddArticleScript : Screen("add_article_script") {
        fun createRoute(): String = "add_article_script"
    }

    object AddMusicScript : Screen("add_music_script") {
        fun createRoute(): String = "add_music_script"
    }

    object AddEbookScript : Screen("add_ebook_script") {
        fun createRoute(): String = "add_ebook_script"
    }

    object AddVideoScript : Screen("add_video_script") {
        fun createRoute(): String = "add_video_script"
    }

    object EditArticleScript : Screen("edit_article_script/{articleId}") {
        fun createRoute(articleId: String): String = "edit_article_script/$articleId"
    }

    object EditMusicScript : Screen("edit_music_script/{musicScriptId}") {
        fun createRoute(musicScriptId: String): String = "edit_music_script/$musicScriptId"
    }

    object EditEbookScript : Screen("edit_ebook_script/{ebookScriptId}") {
        fun createRoute(ebookScriptId: String): String = "edit_ebook_script/$ebookScriptId"
    }

    object EditVideoScript : Screen("edit_video_script/{videoScriptId}") {
        fun createRoute(videoScriptId: String): String = "edit_video_script/$videoScriptId"
    }
}

@Composable
fun VlogNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()
    val isLoggedIn = userViewModel.isLoggedIn()

    // 确定起始目的地
    val startDestination = if (isLoggedIn) {
        BottomNavItem.Home.route
    } else {
        Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航栏
            val currentRoute = currentRoute(navController)
            if (currentRoute in listOf(
                    BottomNavItem.Home.route,
                    BottomNavItem.Bazaar.route,
                    BottomNavItem.Publish.route,
                    BottomNavItem.Subscript.route,
                    BottomNavItem.Profile.route
                )
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            // 认证相关页面
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }



            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    navController = navController
                )
            }

            composable(BottomNavItem.Bazaar.route) {
                BazaarScriptsScreen(
                    navController = navController
                )
            }

            composable(BottomNavItem.Subscript.route) {
                SubScriptsScreen(
                    navController = navController
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    navController = navController
                )
            }

            
            composable(Screen.ScriptAdd.route) {
                AddSubScriptsScreen(
                    navController = navController
                )
            }

            composable(
                route = Screen.ScriptEdit.route,
                arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                EditSubScriptsScreen(
                    navController = navController,
                    scriptId = scriptId
                )
            }

            composable(
                route = Screen.ItemList.route,
                arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                ItemListScreen(
                    navController = navController,
                    scriptId = scriptId
                )
            }

            composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(
                    navArgument("scriptId") { type = NavType.StringType },
                    navArgument("articlesId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                val itemId = backStackEntry.arguments?.getString("articlesId") ?: ""
                ItemDetailScreen(
                    navController = navController,
                    itemId = itemId,
                    scriptId = scriptId
                )
            }
            
//            composable(
//                route = Screen.ShareApp.route,
//                arguments = listOf(navArgument("dbName") {
//                    type = NavType.StringType
//                    defaultValue = ""
//                    nullable = true
//                })
//            ) { backStackEntry ->
//                val dbName = backStackEntry.arguments?.getString("dbName") ?: ""
//                ShareScreen(
//                    navController = navController,
//                    databaseName = dbName
//                )
//            }
//
//            composable(Screen.DownloadApp.route) {
//                DownloadAppScreen(
//                    navController = navController
//                )
//            }
            
            composable(
                route = Screen.Workers.route,
                arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                WorkersScreen(
                    navController = navController,
                    scriptId = scriptId
                )
            }
            
            composable(
                route = Screen.MappingConfig.route,
                arguments = listOf(navArgument("scriptId") { type = NavType.IntType },navArgument("initialConfig") {
                    type = NavType.StringType 
                    defaultValue = ""
                    nullable = true
                })
            ) { backStackEntry ->
                val initialConfig = backStackEntry.arguments?.getString("initialConfig") ?: ""
                val decodedConfig = URLDecoder.decode(initialConfig, "UTF-8")
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                val context = LocalContext.current
                // 从本地存储中获取自定义数据库名称
val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
val customDbName = sharedPreferences.getString("custom_database_name", "")
val localDataHelper = remember { 
    if (customDbName.isNullOrBlank()) LocalDataHelper(context) 
    else LocalDataHelper(context, "$customDbName.db") 
}

//                MappingConfigScreen(
//                    navController = navController,
//                    initialMappingConfigJson = decodedConfig,
//                    apiConfigId = apiConfigId,
//                    localDataHelper = localDataHelper,
//                    onSaveConfig = { config ->
//                        // 返回到API配置界面并传递配置
//                        navController.previousBackStackEntry?.savedStateHandle?.set("mappingConfig", config)
//                        navController.popBackStack()
//                    }
//                )
            }

            composable(
                route = Screen.MusicTracks.route,
                arguments = listOf(
                    navArgument("subScriptId") { type = NavType.StringType },
                    navArgument("musicDatabaseName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
                val musicDatabaseName = backStackEntry.arguments?.getString("musicDatabaseName") ?: ""
                com.vlog.my.screens.subscripts.music.MusicTracksScreen(
                    navController = navController,
                    subScriptId = subScriptId,
                    musicDatabaseName = musicDatabaseName
                )
            }




            // Video Feature Screen Composable
            composable(
                route = Screen.VideoList.route,
                arguments = listOf(
                    navArgument("scriptId") { type = NavType.StringType },
                    navArgument("databaseName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: return@composable
                val databaseName = backStackEntry.arguments?.getString("databaseName") ?: return@composable
                com.vlog.my.screens.videos.VideoListScreen(
                    navController = navController,
                    scriptId = scriptId,
                    databaseName = databaseName
                )
            }

            composable(
                route = Screen.AddVideo.route,
                arguments = listOf(
                    navArgument("databaseName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val databaseName = backStackEntry.arguments?.getString("databaseName") ?: return@composable
                com.vlog.my.screens.videos.AddVideoScreen(
                    databaseName = databaseName
                    // onUploadSuccess = { navController.popBackStack() } // Example
                )
            }

            composable(
                route = Screen.VideoPlayer.route,
                arguments = listOf(
                    navArgument("databaseName") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("videoId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("videoUrl") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val databaseNameArg = backStackEntry.arguments?.getString("databaseName")
                val videoIdArg = backStackEntry.arguments?.getString("videoId")
                val videoUrlArg = backStackEntry.arguments?.getString("videoUrl")?.let {
                    java.net.URLDecoder.decode(it, "UTF-8") // Decode URL
                }

                // Validate that we have a valid source
                if ((databaseNameArg != null && videoIdArg != null) || videoUrlArg != null) {
                    com.vlog.my.screens.videos.VideoPlayerScreen(
                        navController = navController,
                        databaseName = databaseNameArg.toString(),
                        initialVideoId = videoIdArg.toString(),
                        initialVideoUrl = videoUrlArg
                    )
                } else {
                    // Handle error: Invalid arguments, navigate back or show error message
                    // For simplicity, just popBackStack, but a real app might show a toast/error screen
                    Text("Error: Invalid video source provided.")
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable(Screen.AddArticleScript.route) {
                AddArticleScreen(navController = navController)
            }

            composable(Screen.AddMusicScript.route) {
                AddMusicScreen(navController = navController)
            }

            composable(Screen.AddVideoScript.route) {
                AddVideoScriptScreen(navController = navController)
            }

            composable(Screen.AddEbookScript.route) {
                AddEbookScreen(navController = navController)
            }

            composable(
                route = Screen.EditArticleScript.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId")
                EditArticleScreen(navController = navController, articleId = articleId)
            }

            composable(
                route = Screen.EditMusicScript.route,
                arguments = listOf(navArgument("musicScriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val musicScriptId = backStackEntry.arguments?.getString("musicScriptId")
                EditMusicScreen(navController = navController, musicScriptId = musicScriptId)
            }

            composable(
                route = Screen.EditVideoScript.route,
                arguments = listOf(navArgument("videoScriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoScriptId = backStackEntry.arguments?.getString("videoScriptId")
                EditVideoScriptScreen(navController = navController, videoScriptId = videoScriptId)
            }

        }
    }
}


@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Bazaar,
        BottomNavItem.Publish,
        BottomNavItem.Subscript,
        BottomNavItem.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
