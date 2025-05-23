package com.vlog.my.screens.subscripts.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compost
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vlog.my.data.scripts.ContentType
import com.vlog.my.data.scripts.SubScripts

@Composable
fun SubScriptsItem(
    subScripts: SubScripts,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFetchDataClick: (SubScripts) -> Unit,
    onWorkClick: (SubScripts) -> Unit,
    onLogoEditClick: (SubScripts) -> Unit,
    onPublishClick: (SubScripts) -> Unit,
    onManageTracksClick: (SubScripts) -> Unit // Added callback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Logo和名称行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo显示
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    // 显示Logo图片，如果为空则显示默认图标
                    if (subScripts.logoUrl.isNullOrEmpty()) {
                        // 默认图标
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "默认Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // 显示自定义Logo
                        AsyncImage(
                            model = subScripts.logoUrl,
                            contentDescription = "API Logo",
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // 编辑Logo按钮
                IconButton(
                    onClick = { onLogoEditClick(subScripts) },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑Logo"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // API名称
                Text(
                    text = "Name: ${subScripts.name} / ${ContentType.findById(subScripts.isTyped)?.typeName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // API Key（如果有）
            subScripts.apiKey?.let {
                Text(
                    text = "API Key: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 显示关联的数据库名称
            val databaseDisplayName = if (subScripts.isTyped == ContentType.MUSIC.typeId) {
                if (subScripts.databaseName.isNullOrBlank()) "默认数据库" else subScripts.databaseName
            } else {
                if (subScripts.databaseName.isNullOrBlank()) "默认数据库" else "${subScripts.databaseName}.db"
            }
            Text(
                text = "数据库: $databaseDisplayName",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑按钮
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑"
                    )
                    Text(
                        text = "编辑",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 删除按钮
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDeleteClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除"
                    )
                    Text(
                        text = "删除",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 添加work
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onWorkClick(subScripts) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "定时任务"
                    )
                    Text(
                        text = "任务",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 发布按钮
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPublishClick(subScripts) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Compost,
                        contentDescription = "发布"
                    )
                    Text(
                        text = "发布",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                // 编辑按钮
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (subScripts.isTyped == ContentType.MUSIC.typeId) {
                                onManageTracksClick(subScripts)
                            } else {
                                onFetchDataClick(subScripts)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Dataset,
                        contentDescription = "查看数据"
                    )
                    Text(
                        text = "数据",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                // 编辑按钮
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }

            }
        }
    }
}