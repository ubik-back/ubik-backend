#!/bin/bash

# Script de verificación del sistema Docker

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🔍 Verificando Sistema Docker para Motel Management"
echo "===================================================="
echo ""

# Función para verificar un requisito
check_requirement() {
    local name=$1
    local command=$2
    local suggestion=$3
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $name está instalado${NC}"
        return 0
    else
        echo -e "${RED}❌ $name NO está instalado${NC}"
        if [ -n "$suggestion" ]; then
            echo -e "${YELLOW}   💡 $suggestion${NC}"
        fi
        return 1
    fi
}

# Verificar requisitos
echo "📋 Requisitos del Sistema:"
echo ""

all_ok=true

check_requirement "Docker" "docker --version" "Instala Docker Desktop desde https://www.docker.com/products/docker-desktop/" || all_ok=false

check_requirement "Docker Compose" "docker-compose --version" "Viene con Docker Desktop" || all_ok=false

# Verificar que Docker esté corriendo
if docker info > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Docker está corriendo${NC}"
else
    echo -e "${RED}❌ Docker NO está corriendo${NC}"
    echo -e "${YELLOW}   💡 Inicia Docker Desktop${NC}"
    all_ok=false
fi

# Verificar archivos necesarios
echo ""
echo "📁 Archivos de Configuración:"
echo ""

check_file() {
    local file=$1
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ $file existe${NC}"
        return 0
    else
        echo -e "${RED}❌ $file NO existe${NC}"
        return 1
    fi
}

check_file "docker-compose.yml" || all_ok=false
check_file "docker-compose-full.yml" || all_ok=false
check_file "Dockerfile" || all_ok=false
check_file ".env" || all_ok=false
check_file "src/main/resources/Postgres-init-motel.sql" || all_ok=false
check_file "pom.xml" || all_ok=false

# Verificar scripts
echo ""
echo "🔧 Scripts de Ejecución:"
echo ""

check_file "start-database.sh" || all_ok=false
check_file "start-all.sh" || all_ok=false
check_file "stop-services.sh" || all_ok=false

# Verificar que los scripts sean ejecutables
if [ -x "start-database.sh" ]; then
    echo -e "${GREEN}✅ Los scripts tienen permisos de ejecución${NC}"
else
    echo -e "${YELLOW}⚠️  Los scripts NO son ejecutables${NC}"
    echo -e "${YELLOW}   💡 Ejecuta: chmod +x *.sh${NC}"
fi

# Verificar puertos disponibles
echo ""
echo "🔌 Puertos Disponibles:"
echo ""

check_port() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${YELLOW}⚠️  Puerto $port ya está en uso ($service)${NC}"
        echo -e "${YELLOW}   💡 Ejecuta: lsof -i :$port${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Puerto $port disponible ($service)${NC}"
        return 0
    fi
}

check_port 5432 "PostgreSQL"
check_port 8083 "Motel App"
check_port 8081 "Adminer"

# Estado de contenedores
echo ""
echo "🐳 Estado de Contenedores Docker:"
echo ""

if docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -q motel; then
    echo "Contenedores relacionados con el proyecto:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep motel || echo "Ninguno"
else
    echo "No hay contenedores corriendo del proyecto"
fi

# Verificar imágenes
echo ""
echo "🖼️  Imágenes Docker:"
echo ""

if docker images | grep -q "motel\|postgres"; then
    echo "Imágenes disponibles:"
    docker images | grep "motel\|postgres" || echo "Ninguna"
else
    echo "No hay imágenes del proyecto descargadas"
fi

# Verificar volúmenes
echo ""
echo "💾 Volúmenes de Datos:"
echo ""

if docker volume ls | grep -q "postgres-data"; then
    echo -e "${GREEN}✅ Volumen postgres-data existe${NC}"
    
    # Mostrar tamaño del volumen
    size=$(docker system df -v | grep "postgres-data" | awk '{print $3}' || echo "N/A")
    echo "   Tamaño: $size"
else
    echo "No hay volúmenes creados (se crearán al iniciar)"
fi

# Resumen final
echo ""
echo "===================================================="

if [ "$all_ok" = true ]; then
    echo -e "${GREEN}✅ ¡Todo listo! Puedes iniciar el sistema${NC}"
    echo ""
    echo "🚀 Comandos para iniciar:"
    echo "   ./start-database.sh    - Solo base de datos"
    echo "   ./start-all.sh         - Sistema completo"
else
    echo -e "${RED}❌ Hay algunos problemas que debes resolver${NC}"
    echo ""
    echo "📖 Revisa el README-DOCKER.md para más información"
fi

echo "===================================================="
echo ""