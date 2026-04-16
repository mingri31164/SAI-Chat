#!/bin/bash
# Agent CI/CD 验证脚本
# 用于本地执行CI/CD验证

set -e

echo "=========================================="
echo "Agent CI/CD 本地验证"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数定义
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java版本
check_java() {
    log_info "检查Java环境..."
    if ! command -v java &> /dev/null; then
        log_error "Java未安装"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java版本需要17+，当前版本: $JAVA_VERSION"
        exit 1
    fi
    log_info "Java版本检查通过"
}

# 检查Maven
check_maven() {
    log_info "检查Maven环境..."
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装"
        exit 1
    fi
    log_info "Maven检查通过"
}

# 编译项目
compile_project() {
    log_info "编译项目..."
    mvn -B compile -q
    log_info "编译完成"
}

# 运行单元测试
run_unit_tests() {
    log_info "运行单元测试..."
    mvn -B test -pl chat-agent \
        -Dtest=TraceSystemTest,MetricsSystemTest \
        -Dspring.profiles.active=test

    if [ $? -eq 0 ]; then
        log_info "单元测试通过"
    else
        log_error "单元测试失败"
        exit 1
    fi
}

# 运行集成测试
run_integration_tests() {
    log_info "运行集成测试..."
    mvn -B verify -pl chat-agent \
        -Dspring.profiles.active=test \
        -DskipITs=false

    if [ $? -eq 0 ]; then
        log_info "集成测试通过"
    else
        log_warn "集成测试失败(可能需要额外服务)"
    fi
}

# 运行覆盖率
run_coverage() {
    log_info "生成代码覆盖率报告..."
    mvn -B test -pl chat-agent \
        -Djacoco.skip=false \
        -Djacoco.destFile=chat-agent/target/jacoco.exec

    if [ -f chat-agent/target/site/jacoco/index.html ]; then
        log_info "覆盖率报告已生成: chat-agent/target/site/jacoco/index.html"
    fi
}

# 生成测试报告
generate_report() {
    log_info "生成测试报告..."
    if [ -d chat-agent/target/surefire-reports ]; then
        PASSED=$(grep -c 'testcase.*time' chat-agent/target/surefire-reports/*.xml 2>/dev/null || echo 0)
        FAILED=$(grep -c 'failures="[1-9]' chat-agent/target/surefire-reports/*.xml 2>/dev/null || echo 0)
        echo "=========================================="
        echo "测试结果摘要"
        echo "=========================================="
        echo "通过: $PASSED"
        echo "失败: $FAILED"
        echo "=========================================="
    fi
}

# 主流程
main() {
    check_java
    check_maven
    compile_project
    run_unit_tests
    # run_integration_tests  # 可选，需要额外服务
    # run_coverage  # 可选
    generate_report

    log_info "=========================================="
    log_info "CI/CD 本地验证完成!"
    log_info "=========================================="
}

# 执行
main "$@"
