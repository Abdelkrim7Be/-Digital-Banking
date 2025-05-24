# MySQL Setup Guide for Digital Banking Application

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0 or higher

## 🗄 MySQL Installation

### Windows
1. Download MySQL Installer from [MySQL Official Website](https://dev.mysql.com/downloads/installer/)
2. Run the installer and choose "Developer Default"
3. Follow the installation wizard
4. Set root password during installation

### macOS
```bash
# Using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql

# Set root password
mysql_secure_installation
```

### Linux (Ubuntu/Debian)
```bash
# Update package index
sudo apt update

# Install MySQL Server
sudo apt install mysql-server

# Secure MySQL installation
sudo mysql_secure_installation

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql
```

## 🔧 Database Configuration

### 1. Create Database (Optional)
The application will create the database automatically, but you can create it manually:

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE digital_banking;

-- Create a dedicated user (optional)
CREATE USER 'banking_user'@'localhost' IDENTIFIED BY 'banking_password';
GRANT ALL PRIVILEGES ON digital_banking.* TO 'banking_user'@'localhost';
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

### 2. Update Application Configuration

Edit `src/main/resources/application.properties`:

```properties
# Replace 'your_password' with your actual MySQL root password
spring.datasource.password=your_actual_password

# If you created a dedicated user, update these:
# spring.datasource.username=banking_user
# spring.datasource.password=banking_password
```

## 🚀 Running the Application

### 1. Verify MySQL is Running
```bash
# Check MySQL service status
# Windows: Check Services or Task Manager
# macOS: brew services list | grep mysql
# Linux: sudo systemctl status mysql
```

### 2. Test Database Connection
```bash
# Test connection to MySQL
mysql -u root -p -e "SELECT VERSION();"
```

### 3. Start the Application
```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 4. Verify Application Started
- Check console output for successful startup
- Access Swagger UI: `http://localhost:8085/swagger-ui.html`
- Check database tables were created:
  ```sql
  mysql -u root -p
  USE digital_banking;
  SHOW TABLES;
  ```

## 🔍 Troubleshooting

### Common Issues

#### 1. Connection Refused
**Error:** `Connection refused` or `Communications link failure`

**Solutions:**
- Verify MySQL service is running
- Check if MySQL is listening on port 3306: `netstat -an | grep 3306`
- Restart MySQL service

#### 2. Access Denied
**Error:** `Access denied for user 'root'@'localhost'`

**Solutions:**
- Verify password is correct
- Reset root password if needed:
  ```bash
  # Stop MySQL service
  # Start MySQL in safe mode
  sudo mysqld_safe --skip-grant-tables &
  
  # Connect without password
  mysql -u root
  
  # Reset password
  USE mysql;
  UPDATE user SET authentication_string=PASSWORD('new_password') WHERE User='root';
  FLUSH PRIVILEGES;
  EXIT;
  
  # Restart MySQL normally
  ```

#### 3. Database Creation Failed
**Error:** Database `digital_banking` doesn't exist

**Solutions:**
- Ensure `createDatabaseIfNotExist=true` is in the connection URL
- Create database manually (see step 1 above)
- Check user permissions

#### 4. Table Creation Issues
**Error:** Table creation or schema issues

**Solutions:**
- Check `spring.jpa.hibernate.ddl-auto=update` in properties
- Verify MySQL user has CREATE privileges
- Check application logs for detailed error messages

### Verification Commands

```bash
# Check MySQL version
mysql --version

# Check if MySQL is running
# Windows: tasklist | findstr mysql
# macOS/Linux: ps aux | grep mysql

# Test database connection
mysql -u root -p -e "SHOW DATABASES;"

# Check application database
mysql -u root -p -e "USE digital_banking; SHOW TABLES;"
```

## 📊 Database Schema

After successful startup, the following tables will be created:

- `customer` - Customer information
- `bank_account` - Base account information
- `current_account` - Current account specific data
- `saving_account` - Saving account specific data
- `account_operation` - Transaction history

## 🔄 Switching Back to H2 (if needed)

If you want to switch back to H2 for development:

1. **Uncomment H2 dependency** in `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. **Update `application.properties`**:
   ```properties
   # Comment out MySQL configuration
   # spring.datasource.url=jdbc:mysql://localhost:3306/digital_banking...
   
   # Uncomment H2 configuration
   spring.datasource.url=jdbc:h2:mem:digital-bank
   spring.datasource.driver-class-name=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.h2.console.enabled=true
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

3. **Restart the application**

## 📞 Support

If you encounter issues:
1. Check MySQL error logs
2. Review application console output
3. Verify all configuration steps
4. Ensure MySQL service is running and accessible

For additional help, refer to:
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Spring Boot Database Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
