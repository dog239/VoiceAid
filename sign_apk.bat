@echo off

rem 复制并重命名APK文件的脚本

set SOURCE_APK=app-release-signed.apk
set OUTPUT_APK=voiceaid-new-%date:~0,4%%date:~5,2%%date:~8,2%.apk

echo 正在生成新的APK文件...
echo 源文件: %SOURCE_APK%
echo 目标文件: %OUTPUT_APK%

rem 检查源文件是否存在
if not exist "%SOURCE_APK%" (
    echo 错误：源APK文件不存在！
    pause
    exit /b 1
)

rem 复制文件
copy "%SOURCE_APK%" "%OUTPUT_APK%"

rem 验证复制是否成功
if %errorlevel% equ 0 (
    echo 操作完成！
    echo 新的APK文件: %OUTPUT_APK%
    echo 请将此文件复制到手机上并安装。
    
    echo 提示：如果您的手机提示"安装包无效或损坏"，请在手机设置中开启"允许安装来自未知来源的应用"选项。
) else (
    echo 操作失败！请检查错误信息。
)

pause