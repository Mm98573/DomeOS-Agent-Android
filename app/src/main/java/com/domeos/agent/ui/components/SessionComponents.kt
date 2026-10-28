package com.domeos.agent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domeos.agent.data.model.ChatSession
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * 会话列表项
 */
@Composable
fun SessionListItem(
    session: ChatSession,
    isSelected: Boolean,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasUnread = false // TODO: 实现未读逻辑

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 置顶图标
                if (isPinned) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.PushPin,
                        contentDescription = "置顶",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(end = 8.dp)
                    )
                }

                // 会话图标/颜色标记
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = session.colorTag?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    session.icon?.let { emoji ->
                        Text(text = emoji, fontSize = 18.dp)
                    } ?: Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 标题和预览
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp),
                    crossAxisAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = session.title,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (hasUnread) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Text(
                        text = session.lastMessagePreview.take(60),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
            }

            // 右侧：时间和更多菜单
            Column(
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = formatSessionTime(session.updatedAt),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                onMoreClick?.let {
                    IconButton(onClick = it, modifier = Modifier.padding(top = 4.dp, start = 8.dp)) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 格式化会话时间 */
private fun formatSessionTime(instant: kotlinx.datetime.Instant): String {
    val zdt = instant.atZone(TimeZone.currentSystemDefault())
    val now = kotlinx.datetime.Clock.System.now().atZone(TimeZone.currentSystemDefault())
    return if (zdt.toLocalDate() == now.toLocalDate()) {
        zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } else if (zdt.toLocalDate().plusDays(1) == now.toLocalDate()) {
        "昨天"
    } else {
        zdt.format(DateTimeFormatter.ofPattern("MM/dd"))
    }
}

/** 会话列表头部 */
@Composable
fun SessionListHeader(
    onNewChatClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "会话",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                androidx.compose.material3.FilledButton(
                    onClick = onNewChatClick,
                    colors = androidx.compose.material3.ButtonDefaults.filledButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 6.dp))
                        Text(text = "新建", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** 会话列表底部 */
@Composable
fun SessionListFooter(
    activeCount: Int,
    archivedCount: Int,
    onArchivedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$activeCount 个会话",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (archivedCount > 0) {
                Text(
                    text = "归档 ($archivedCount)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .wrapContentSize(Alignment.Center)
                        .clickable { onArchivedClick() }
                )
            }
        }
    }
}

/** 会话分组标题 */
@Composable
fun SessionGroupHeader(title: String, count: Int, modifier: Modifier = Modifier) {
    Text(
        text = "$title ($count)",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/** 空会话状态 */
@Composable
fun EmptySessionState(
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    androidx.compose.material3.Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无会话",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方按钮开始新的对话",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.FilledButton(
                onClick = onNewChatClick,
                colors = androidx.compose.material3.ButtonDefaults.filledButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                    Text(text = "新建会话", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}