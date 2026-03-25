![SteVe](src/main/webapp/static/images/logo.png)

# SteVe (OCPP 1.6)

1. 安装 JDK 21 和 Maven

- 下载并安装 **JDK 21**：  
  [https://jdk.java.net/archive/](https://jdk.java.net/archive/)  

- 下载并安装 **Apache Maven**：  
  [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)

- 将 `jdk/bin` 和 `maven/bin` 目录添加到系统环境变量 `PATH` 中。

---

2. 安装 MySQL 8 或 MariaDB

- 下载地址：  
  [MySQL :: Download MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

- 选择 **MySQL 8.4.8 LTS** ：Windows (x86, 64-bit), MSI Installer

- 安装过程：
  - 添加 root 用户密码为 `root`
  - bin目录添加到系统环境变量

- 验证安装：打开终端，执行以下命令并输入密码 `root`：
```bash
mysql -u root -p
```

3. 初始化数据库（可选：清理旧数据）

如果之前已安装过 SteVe，建议先清理旧数据库以避免冲突。

```
DROP DATABASE IF EXISTS stevedb;
DROP USER IF EXISTS 'steve'@'localhost';
```

4. 创建 SteVe 所需的数据库和用户

```
CREATE DATABASE stevedb CHARACTER SET utf8 COLLATE utf8_unicode_ci;
CREATE USER 'steve'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON stevedb.* TO 'steve'@'localhost';
```

5. 构建项目

在项目根目录下执行：

```
./mvnw package
```

6. 启动 SteVe

```
java -jar target/steve.war
```