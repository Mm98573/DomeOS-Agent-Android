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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DerivedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 聊天输入区组件
 */
@Composable
fun ChatInputArea(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    isStreaming: Boolean = false,
    onStopClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf(text) }
    var showAttachOptions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // 同步外部文本
    androidx.compose.runtime.LaunchedEffect(text) {
        if (inputText != text) {
            inputText = text
        }
    }

    val canSend = derivedStateOf {
        inputText.trim().isNotBlank() && enabled && !isStreaming
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // 附件选项
        if (showAttachOptions) {
            AttachOptionsBar(onDismiss = { showAttachOptions = false })
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 附件按钮
            if (!isStreaming) {
                IconButton(
                    onClick = { showAttachOptions = true },
                    enabled = enabled,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.AttachFile,
                        contentDescription = "附件",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 输入框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextField(
                        value = inputText,
                        onValueChange = { newText ->
                            inputText = newText
                            onTextChange(newText)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minHeight(48.dp)
                            .padding(vertical = 8.dp),
                        singleLine = false,
                        maxLines = 6,
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                            onDone = {
                                if (canSend.value) onSendClick()
                            }
                        ),
                        placeholder = { Text(text = "输入消息...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                        ),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedStrokeColor = Color.Transparent,
                            focusedStrokeColor = Color.Transparent,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )

                    // 语音输入按钮
                    if (!isStreaming && onVoiceClick != null) {
                        IconButton(
                            onClick = onVoiceClick!!,
                            enabled = enabled,
                            modifier = Modifier.size(40.dp).padding(end = -8.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Mic,
                                contentDescription = "语音输入",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 发送/停止按钮
            if (isStreaming) {
                IconButton(
                    onClick = onStopClick!!,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onSendClick,
                    enabled = canSend.value,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (canSend.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                    .background(
                        color = if (canSend.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(24.dp)
                    )
            }
        }

        // 提示文本
        if (!isStreaming) {
            Text(
                text = "Enter 发送，Shift+Enter 换行",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 56.dp, top = 4.dp)
            )
        }
    }
}

/** 附件选项栏 */
@Composable
fun AttachOptionsBar(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = { /* 图片 */ onDismiss() }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Image, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(text = "图片", fontSize = 11.sp)
            }
        }
        IconButton(onClick = { /* 文件 */ onDismiss() }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(text = "文件", fontSize = 11.sp)
            }
        }
        IconButton(onClick = { /* 代码 */ onDismiss() }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Code, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(text = "代码", fontSize = 11.sp)
            }
        }
        IconButton(onClick = { /* 相机 */ onDismiss() }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(text = "拍照", fontSize = 11.sp)
            }
        }
        IconButton(onClick = { /* 语音 */ onDismiss() }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(text = "语音", fontSize = 11.sp)
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss) {
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(24.dp).padding(12.dp))
        }
    }
}

/** 快速操作栏 */
@Composable
fun QuickActionsBar(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            androidx.compose.material3.FilledTonalButton(
                onClick = action.onClick,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (action.icon != null) {
                        Icon(imageVector = action.icon!!, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 6.dp))
                    }
                    Text(text = action.label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

data class QuickAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)