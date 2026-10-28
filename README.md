# DomeOS Agent - Android 版

基于 Material 3 Expressive 设计的 AI Agent 客户端，移植自 [WinUI 3 版本](https://github.com/liu7pro/DomeOS-AI-Agent-with-WinUI-3)。

## ✨ 功能特性

- 🤖 **多模型支持**: OpenAI / Anthropic / Google Gemini / OpenRouter / Ollama / 自定义
- 💬 **流式对话**: 逐 Token 渲染，长回复不卡顿
- 🔧 **工具调用可视化**: 结构化展示 Agent 思考与调用过程
- 📝 **Markdown & 数学公式**: 完整渲染支持
- 🎨 **Material 3 Expressive**: 动态圆角、动态配色、流式动画
- 🌙 **主题系统**: 浅色/深色/跟随系统/Expressive 动态
- 📦 **会话管理**: 历史、重命名、搜索、归档、置顶、分支
- ⚡ **快速访问**: 悬浮窗、开机自启、全局热键、通知栏入口
- 🔐 **隐私安全**: 本地存储、生物识别锁定、API Key 加密
- 📤 **数据可迁移**: 一键导出/导入所有会话与设置

## 🚀 快速开始

### 本地编译
```bash
# 1. 环境要求
# - JDK 17+ (推荐 21)
# - Android SDK (API 35, Build Tools 35.0.0)

# 2. 生成 Gradle Wrapper
mkdir -p gradle/wrapper
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar
chmod +x gradlew

# 3. 编译 Debug APK
./gradlew :app:assembleDebug

# 4. 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions 自动编译 (推荐)

#### 1. 创建 GitHub 仓库
```bash
# 在 GitHub 网页创建新仓库，或使用 CLI
gh repo create DomeOS-Agent --public --source=. --push
```

#### 2. 推送代码
```bash
git init
git add .
git commit -m "feat: initial commit - DomeOS Agent Android"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/DomeOS-Agent.git
git push -u origin main
```

#### 3. 自动触发构建
推送到 `main` 分支或创建 Tag (`v1.0.0`) 会自动触发：
- ✅ 代码检查
- ✅ 单元测试
- ✅ Debug APK 构建 (可在 Actions 下载)
- ✅ Release APK 构建 (仅 Tag 触发)

#### 4. 下载构建产物
- 进入仓库 **Actions** 标签页
- 点击最新的工作流运行
- 在 **Artifacts** 下载 `app-debug.apk`

#### 5. 发布正式版本 (可选)
```bash
# 创建并推送 Tag
git tag v1.0.0
git push origin v1.0.0
```
这将自动：
- 构建签名 Release APK
- 创建 GitHub Release 页面
- 上传 APK 到 Release Assets

### 签名配置 (Release 必需)
在 GitHub 仓库 **Settings > Secrets and variables > Actions** 添加：
| Secret 名称 | 说明 |
|-------------|------|
| `KEYSTORE_BASE64` | `base64 -w0 keystore.jks` 输出 |
| `KEYSTORE_PASSWORD` | Keystore 密码 |
| `KEY_ALIAS` | Key 别名 |
| `KEY_PASSWORD` | Key 密码 |

生成 keystore:
```bash
keytool -genkeypair -v -keystore keystore.jks -alias domeos \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 keystore.jks  # 复制输出到 KEYSTORE_BASE64
```

## 📁 项目结构

```
app/src/main/java/com/domeos/agent/
├── data/
│   ├── model/          # ChatMessage, ChatSession, ProviderConfig, AppSettings
│   ├── local/          # Room Database, DAOs, Converters
│   ├── remote/         # Retrofit APIs, DTOs, ApiClientFactory
│   └── repository/     # Repository 实现
├── domain/
│   ├── repository/     # Repository 接口
│   └── usecase/        # ChatUseCases, SessionUseCases, ProviderUseCases, SettingsUseCases
├── ui/
│   ├── main/           # MainScreen, SessionListScreen, MainViewModel
│   ├── settings/       # SettingsScreen
│   ├── splash/         # SplashActivity
│   └── components/     # 通用组件
├── service/            # QuickAccessService (悬浮窗)
├── receiver/           # BootReceiver (开机自启)
├── di/                 # Hilt Modules
└── util/               # EncryptionUtil, PreferencesUtil
```

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| UI | Jetpack Compose, Material 3, Material 3 Expressive |
| 架构 | MVVM, Clean Architecture, Repository Pattern |
| 依赖注入 | Hilt |
| 数据库 | Room (Kotlin Coroutines + Flow) |
| 网络 | Retrofit + Moshi + OkHttp |
| 异步 | Kotlin Coroutines + Flow |
| 序列化 | Kotlinx Serialization + Moshi |
| 图片加载 | Coil |
| Markdown | markdown-compose / Markwon |
| 安全 | Android Keystore (AES-GCM) + EncryptedSharedPreferences |

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 🙏 致谢

- 原版 [DomeOS AI Agent WinUI 3](https://github.com/liu7pro/DomeOS-AI-Agent-with-WinUI-3)
- Material 3 Expressive 设计规范
- 所有开源库贡献者