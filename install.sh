#!/bin/bash

# =============================================================================
# DataPipeline 一键安装与部署脚本 (Linux 环境)
# =============================================================================

# 颜色控制字符
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=============================================================================${NC}"
echo -e "${BLUE}                   DataPipeline 数据管道管理平台一键部署脚本                   ${NC}"
echo -e "${BLUE}=============================================================================${NC}"

# 1. 检查 Docker 是否安装
echo -e "正在检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[ERROR] 系统中未检测到 Docker，请先安装 Docker！${NC}"
    echo -e "您可以参考以下命令进行快速安装（以 Ubuntu/CentOS 为例）："
    echo -e "  curl -fsSL https://get.docker.com | bash -s docker"
    echo -e "  systemctl enable --now docker"
    exit 1
else
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}[OK] 检测到 Docker 已安装: ${DOCKER_VERSION}${NC}"
fi

# 2. 检查 Docker 服务是否正在运行
if ! docker info &> /dev/null; then
    echo -e "${YELLOW}[WARN] 检测到 Docker 服务未启动或无权限访问，正在尝试启动...${NC}"
    sudo systemctl start docker || {
        echo -e "${RED}[ERROR] Docker 服务启动失败！请手动执行 'systemctl start docker' 后重新运行此脚本。${NC}"
        exit 1
    }
fi

# 3. 检查 Docker Compose 是否安装 (自动检测 docker compose 插件或独立版 docker-compose)
COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
    echo -e "${GREEN}[OK] 检测到 Docker Compose v2 插件已集成安装${NC}"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
    echo -e "${GREEN}[OK] 检测到独立版 docker-compose 已安装${NC}"
else
    echo -e "${RED}[ERROR] 系统中未检测到 docker compose 插件或 docker-compose 命令！${NC}"
    echo -e "您可以参考以下命令安装 docker-compose-plugin（以 Ubuntu 为例）："
    echo -e "  sudo apt-get update && sudo apt-get install docker-compose-plugin"
    echo -e "  或者参考 Docker 官方文档进行安装。"
    exit 1
fi

# 4. 执行 Docker 命令构建并启动本应用程序
echo -e "\n${BLUE}=============================================================================${NC}"
echo -e " 正在使用 '${COMPOSE_CMD}' 构建并启动本应用程序，首次拉取镜像及编译约耗时 2-5 分钟..."
echo -e "${BLUE}=============================================================================${NC}\n"

# 运行构建启动命令
if $COMPOSE_CMD up -d --build; then
    echo -e "\n${GREEN}[SUCCESS] DataPipeline 应用程序部署成功！${NC}"
    echo -e "访问地址："
    echo -e "  - ${BLUE}前端主页${NC}：http://localhost:80"
    echo -e "  - ${BLUE}后端服务接口${NC}：http://localhost:8080"
    echo -e "默认登录账号："
    echo -e "  - 超级管理员：${YELLOW}admin / admin@123${NC}"
    echo -e "  - 普通用户：${YELLOW}test / test123${NC}"
    echo -e "\n您可以通过运行以下命令来查看容器实时日志："
    echo -e "  $COMPOSE_CMD logs -f"
else
    echo -e "\n${RED}[ERROR] 部署失败，请检查上方控制台编译与启动日志！${NC}"
    exit 1
fi
