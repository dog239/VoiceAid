已实现并接入的服务器功能（客户端 ↔ 后端对齐）
登录/验证码/注册/改密/注销：/login, /login_captcha, /get_captcha, /register, /change_password, /logoff_user，在 NetInteractUtils 与 LoginActivity/RegisterActivity 中均有调用。
测评上传/更新/获取/分页/ID 列表/删除：/upload_evaluations, /update_evaluation, /get_evaluations_limit, /get_evaluation, /get_evaluation_ids, /delete_evaluations, /delete_evaluation，对应 NetInteractUtils 方法完整。
用户列表与用户信息：/get_uids, /get_user_info，history/allhistorylist.java 实际使用。
模块创建/更新/获取/删除：/create_module, /update_module, /get_module, /delete_module_admin，注册后创建模块已接入。
可能未完全落地或有实现风险的点
登录与注册含“本地调试模式”分支：LoginActivity、RegisterActivity 中存在 useLocalDebug 逻辑，若被改为 true 会绕过服务器流程。当前是 false，但属于潜在风险点（多人协作易被误改）。
管理员验证缺失：客户端调用管理员相关接口仅传 admin/adminUid，后端也未真正校验管理员身份（/delete_evaluations, /delete_user_admin, /delete_module_admin, /get_uids），这在功能上可用，但权限逻辑未实现。
音频上传/获取：/upload_audio, /get_audio 在客户端封装完成，但我未看到明确 UI 入口调用（需要再查 AudioRecorder/AudioPlayer 或测评流程是否实际触发）。
获取全部测评（非分页）已标记 Deprecated：getEvaluations() 仍在客户端，后端也提供 /get_evaluations，但 UI 可能已转向分页接口。
网络明文：network.xml 允许明文流量，服务器地址为 `http://123.57.104.101/voiceaid`，功能可用但不安全。
建议你确认的入口点（是否“已接入 UI”）
音频上传/获取是否在测评流程中实际触发（查 AudioRecorder/AudioPlayer 和测评 Activity）。
管理员相关功能是否有 UI 入口（删除用户、删除模块、获取普通用户列表）。
