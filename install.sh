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
    echo -e "${YELLOW}[WARN] 系统中未检测到 Docker！${NC}"
    read -p "是否要自动安装 Docker？(y/n): " install_docker
    if [[ "$install_docker" =~ ^[Yy]$ ]]; then
        echo -e "正在尝试从官方源下载安装脚本..."
        # 避免管道屏蔽退出状态码，先下载文件再执行
        if curl -fsSL --connect-timeout 8 https://get.docker.com -o get-docker.sh; then
            echo -e "正在执行官方一键安装脚本 (可能需要 sudo 权限)..."
            if sudo sh get-docker.sh; then
                echo -e "${GREEN}[OK] Docker 官方脚本安装成功！${NC}"
                rm -f get-docker.sh
                echo -e "正在尝试启动并启用 Docker 服务..."
                sudo systemctl enable --now docker
            else
                rm -f get-docker.sh
                echo -e "${RED}[ERROR] 官方 Docker 安装脚本执行失败，请手动检查系统兼容性。${NC}"
                exit 1
            fi
        else
            rm -f get-docker.sh
            echo -e "${YELLOW}[WARN] 官方一键脚本下载失败（可能由于国内网络环境导致 Connection Reset）${NC}"
            echo -e "正在尝试通过国内极速镜像源进行自动安装..."
            if curl -fsSL --connect-timeout 8 https://linuxmirrors.cn/docker.sh -o get-docker-cn.sh; then
                echo -e "正在执行国内镜像源安装脚本，请根据界面提示交互式选择（推荐选择阿里云/腾讯云源，并配置加速器）..."
                if sudo bash get-docker-cn.sh; then
                    echo -e "${GREEN}[OK] Docker 通过国内镜像源安装成功！${NC}"
                    rm -f get-docker-cn.sh
                    echo -e "正在尝试启动并启用 Docker 服务..."
                    sudo systemctl enable --now docker
                else
                    rm -f get-docker-cn.sh
                    echo -e "${RED}[ERROR] 国内镜像源 Docker 安装脚本执行失败，请手动安装后重试。${NC}"
                    exit 1
                fi
            else
                rm -f get-docker-cn.sh
                echo -e "${RED}[ERROR] Docker 自动安装失败（官方与国内镜像均无法连接），请检查网络连接并手动安装。${NC}"
                exit 1
            fi
        fi
    else
        echo -e "${RED}[ERROR] 已取消自动安装。请先安装 Docker 后重新运行此脚本。${NC}"
        exit 1
    fi
else
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}[OK] 检测到 Docker 已安装: ${DOCKER_VERSION}${NC}"
fi

# 2. 检查 Docker 服务是否正在运行
if ! docker info &> /dev/null; then
    echo -e "${YELLOW}[WARN] 检测到 Docker 服务未启动或当前用户无权限访问，正在尝试启动...${NC}"
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
    echo -e "${YELLOW}[WARN] 系统中未检测到 docker compose 插件或 docker-compose 命令！${NC}"
    read -p "是否要自动下载并安装独立版 docker-compose？(y/n): " install_compose
    if [[ "$install_compose" =~ ^[Yy]$ ]]; then
        COMPOSE_URL="https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)"
        COMPOSE_CN_URL="https://mirror.ghproxy.com/${COMPOSE_URL}"
        
        echo -e "正在从 GitHub 官方下载最新独立版 docker-compose..."
        if sudo curl -L --connect-timeout 8 "$COMPOSE_URL" -o /usr/local/bin/docker-compose; then
            sudo chmod +x /usr/local/bin/docker-compose
            COMPOSE_CMD="docker-compose"
            echo -e "${GREEN}[OK] docker-compose 下载并安装成功！${NC}"
        else
            echo -e "${YELLOW}[WARN] 官方 Github 地址下载失败，正在尝试通过国内加速代理下载...${NC}"
            if sudo curl -L --connect-timeout 10 "$COMPOSE_CN_URL" -o /usr/local/bin/docker-compose; then
                sudo chmod +x /usr/local/bin/docker-compose
                COMPOSE_CMD="docker-compose"
                echo -e "${GREEN}[OK] docker-compose 通过加速代理下载并安装成功！${NC}"
            else
                echo -e "${RED}[ERROR] docker-compose 自动下载失败（包含加速代理尝试）！请手动安装后重试。${NC}"
                exit 1
            fi
        fi
    else
        echo -e "${RED}[ERROR] 已取消自动安装。请先手动安装 docker-compose 后重新运行此脚本。${NC}"
        exit 1
    fi
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
