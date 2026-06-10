#!/bin/bash

# =====================================================
# HITAnalysis Database Initialization Script
# Version: 1.0.0
# Date: 2026-05-11
# Description: Initialize MySQL database for HITAnalysis
# =====================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_NAME="bi_db"
SQL_DIR="./"

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check MySQL connection
print_info "Checking MySQL connection..."
if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p -e "SELECT 1;" &>/dev/null; then
    print_error "Cannot connect to MySQL. Please check your connection settings."
    exit 1
fi
print_info "MySQL connection successful."

# Execute SQL files in order
SQL_FILES=(
    "01_create_database.sql"
    "02_create_log_tables.sql"
    "03_create_indicator_tables.sql"
    "04_create_target_tables.sql"
    "05_create_analysis_ai_tables.sql"
    "06_create_summary_tables.sql"
    "07_create_procedures.sql"
    "08_create_views.sql"
    "09_create_triggers.sql"
    "10_init_data.sql"
)

print_info "Starting database initialization..."

for sql_file in "${SQL_FILES[@]}"; do
    if [ -f "$SQL_DIR/$sql_file" ]; then
        print_info "Executing: $sql_file"
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" < "$SQL_DIR/$sql_file"
        print_info "✓ $sql_file completed successfully"
    else
        print_error "SQL file not found: $sql_file"
        exit 1
    fi
done

print_info "Database initialization completed successfully!"

# Verify database setup
print_info "Verifying database setup..."
echo ""
echo "Database tables:"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" -e "SHOW TABLES;"
echo ""

echo "User count:"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" -e "SELECT COUNT(*) as user_count FROM sys_user;"
echo ""

echo "Role count:"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" -e "SELECT COUNT(*) as role_count FROM sys_role;"
echo ""

echo "Indicator count:"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p "$DB_NAME" -e "SELECT COUNT(*) as indicator_count FROM bi_indicator;"
echo ""

print_info "✓ Database is ready for use!"
print_warn "Please update the admin password in sys_user table before starting the application."
print_info "Default admin account: username=admin, password needs to be hashed using BCrypt."

echo ""
echo "====================================================="
print_info "Initialization complete. You can now start the application."
echo "====================================================="