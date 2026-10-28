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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domeos.agent.data.model.ChatMessage
import com.domeos.agent.data.model.MessageRole
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * 消息气泡组件
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    onCopyClick: (String) -> Unit,
    onRegenerateClick: (() -> Unit)? = null,
    onToolClick: ((String) -> Unit)? = null,
    showAvatar: Boolean = true,
    isStreaming: Boolean = false
) {
    val role = message.role
    val isUser = role == MessageRole.USER
    val isAssistant = role == MessageRole.ASSISTANT
    val isTool = role == MessageRole.TOOL
    val isSystem = role == MessageRole.SYSTEM

    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        isAssistant -> MaterialTheme.colorScheme.surfaceContainerHigh
        isTool -> MaterialTheme.colorScheme.secondaryContainer
        isSystem -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val textColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        isAssistant -> MaterialTheme.colorScheme.onSurface
        isTool -> MaterialTheme.colorScheme.onSecondaryContainer
        isSystem -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val avatarIcon = when {
        isUser -> androidx.compose.material.icons.Icons.Default.Person
        isAssistant -> androidx.compose.material.icons.Icons.Default.SmartToy
        isTool -> androidx.compose.material.icons.Icons.Default.Build
        isSystem -> androidx.compose.material.icons.Icons.Default.Info
        else -> androidx.compose.material.icons.Icons.Default.Help
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser && showAvatar) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 4.dp, end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatarIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            crossAxisAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (!isUser && message.role != MessageRole.SYSTEM) {
                Text(
                    text = getRoleDisplayName(role),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(
                            topStart = if (isUser) 0.dp else 16.dp,
                            topEnd = if (isUser) 16.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = if (isUser) 16.dp else 0.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    crossAxisAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    // 推理内容
                    message.reasoning?.let { reasoning ->
                        if (reasoning.isNotBlank()) {
                            ReasoningBlock(content = reasoning)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // 主要内容
                    MessageContent(
                        content = message.content,
                        isCodeBlock = false,
                        textColor = textColor
                    )

                    // 工具调用
                    if (message.toolCalls?.isNotBlank() == true) {
                        ToolCallsBlock(
                            toolCalls = message.getToolCalls(),
                            onToolClick = onToolClick
                        )
                    }

                    // 工具结果
                    if (message.toolResults?.isNotBlank() == true) {
                        ToolResultsBlock(
                            toolResults = message.getToolResults()
                        )
                    }

                    // 错误信息
                    message.error?.let { error ->
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        ErrorMessage(text = error)
                    }

                    // 底部操作栏
                    if (isAssistant && !isStreaming) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        MessageActions(
                            messageId = message.id,
                            content = message.content,
                            onCopyClick = onCopyClick,
                            onRegenerateClick = onRegenerateClick
                        )
                    }

                    // 流式指示器
                    if (isStreaming) {
                        StreamingIndicator()
                    }
                }
            }

            // 时间戳
            Text(
                text = formatTime(message.createdAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
            )
        }

        if (isUser && showAvatar) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 4.dp, start = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatarIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** 角色显示名称 */
private fun getRoleDisplayName(role: MessageRole): String = when (role) {
    MessageRole.USER -> "你"
    MessageRole.ASSISTANT -> "DomeOS"
    MessageRole.SYSTEM -> "系统"
    MessageRole.TOOL -> "工具"
    MessageRole.DEVELOPER -> "开发者"
}

/** 格式化时间 */
private fun formatTime(instant: kotlinx.datetime.Instant): String {
    val zdt = instant.atZone(TimeZone.currentSystemDefault())
    val now = kotlinx.datetime.Clock.System.now().atZone(TimeZone.currentSystemDefault())
    return if (zdt.toLocalDate() == now.toLocalDate()) {
        zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } else {
        zdt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }
}

/** 消息内容渲染（支持 Markdown） */
@Composable
fun MessageContent(
    content: String,
    isCodeBlock: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (content.isBlank()) return

    // 简单的 Markdown 渲染：代码块、行内代码、粗体、斜体
    // 实际项目中建议使用 Markwon 或 markdown-compose 库
    androidx.compose.foundation.text.BasicText(
        text = AnnotatedString(content),
        style = TextStyle(
            color = textColor,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** 推理块 */
@Composable
fun ReasoningBlock(content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp).padding(end = 8.dp)
            )
            Text(
                text = "思考过程",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 工具调用块 */
@Composable
fun ToolCallsBlock(
    toolCalls: List<com.domeos.agent.data.model.ToolCall>,
    onToolClick: ((String) -> Unit)?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        toolCalls.forEach { call ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = call.function.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = call.function.arguments.take(100),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                            )
                        }
                    }
                    onToolClick?.let { click ->
                        IconButton(onClick = { click(call.id) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ExpandMore,
                                contentDescription = "展开",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 工具结果块 */
@Composable
fun ToolResultsBlock(
    toolResults: List<com.domeos.agent.data.model.ToolResult>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        toolResults.forEach { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (result.isError)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.isError)
                                    androidx.compose.material.icons.Icons.Default.Error
                                else
                                    androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (result.isError)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(end = 8.dp)
                            )
                            Text(
                                text = result.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.content,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/** 错误消息 */
@Composable
fun ErrorMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp).padding(end = 8.dp).padding(top = 2.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 消息操作栏 */
@Composable
fun MessageActions(
    messageId: Long,
    content: String,
    onCopyClick: (String) -> Unit,
    onRegenerateClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        horizontalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onCopyClick(content) },
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                contentDescription = "复制",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        onRegenerateClick?.let { regenerate ->
            IconButton(
                onClick = regenerate,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                    contentDescription = "重新生成",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** 流式指示器 */
@Composable
fun StreamingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.animation.graphics.vector.AnimatedVector(
            animatedVector = androidx.compose.animation.graphics.vector.animatedVectorResource(
                id = R.drawable.ic_typing_indicator
            ),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "正在生成...",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** 代码块组件 */
@Composable
fun CodeBlock(
    code: String,
    language: String = "",
    onCopyClick: (String) -> Unit
) {
    var showCopyTooltip by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 语言标签和复制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (language.isNotBlank()) {
                    Text(
                        text = language,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                IconButton(
                    onClick = { onCopyClick(code); showCopyTooltip = true },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                        contentDescription = "复制代码",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // 代码内容
            androidx.compose.foundation.text.BasicText(
                text = AnnotatedString(code),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}