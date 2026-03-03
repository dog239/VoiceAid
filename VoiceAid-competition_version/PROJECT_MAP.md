# 1. 项目概览
- 一句话说明: Android 端学前儿童汉语测评应用, 支持测试、录音、结果统计与 PDF 报告。
- 关键用户流程: `login/Loginactivity` 登录 -> `com/example/CCLEvaluation/startActivity` 选择模式 -> `com/example/CCLEvaluation/mainactivity` 菜单 -> `com/example/CCLEvaluation/childinfoactivity` 录入被测信息 -> `com/example/CCLEvaluation/evmenuactivity` 进入各测评模块 -> `com/example/CCLEvaluation/testactivity` 录音作答 -> `com/example/CCLEvaluation/resultactivity`/`com/example/CCLEvaluation/wordtest4result` 查看结果 -> `utils/Netinteractutils` 上传/下载 -> `history/*` 历史记录 -> `com/example/CCLEvaluation/PdfGenerator` 生成报告。
- 技术栈与主要依赖: Java + XML 视图(`app/src/main/res/layout/*`), AppCompat/Material/ConstraintLayout/RecyclerView, OkHttp 4.8.1, iText 5 + itext-asian(报告生成), calligraphy(字体), MultiDex; compileSdk/targetSdk 34, minSdk 20 (`app/build.gradle.kts`).

# 2. 目录结构与模块职责
- 目录树(深度 3~4):
```text
.
├─ app/
│  ├─ build.gradle.kts
│  └─ src/
│     ├─ main/
│     │  ├─ AndroidManifest.xml
│     │  ├─ assets/
│     │  │  └─ fonts/
│     │  ├─ java/
│     │  │  ├─ adapter/
│     │  │  ├─ audiotest/
│     │  │  ├─ bean/
│     │  │  ├─ com/example/CCLEvaluation/
│     │  │  ├─ history/
│     │  │  ├─ login/
│     │  │  └─ utils/
│     │  └─ res/
│     │     ├─ drawable/
│     │     ├─ layout/
│     │     ├─ values/
│     │     └─ xml/
│     ├─ androidTest/
│     └─ test/
├─ build.gradle.kts
├─ settings.gradle.kts
└─ gradle/wrapper/
```
- 模块/包职责概览:
- `app/src/main/java/com/example/CCLEvaluation/*`: 核心 Activity 与业务编排(入口菜单、测评流程、结果页、PDF 生成等)。
- `app/src/main/java/login/*`: 登录/注册流程与 SharedPreferences 登录态 (`login/Loginactivity`).
- `app/src/main/java/history/*`: 历史记录列表、详情展示与删除(本地与服务端记录)。
- `app/src/main/java/audiotest/*`: 录音自检与播放(`audiotest/Audiocheck`, `audiotest/Audiocheckresult`).
- `app/src/main/java/utils/*`: 网络(`Netinteractutils`)、录音(`AudioRecorder`/`AudioPlayer`)、存储(`dataManager`/`sdcard`/`dirpath`)、权限(`permissionutils`)、题库(`ImageUrls`)等。
- `app/src/main/java/bean/*`: 测评题目与结果数据模型(如 `bean/a`, `bean/e`, `bean/nwr` 等)。
- `app/src/main/res/layout/*`: 各页面布局(登录、首页菜单、测评、结果、历史等)。
- `app/src/main/res/xml/*`: network security config(`xml/network.xml`), FileProvider(`xml/file_paths.xml`)。

# 3. 入口与导航
- Launcher 入口: `login/Loginactivity` 在 `app/src/main/AndroidManifest.xml` 中声明为 `MAIN/LAUNCHER`。
- 登录与主页面跳转: `login/Loginactivity` 成功后跳到 `com/example/CCLEvaluation/startActivity`, 再由 `startActivity` 进入 `com/example/CCLEvaluation/mainactivity` (携带 `isTest`/`Uid`).
- 菜单路由关系:
- `mainactivity` 根据 `isTest` 展示不同菜单并路由到 `childinfoactivity`(新测评)、`history/historylist`(本地历史)、`history/privatehistorylist`(云端历史)、`history/allhistorylist`(管理员全量用户)、`audiotest/Audiocheck`(录音自检)、`choosemoduleactivity`(模块配置)、`deleteactivity`(账户/数据删除)。
- `evmenuactivity` 作为测评功能入口, 根据按钮跳转到 `testactivity` 或结果页(见 `com/example/CCLEvaluation/evmenuactivity`).
- 各模块路由方式: 纯 Activity + `Intent` 跳转; 未发现 Fragment/Compose Navigation。
- 重要 UI/菜单入口实现位置: `app/src/main/res/layout/activity_login.xml`, `activity_start.xml`, `activity_main.xml`, `activity_ev_menu.xml`, `activity_test.xml`, `activity_result11.xml`, `history_list.xml`, `word_test4_result.xml`, `audio_check.xml`, `audio_check_result.xml`。

# 4. 测评模块概览
- 模块入口与结果页:
- 词汇类: E/RE/S/NWR -> `evmenuactivity` -> `testactivity` (`format` 传 E/RE/S/NWR) -> `resultactivity` 或 `wordtest4result`(NWR 结果页)。
- 发音/构音: A -> `evmenuactivity` -> `testactivity` -> `resultactivity`。
- 语法: RG -> `evmenuactivity` -> `testactivity` -> `resultactivity`。
- 叙述: PST/PN -> `evmenuactivity` -> `testactivity` -> `resultactivity`。
- 核心数据结构:
- 本地存储 JSON 结构由 `utils/dataManager.createData` 生成: `{ info: {...}, evaluations: { A:[], E:[], NWR:[], PN:[], PST:[], RE:[], RG:[], S:[] } }`。
- 每个模块条目通常包含 `num`, `result`, `time`, `audioPath` 以及模块特有字段(如 A 的 `target_tone1/2`, E/S 的 `target/answer` 等), 具体实现分散在 `app/src/main/java/bean/*`.
- ViewModel/UseCase: 未发现; 主要由 Activity + utils 单例驱动(`testactivity`, `utils/testcontext`).

# 5. 音频录制与权限
- 录音实现:
- `utils/AudioRecorder` 使用 `MediaRecorder`(AMR_WB) 录音, 文件名 `yyyyMMdd_HH_mm_ss.amr`, 输出目录来自 `dirpath.PATH_FETCH_DIR_AUDIO`。
- `audiotest/Audiocheck` 使用 `MediaRecorder` 自检录音, 文件固定为 `.../test/check.amr`。
- 音频播放: `utils/AudioPlayer` 基于 `MediaPlayer`。
- 存储路径:
- 统一目录由 `utils/sdcard` 创建: `getExternalFilesDir(Ifileinter.APP_DIR)` 下的 `info/` 与 `audio/` (`utils/Ifileinter`).
- 运行时权限与异常处理:
- Manifest 中包含 `RECORD_AUDIO`, `READ/WRITE/MANAGE_EXTERNAL_STORAGE`, `INTERNET` (`app/src/main/AndroidManifest.xml`).
- `startActivity` 通过 `permissionutils` 申请 `RECORD_AUDIO`; `evmenuactivity` 在导出 PDF 前请求读写存储权限。
- Android 10+ 的 scoped storage 与 `MANAGE_EXTERNAL_STORAGE`/`requestLegacyExternalStorage` 可能影响存储可用性(目标 SDK 34)。

# 6. 数据存储与历史记录
- 本地存储: JSON + 文件系统, 由 `utils/dataManager` 负责(`saveData/loadData/deleteData`, `saveAudioFile/deleteAudioFile`)。
- 索引文件: `Index.json` 由 `dataManager.createIndex` 维护, `history/historylist` 读取展示本地历史。
- 历史记录入口:
- 本地: `history/historylist` -> `history/showchildinformation`.
- 云端: `history/privatehistorylist` 通过 `Netinteractutils.getEvaluationIDs/getEvaluations` 拉取并保存本地, 再进入 `history/showprivatechildinformation`。
- 管理员: `history/allhistorylist` 拉用户列表再进入 `privatehistorylist`。
- 关键数据字段(来自 `utils/dataManager` + `bean/*`):
- `info`: `name`, `class`, `serialNumber`, `birthDate`, `testDate`, `testLocation`, `examiner`.
- `evaluations`: 每题 `num` + `result`/`time`/`audioPath` + 模块字段.

# 7. 上传与下载
- 网络层结构: `utils/Netinteractutils` 封装 OkHttp 请求, URL 基地址硬编码为 `http://49.233.107.121/ccle`, cleartext 放行配置在 `app/src/main/res/xml/network.xml`.
- 上传流程:
- `evmenuactivity` -> `Netinteractutils.uploadEvaluation`(POST `uid` + `childUser` JSON) -> 回调 `UploadEvaluationCallback` -> `uploadAudioInParallel` 并行上传每题音频(`uploadAudio` multipart, `audio/amr`).
- 下载流程:
- `privatehistorylist` 拉取测评记录并保存为本地 JSON.
- `evmenuactivity` 通过 `Netinteractutils.getAudio` 获取 Base64 音频, 在 `audioCallback` 中解码并写入 `dirpath.PATH_FETCH_DIR_AUDIO`.
- 错误处理/进度: 主要靠 Toast 与 `UiRefreshListener`(加载动画), 未看到统一重试或失败回滚。

# 8. PDF 报告生成
- 入口: `evmenuactivity` 点击 `btn_pdf` -> `createPDFWithSAF` -> `writePDF` -> `PdfGenerator.generatePdf`。
- 渲染实现: iText 5 (`com.itextpdf:itextpdf`, `itext-asian`), 字体来自 `assets/fonts/songsim.ttf` (`PdfGenerator` 内 `FontFactory.getFont`).
- 数据来源与组装: 从 `dataManager.loadData` 读取 JSON, 计算各模块得分(A/E/RE/S/NWR/RG/PN/PST)并填表(`PdfGenerator.generatePdf`).
- 输出路径与分享: 使用 SAF(`Intent.ACTION_CREATE_DOCUMENT`) 让用户选择导出位置; 代码中 `openPdfFile` 存在但未见调用。

# 9. 构建、运行与打包
- 运行 Debug: Android Studio 直接运行 `app` 模块即可(`settings.gradle.kts`, `app/build.gradle.kts`).
- Release/APK/AAB: 未在 README 指明; Gradle 任务可用 `./gradlew assembleRelease` 或 `./gradlew bundleRelease`.
- 重要构建配置: `compileSdk/targetSdk 34`, `minSdk 20`, `multiDexEnabled true`, `release` 不混淆(`isMinifyEnabled = false`), Java 8 (`app/build.gradle.kts`).

# 10. 新版本改动建议与风险点
- 高概率改动点(路径 + 原因):
- `app/src/main/java/login/Loginactivity`: 当前默认走本地调试登录, 真实登录逻辑被注释.
- `app/src/main/java/utils/Netinteractutils`: 接口地址硬编码、明文 HTTP、回调式 API; 新版本若有鉴权/域名变更需重构.
- `app/src/main/java/com/example/CCLEvaluation/evmenuactivity`: 模块入口/上传/下载/PDF 入口集中, 业务耦合重.
- `app/src/main/java/utils/dataManager`: JSON 结构与索引文件(Index.json)决定数据兼容性.
- `app/src/main/java/utils/AudioRecorder` + `audiotest/*`: 录音格式与路径策略需统一.
- `app/src/main/AndroidManifest.xml`: 存储权限/`requestLegacyExternalStorage` 对 Android 13+ 兼容性有风险.
- 耦合/硬编码/潜在坑:
- `Netinteractutils` 保存 Activity context 的单例容易内存泄漏.
- `uploadAudioInParallel` 未等待上传完成, UI 直接跳回主页面, 失败不可见.
- 文件存储依赖 `startActivity` 初始化 `dirpath.*`; 若绕过该入口可能为 null.
- `MANAGE_EXTERNAL_STORAGE` + `READ/WRITE_EXTERNAL_STORAGE` 在 targetSdk 34 下受限; 某些写入可能失败.
- `PdfGenerator` 继承 `evmenuactivity` 且持有静态 `context`, 耦合高.
- 推荐改造顺序(低风险路径):
- 1) 固化数据模型与存储(`dataManager`/JSON schema/Index.json)并补充迁移策略.
- 2) 统一录音与路径管理(`AudioRecorder`, `audiotest`, `dirpath`, `sdcard`).
- 3) 规范网络层(域名配置/TLS/鉴权/错误处理), 再拆分上传与下载流程.
- 4) 拆分 `evmenuactivity` 业务(模块入口、上传下载、PDF)为独立组件.
- 5) 更新 PDF 生成与字体策略(避免静态 context + Activity 继承).
