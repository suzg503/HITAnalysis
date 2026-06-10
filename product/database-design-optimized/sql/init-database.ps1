# =====================================================
# HITAnalysis Database Initialization Script
# Version: 1.0.0
# Date: 2026-05-11
# Description: Initialize MySQL database for HITAnalysis (Windows PowerShell)
# =====================================================

# Configuration
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_USER = "root"
$DB_NAME = "bi_db"
$SQL_DIR = $PSScriptRoot

# Function to print colored messages
function Print-Info {
    Write-Host "[INFO] $args" -ForegroundColor Green
}

function Print-Warn {
    Write-Host "[WARN] $args" -ForegroundColor Yellow
}

function Print-Error {
    Write-Host "[ERROR] $args" -ForegroundColor Red
}

# Check MySQL command availability
Print-Info "Checking MySQL availability..."
try {
    $mysqlVersion = mysql --version 2>&1
    Print-Info "MySQL found: $mysqlVersion"
} catch {
    Print-Error "MySQL command not found. Please ensure MySQL is installed and mysql.exe is in PATH."
    exit 1
}

# Get MySQL password
Print-Info "Please enter MySQL password for user '$DB_USER':"
$DB_PASSWORD = Read-Host -AsSecureString
$DB_PASSWORDPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD))

# Check MySQL connection
Print-Info "Checking MySQL connection..."
try {
    $connectionTest = mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORDPlain" -e "SELECT 1;" 2>&1
    if ($connectionTest -match "ERROR") {
        Print-Error "Cannot connect to MySQL. Please check your password and connection settings."
        exit 1
    }
    Print-Info "MySQL connection successful."
} catch {
    Print-Error "MySQL connection test failed."
    exit 1
}

# SQL files to execute in order
$SQL_FILES = @(
    "01_create_database.sql",
    "02_create_log_tables.sql",
    "03_create_indicator_tables.sql",
    "04_create_target_tables.sql",
    "05_create_analysis_ai_tables.sql",
    "06_create_summary_tables.sql",
    "07_create_procedures.sql",
    "08_create_views.sql",
    "09_create_triggers.sql",
    "10_init_data.sql"
)

Print-Info "Starting database initialization..."

foreach ($sqlFile in $SQL_FILES) {
    $filePath = Join-Path $SQL_DIR $sqlFile

    if (Test-Path $filePath) {
        Print-Info "Executing: $sqlFile"

        # Execute SQL file (handle password securely)
        $env:MYSQL_PWD = $DB_PASSWORDPlain
        $result = mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "source $filePath" 2>&1
        $env:MYSQL_PWD = ""

        if ($result -match "ERROR") {
            Print-Error "Failed to execute $sqlFile"
            Print-Error $result
            exit 1
        }

        Print-Info "✓ $sqlFile completed successfully"
    } else {
        Print-Error "SQL file not found: $sqlFile"
        Print-Error "Path checked: $filePath"
        exit 1
    }
}

Print-Info "Database initialization completed successfully!"

# Verify database setup
Print-Info "Verifying database setup..."
Write-Host ""

Print-Info "Database tables:"
$env:MYSQL_PWD = $DB_PASSWORDPlain
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW TABLES;"
$env:MYSQL_PWD = ""
Write-Host ""

Print-Info "User count:"
$env:MYSQL_PWD = $DB_PASSWORDPlain
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as user_count FROM sys_user;"
$env:MYSQL_PWD = ""
Write-Host ""

Print-Info "Role count:"
$env:MYSQL_PWD = $DB_PASSWORDPlain
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as role_count FROM sys_role;"
$env:MYSQL_PWD = ""
Write-Host ""

Print-Info "Indicator count:"
$env:MYSQL_PWD = $DB_PASSWORDPlain
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) as indicator_count FROM bi_indicator;"
$env:MYSQL_PWD = ""
Write-Host ""

Print-Info "✓ Database is ready for use!"
Print-Warn "Please update the admin password in sys_user table before starting the application."
Print-Info "Default admin account: username=admin, password needs to be hashed using BCrypt."

Write-Host ""
Write-Host "====================================================="
Print-Info "Initialization complete. You can now start the application."
Write-Host "====================================================="