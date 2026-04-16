@echo off
REM Agent CI/CD 验证脚本 (Windows)
REM 用于本地执行CI/CD验证

setlocal enabledelayedexpansion

echo ==========================================
echo Agent CI/CD 本地验证
echo ==========================================

set FAILED=0

REM 检查Java
echo [INFO] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java未安装
    exit /b 1
)
echo [INFO] Java检查通过

REM 编译
echo [INFO] 编译项目...
call mvn -B compile -q
if errorlevel 1 (
    echo [ERROR] 编译失败
    set FAILED=1
    goto :end
)
echo [INFO] 编译完成

REM 运行测试
echo [INFO] 运行单元测试...
call mvn -B test -pl chat-agent ^
    -Dtest=TraceSystemTest,MetricsSystemTest ^
    -Dspring.profiles.active=test
if errorlevel 1 (
    echo [ERROR] 单元测试失败
    set FAILED=1
    goto :end
)
echo [INFO] 单元测试通过

:end
if %FAILED%==0 (
    echo ==========================================
    echo [INFO] CI/CD 本地验证完成!
    echo ==========================================
) else (
    echo ==========================================
    echo [ERROR] CI/CD 本地验证失败
    echo ==========================================
)

endlocal
exit /b %FAILED%
