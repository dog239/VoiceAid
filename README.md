# VoiceAid

VoiceAid 是一款用于学龄前儿童语言能力评估的 Android 应用，面向测评场景提供录音采集、发音评估、错误类型判定、结果展示与可追踪的训练建议等能力。

## 一、主要测评模块

- 构音能力
- 前语言能力
- 词汇能力
- 句法能力
- 社交能力

## 二、技术栈与架构

### 移动端开发与架构

- 开发语言：Java
- UI 框架：Android 原生（XML 布局、Material Design Components、AppCompat）
- 架构模式：MVVM（Model-View-ViewModel）
- 核心组件：Activity/Fragment 生命周期管理、RecyclerView、ConstraintLayout
- 兼容性支持：MultiDex（解决方法数超限）、Calligraphy（自定义字体）

### 音频采集与处理

- 采集接口：Android MediaRecorder / AudioRecord
- 编码格式：AMR-WB（保留 50 Hz-7 kHz 关键频带）
- 播放引擎：MediaPlayer（录音回放、示范音播放）

### AI 与智能决策

- 语音评测引擎：科大讯飞 ISE SDK（提取声学置信度）
- 构音错误判别：SADA 决策树（替代/省略/增加/扭曲）+ 字符匹配度双轨映射
- 大语言模型：DeepSeek API（报告润色与干预建议生成）
- 检索增强生成（RAG）：向量数据库构建言语康复知识库，约束大模型输出，降低幻觉
- 轻量化协同推理：小模型（轻量级 BERT 变体，负责结构化预处理与模板填充）+ 大模型（语义润色）的流式处理

### 数据持久化与文档生成

- 本地存储：JSON 结构化数据（测评记录、题目元数据），Android 文件 I/O（分区存储适配）
- 云端数据库：SQLite（用户信息、历史记录备份）
- PDF 生成：iText 5 + iText-Asian（中文字体嵌入），动态渲染表格、图表与干预建议

### 网络通信与后端服务

- HTTP 客户端：OkHttp（连接池、MultipartBody 分片上传、异步 Call/Callback）
- 后端框架：Python FastAPI（异步 I/O、高并发处理）
- 数据同步：RESTful API，端云异构备份（本地 JSON + 云端 SQLite）
- 注：后端代码不包含在本项目中
