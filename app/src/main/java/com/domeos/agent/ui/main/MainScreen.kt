package com.domeos.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domeos.agent.ui.components.ChatInputArea
import com.domeos.agent.ui.components.CommonComponents
import com.domeos.agent.ui.components.MessageBubble
import com.domeos.agent.ui.components.MessageComponents
import com.domeos.agent.ui.components.SessionComponents
import com.domeos.agent.ui.components.SessionListItem
import com.domeos.agent.ui.components.SessionListHeader
import com.domeos.agent.ui.components.SessionListFooter
import com.domeos.agent.ui.components.EmptySessionState
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.ChatSession

/**
 * 主聊天界面
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToQuickAccess: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val shouldScrollToBottom = remember { mutableStateOf(true) }

    // 自动滚动到底部
    androidx.compose.runtime.LaunchedEffect(messages.size, isStreaming) {
        if (shouldScrollToBottom.value) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部栏
                CommonComponents.DomeOSTopAppBar(
                    title = currentSession?.title ?: "DomeOS",
                    showBackButton = false,
                    actions = {
                        // 模型选择
                        IconButton(onClick = { /* 显示模型选择 */ }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Psychology,
                                contentDescription = "选择模型",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // 设置
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // 会话列表
                        IconButton(onClick = onNavigateToSessions) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                                contentDescription = "会话列表",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // 消息列表
                if (isLoading) {
                    CommonComponents.DomeOSLoadingState(modifier = Modifier.fillMaxSize().weight(1f))
                } else if (messages.isEmpty()) {
                    EmptyChatState(onNewChatClick = { viewModel.createNewSession() })
                        .fillMaxSize()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        state = scrollState,
                        reverseLayout = false,
                        userScrollEnabled = true
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onCopyClick = { content ->
                                    android.content.ClipboardManagerCompat.getInstance(
                                        androidx.core.content.ContextCompat.getMainExecutor(androidx.core.content.ContextCompat.getMainExecutor())
                                    ).setText(content)
                                    // TODO: 显示 Toast "已复制"
                                },
                                onRegenerateClick = { viewModel.regenerate() },
                                isStreaming = isStreaming && viewModel.currentStreamingMessageId.value == message.id
                            )
                        }
                    }
                }

                // 输入区
                ChatInputArea(
                    text = inputText,
                    onTextChange = { viewModel.updateInputText(it) },
                    onSendClick = { viewModel.sendMessage() },
                    onVoiceClick = { /* 语音输入 */ },
                    isStreaming = isStreaming,
                    onStopClick = { viewModel.stopGeneration() }
                )
            }
        }

        // 错误提示
        error?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                com.domeos.agent.ui.components.DomeOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 100.dp),
                    elevated = true
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                        IconButton(onClick = { viewModel._error.value = null }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
            }
        }
    }
}

/** 空聊天状态 */
@Composable
fun EmptyChatState(onNewChatClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // DomeOS Logo 动画
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.graphics.vector.AnimatedVector(
                    animatedVector = androidx.compose.animation.graphics.vector.animatedVectorResource(
                        id = R.drawable.ic_splash_animated
                    ),
                    modifier = Modifier.size(100.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "DomeOS Agent",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "你的专属 AI 助手\n随时准备为你服务",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

            // 快速操作建议
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = androidx.compose.material.icons.Icons.Default.Code,
                    title = "编程助手",
                    description = "代码编写、调试、重构",
                    onClick = { /* 预填编程提示词 */ }
                )
                QuickActionCard(
                    icon = androidx.compose.material.icons.Icons.Default.Article,
                    title = "写作助手",
                    description = "文章润色、创意写作、翻译",
                    onClick = { /* 预填写作提示词 */ }
                )
                QuickActionCard(
                    icon = androidx.compose.material.icons.Icons.Default.Analytics,
                    title = "数据分析",
                    description = "数据处理、图表生成、洞察",
                    onClick = { /* 预填数据分析提示词 */ }
                )
                QuickActionCard(
                    icon = androidx.compose.material.icons.Icons.Default.Lightbulb,
                    title = "灵感激发",
                    description = "头脑风暴、方案设计、决策支持",
                    onClick = { /* 预填创意提示词 */ }
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .padding(horizontal = 16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}