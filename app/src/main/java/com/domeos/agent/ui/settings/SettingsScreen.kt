package com.domeos.agent.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domeos.agent.data.model.AppSettings
import com.domeos.agent.data.model.ProviderConfig
import com.domeos.agent.data.model.ProviderType
import com.domeos.agent.data.model.ThemeMode
import com.domeos.agent.ui.components.CommonComponents
import com.domeos.agent.ui.components.DomeOSCard
import com.domeos.agent.domain.usecase.ProviderUseCases
import com.domeos.agent.domain.usecase.SettingsUseCases

/**
 * 设置界面
 */
@Composable
fun SettingsScreen(
    settingsUseCases: SettingsUseCases = hiltViewModel(),
    providerUseCases: ProviderUseCases = hiltViewModel(),
    onCloseClick: () -> Unit
) {
    val settings by settingsUseCases.getSettings().collectAsStateWithLifecycle()
    val providers by providerUseCases.getAllProviders().collectAsStateWithLifecycle()
    val enabledProviders by providerUseCases.getEnabledProviders().collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonComponents.DomeOSTopAppBar(
            title = "设置",
            showBackButton = true,
            onBackClick = onCloseClick
        )

        CommonComponents.DomeOSDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 常规设置
            SettingsSection(title = "常规") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Language,
                    title = "语言",
                    subtitle = getLanguageDisplayName(settings?.language ?: "zh_CN"),
                    onClick = { /* 显示语言选择 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Palette,
                    title = "主题",
                    subtitle = getThemeDisplayName(settings?.themeMode ?: ThemeMode.SYSTEM),
                    onClick = { showThemeDialog() }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.TextFields,
                    title = "字体大小",
                    subtitle = "${(settings?.fontScale ?: 1.0f) * 100}%",
                    onClick = { /* 显示字体大小调节 */ }
                )
            }

            // 模型设置
            SettingsSection(title = "模型与提供商") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Psychology,
                    title = "默认提供商",
                    subtitle = providers.firstOrNull { it.id == settings?.defaultProviderId }?.name ?: "未设置",
                    onClick = { /* 显示提供商选择 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.SmartToy,
                    title = "默认模型",
                    subtitle = settings?.defaultModelId ?: "未设置",
                    onClick = { /* 显示模型选择 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Edit,
                    title = "全局系统提示词",
                    subtitle = if ((settings?.globalSystemPrompt ?: "").isNotBlank()) "已设置" else "未设置",
                    onClick = { /* 编辑系统提示词 */ }
                )
            }

            // 提供商管理
            SettingsSection(title = "提供商管理") {
                enabledProviders.forEach { provider ->
                    ProviderSettingsItem(
                        provider = provider,
                        onClick = { /* 编辑提供商 */ },
                        onToggle = { enabled -> providerUseCases.setEnabled(provider.id, enabled) }
                    )
                }
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Add,
                    title = "添加提供商",
                    subtitle = "配置新的 AI 服务提供商",
                    onClick = { /* 添加提供商 */ }
                )
            }

            // 界面设置
            SettingsSection(title = "界面") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.FormatQuote,
                    title = "Markdown 渲染",
                    subtitle = if (settings?.enableMarkdown == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.enableMarkdown ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Functions,
                    title = "数学公式渲染",
                    subtitle = if (settings?.enableMath == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.enableMath ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Code,
                    title = "代码块主题",
                    subtitle = settings?.codeTheme ?: "github-dark",
                    onClick = { /* 选择代码主题 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Token,
                    title = "显示 Token 统计",
                    subtitle = if (settings?.showTokenCount == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.showTokenCount ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.PsychologyAlt,
                    title = "显示推理过程",
                    subtitle = if (settings?.showReasoning == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.showReasoning ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
            }

            // 行为设置
            SettingsSection(title = "行为") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.AutoFixHigh,
                    title = "自动滚动到底部",
                    subtitle = if (settings?.autoScroll == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.autoScroll ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Vibration,
                    title = "震动反馈",
                    subtitle = if (settings?.enableHaptics == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.enableHaptics ?: true,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.VolumeUp,
                    title = "声音反馈",
                    subtitle = if (settings?.enableSounds == true) "开启" else "关闭",
                    trailing = {
                        androidx.compose.material3.Switch(
                            checked = settings?.enableSounds ?: false,
                            onCheckedChange = { /* 更新设置 */ }
                        )
                    }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Keyboard,
                    title = "快捷键设置",
                    subtitle = "配置全局唤醒热键",
                    onClick = { /* 显示快捷键设置 */ }
                )
            }

            // 数据管理
            SettingsSection(title = "数据管理") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Download,
                    title = "导出数据",
                    subtitle = "导出所有会话和设置",
                    onClick = { /* 导出数据 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Upload,
                    title = "导入数据",
                    subtitle = "从文件恢复会话和设置",
                    onClick = { /* 导入数据 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.DeleteForever,
                    title = "清空所有数据",
                    subtitle = "删除所有会话、设置和缓存",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { /* 确认清空 */ }
                )
            }

            // 关于
            SettingsSection(title = "关于") {
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "DomeOS Agent v1.0.0",
                    onClick = { /* 显示版本详情 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.Description,
                    title = "开源协议",
                    subtitle = "MIT License",
                    onClick = { /* 显示协议 */ }
                )
                SettingsItem(
                    icon = androidx.compose.material.icons.Icons.Default.BugReport,
                    title = "反馈问题",
                    subtitle = "GitHub Issues",
                    onClick = { /* 打开反馈页面 */ }
                )
            }
        }
    }
}

/** 设置分组 */
@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 8.dp)
        )
        content()
    }
}

/** 设置项 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val hasTrailing = trailing != null
    val clickable = onClick != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(clickable) { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp).padding(end = 16.dp)
                )
                Column {
                    Text(text = title, fontSize = 15.sp, color = textColor)
                    Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (hasTrailing) {
                trailing()
            } else if (clickable) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** 提供商设置项 */
@Composable
fun ProviderSettingsItem(
    provider: ProviderConfig,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val typeIcon = getProviderIcon(provider.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(text = provider.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(text = provider.baseUrl, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            androidx.compose.material3.Switch(
                checked = provider.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/** 获取提供商图标 */
private fun getProviderIcon(type: ProviderType): ImageVector = when (type) {
    ProviderType.OPENAI -> androidx.compose.material.icons.Icons.Default.SmartToy
    ProviderType.ANTHROPIC -> androidx.compose.material.icons.Icons.Default.Psychology
    ProviderType.GOOGLE -> androidx.compose.material.icons.Icons.Default.Cloud
    ProviderType.OPENROUTER -> androidx.compose.material.icons.Icons.Default.Router
    ProviderType.OLLAMA -> androidx.compose.material.icons.Icons.Default.Computer
    ProviderType.AZURE -> androidx.compose.material.icons.Icons.Default.CloudQueue
    else -> androidx.compose.material.icons.Icons.Default.Settings
}

/** 语言显示名 */
private fun getLanguageDisplayName(code: String): String = when (code) {
    "zh_CN" -> "简体中文"
    "zh_TW" -> "繁體中文"
    "en_US" -> "English"
    "ja_JP" -> "日本語"
    "ko_KR" -> "한국어"
    else -> code
}

/** 主题显示名 */
private fun getThemeDisplayName(mode: ThemeMode): String = when (mode) {
    ThemeMode.LIGHT -> "浅色模式"
    ThemeMode.DARK -> "深色模式"
    ThemeMode.SYSTEM -> "跟随系统"
    ThemeMode.EXPRESSIVE -> "Expressive 动态"
}

/** 显示主题选择对话框 */
@Composable
fun showThemeDialog() {
    // TODO: 实现主题选择对话框
}