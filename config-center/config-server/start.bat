@echo off
echo 启动配置中心服务...
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java，请先安装JDK 8或更高版本
    pause
    exit /b 1
)

REM 检查Maven是否安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven，请先安装Maven 3.6或更高版本
    pause
    exit /b 1
)

echo 正在编译项目...
mvn clean compile

if errorlevel 1 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo 启动应用...
mvn spring-boot:run

pause 