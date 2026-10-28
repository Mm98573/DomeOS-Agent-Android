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
import com.domeos.agent.data.model.ChatSession
import com.domeos.agent.ui.components.CommonComponents
import com.domeos.agent.ui.components.SessionComponents
import com.domeos.agent.ui.components.SessionListHeader
import com.domeos.agent.ui.components.SessionListFooter
import com.domeos.agent.ui.components.SessionListItem
import com.domeos.agent.ui.components.EmptySessionState
import com.domeos.agent.domain.usecase.SessionUseCases

/**
 * 会话列表界面（侧边栏/抽屉）
 */
@Composable
fun SessionListScreen(
    sessionUseCases: SessionUseCases = hiltViewModel(),
    viewModel: MainViewModel = hiltViewModel(),
    onSessionClick: (ChatSession) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSessions by sessionUseCases.getActiveSessions().collectAsStateWithLifecycle()
    val pinnedSessions by sessionUseCases.getPinnedSessions().collectAsStateWithLifecycle()
    val archivedCount by sessionUseCases.getArchivedCount()

    val allSessions = remember(activeSessions, pinnedSessions) {
        // 置顶会话在前
        val pinned = pinnedSessions.toMutableList()
        val regular = activeSessions.filterNot { it.isPinned }
        pinned + regular
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头部
        SessionListHeader(
            onNewChatClick = { viewModel.createNewSession(); onCloseClick() },
            onSearchClick = { /* 显示搜索 */ }
        )

        CommonComponents.DomeOSDivider()

        // 会话列表
        if (allSessions.isEmpty()) {
            EmptySessionState(
                onNewChatClick = { viewModel.createNewSession(); onCloseClick() },
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allSessions, key = { it.id }) { session ->
                    val isSelected = viewModel.currentSession.value?.id == session.id
                    SessionListItem(
                        session = session,
                        isSelected = isSelected,
                        isPinned = session.isPinned,
                        onClick = { onSessionClick(session); onCloseClick() },
                        onLongClick = { /* 显示上下文菜单 */ },
                        onMoreClick = { /* 显示更多菜单 */ }
                    )
                }
            }
        }

        // 底部
        SessionListFooter(
            activeCount = activeSessions.size,
            archivedCount = archivedCount,
            onArchivedClick = { /* 显示归档会话 */ }
        )
    }
}

/** 会话搜索界面 */
@Composable
fun SessionSearchScreen(
    sessionUseCases: SessionUseCases = hiltViewModel(),
    onSessionClick: (ChatSession) -> Unit,
    onCloseClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by remember(query) {
        derivedStateOf {
            if (query.isBlank()) emptyList()
            else sessionUseCases.searchSessions(query).value?.toList() ?: emptyList()
        }
    }.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonComponents.DomeOSTopAppBar(
            title = "搜索会话",
            showBackButton = true,
            onBackClick = onCloseClick
        )

        CommonComponents.DomeOSSearchBar(
            query = query,
            onQueryChange = { query = it },
            placeholder = "搜索会话标题或内容...",
            onClearClick = { query = "" },
            modifier = Modifier.padding(16.dp)
        )

        CommonComponents.DomeOSDivider()

        if (query.isBlank()) {
            CommonComponents.DomeOSEmptyState(
                icon = androidx.compose.material.icons.Icons.Default.Search,
                title = "搜索会话",
                subtitle = "输入关键词查找历史会话",
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        } else if (results.isEmpty()) {
            CommonComponents.DomeOSEmptyState(
                icon = androidx.compose.material.icons.Icons.Default.SearchOff,
                title = "未找到匹配会话",
                subtitle = "尝试其他关键词",
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.id }) { session ->
                    SessionListItem(
                        session = session,
                        isSelected = false,
                        isPinned = session.isPinned,
                        onClick = { onSessionClick(session); onCloseClick() },
                        onMoreClick = { /* 显示更多菜单 */ }
                    )
                }
            }
        }
    }
}