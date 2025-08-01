@echo off
echo 正在编译和运行配置中心服务...

REM 检查Java版本
java -version
if %errorlevel% neq 0 (
    echo 错误：未找到Java，请确保Java已安装并配置到PATH中
    pause
    exit /b 1
)

REM 检查Maven版本
mvn -version
if %errorlevel% neq 0 (
    echo 错误：未找到Maven，请确保Maven已安装并配置到PATH中
    pause
    exit /b 1
)

echo.
echo 正在清理项目...
mvn clean

echo.
echo 正在编译项目...
mvn compile

if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo 编译成功，正在启动应用...
mvn spring-boot:run

pause 