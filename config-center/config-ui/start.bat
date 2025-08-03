@echo off
echo 正在启动配置中心前端...

REM 检查Node.js版本
node --version
if %errorlevel% neq 0 (
    echo 错误：未找到Node.js，请确保Node.js已安装并配置到PATH中
    pause
    exit /b 1
)

REM 检查npm版本
npm --version
if %errorlevel% neq 0 (
    echo 错误：未找到npm，请确保npm已安装并配置到PATH中
    pause
    exit /b 1
)

echo.
echo 正在安装依赖...
npm install

if %errorlevel% neq 0 (
    echo 依赖安装失败，请检查网络连接
    pause
    exit /b 1
)

echo.
echo 正在启动开发服务器...
npm run dev

pause 