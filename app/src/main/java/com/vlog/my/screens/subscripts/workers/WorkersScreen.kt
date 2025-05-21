package com.vlog.my.screens.subscripts.workers

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import kotlin.let
import kotlin.ranges.rangeTo

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    scriptId: String = "",
    workerViewModel: WorkerViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        val userScript by workerViewModel.subScripts.collectAsState()
        val workSettings by workerViewModel.workersSettings.collectAsState()
        val performanceState by workerViewModel.performanceTestState.collectAsState()
        var isEnabled by remember { mutableStateOf(workSettings?.isEnabled == 1) }
        //var intervalMinutes by remember { mutableStateOf((workSettings?.isValued ?: 15).toFloat()) }
        var intervalMinutes by remember {
            mutableFloatStateOf(
                (workSettings?.isValued ?: 30).coerceAtLeast(30).toFloat())
        }
        
        // 加载指定ApiConfig的Workers配置
        LaunchedEffect(scriptId) {
            if (scriptId.isNotEmpty()) {
                workerViewModel.loadWorkSettingsByApiConfigId(scriptId)
            }
        }
        
        LaunchedEffect(workSettings) {
            isEnabled = workSettings?.isEnabled == 1
            //intervalMinutes = (workSettings?.isValued ?: 15).toFloat()
            intervalMinutes = (workSettings?.isValued ?: 30).coerceAtLeast(30).toFloat()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (scriptId.isNotEmpty()) "小程序[ ${userScript?.name} ]定时任务设置" else "首页内容自动更新设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "启用自动更新",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    isEnabled = checked
                                    workerViewModel.updateWorkSettings(checked, intervalMinutes.toInt(), scriptId)
                                }
                            )
                        }

                        if (isEnabled) {
                            Text(
                                text = "更新间隔：${intervalMinutes.toInt()}分钟",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Slider(
                                value = intervalMinutes,
                                onValueChange = { value ->
                                    intervalMinutes = value
                                    workerViewModel.updateWorkSettings(isEnabled, value.toInt(), scriptId)
                                },
                                valueRange = 30f..60f,
                                steps = 30,
                                //valueRange = 1f..60f,
                                //steps = 58,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 显示统计信息
                            workSettings?.let { settings ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "已刷新次数：${settings.refreshCount}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Text(
                                        text = "最后刷新时间：${
                                            if (settings.lastRefreshTime > 0) {
                                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                    .format(Date(settings.lastRefreshTime))
                                            } else {
                                                "从未刷新"
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "下次预计刷新时间：${
                                            if (settings.lastRefreshTime > 0) {
                                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                    .format(Date(settings.lastRefreshTime + settings.isValued * 60 * 1000))
                                            } else {
                                                "未知"
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 性能测试卡片
//            item {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text(
//                            text = "性能测试",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//
//                        var testDataCount by remember { mutableStateOf(1000) }
//
//                        OutlinedTextField(
//                            value = testDataCount.toString(),
//                            onValueChange = {
//                                testDataCount = it.toIntOrNull() ?: 1000
//                            },
//                            label = { Text("测试数据数量") },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = performanceState !is PerformanceTestState.Running
//                        )
//
//                        when (val state = performanceState) {
//                            is PerformanceTestState.Idle -> {
//                                Button(
//                                    onClick = {
//                                        workerViewModel.performanceBatchInsert(testDataCount)
//                                    },
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    Text("开始性能测试")
//                                }
//                            }
//                            is PerformanceTestState.Running -> {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    Text("处理中... ${state.progress}%")
//                                }
//                            }
//                            is PerformanceTestState.Completed -> {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    Text("测试完成！")
//                                    Text("处理数据量：${state.count}条")
//                                    Text("总耗时：${state.durationMs}毫秒")
//                                    Text("平均速度：${String.format("%.2f", state.count * 1000.0 / state.durationMs)}条/秒")
//
//                                    Button(
//                                        onClick = {
//                                            workerViewModel.performanceBatchInsert(testDataCount)
//                                        },
//                                        modifier = Modifier.fillMaxWidth()
//                                    ) {
//                                        Text("重新测试")
//                                    }
//                                }
//                            }
//                            is PerformanceTestState.Error -> {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    Text(
//                                        "测试失败：${state.message}",
//                                        color = MaterialTheme.colorScheme.error
//                                    )
//                                    Button(
//                                        onClick = {
//                                            workerViewModel.performanceBatchInsert(testDataCount)
//                                        },
//                                        modifier = Modifier.fillMaxWidth()
//                                    ) {
//                                        Text("重试")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}
