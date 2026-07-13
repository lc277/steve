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

=======
SteVe requires 
* JDK 21 or newer
* Maven 
* MySQL or MariaDB. You should use [one of these](.github/workflows/main.yml#L11-L35) supported versions.

设置时区

```
SET GLOBAL time_zone = '+00:00';
```

```
CREATE DATABASE stevedb CHARACTER SET utf8 COLLATE utf8_unicode_ci;
CREATE USER 'steve'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON stevedb.* TO 'steve'@'localhost';
```

5. 构建项目

在项目根目录下执行：

```
./mvnw package "-Pprod,mysql"

```

6. 启动 SteVe

<<<<<<< HEAD
```
java -jar target/steve.war
```
=======
    ```
    CREATE DATABASE stevedb CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    CREATE USER 'steve'@'localhost' IDENTIFIED BY 'changeme';
    GRANT ALL PRIVILEGES ON stevedb.* TO 'steve'@'localhost';
    ```
        
2. Download and extract tarball:

    You can download and extract the SteVe releases using the following commands (replace X.X.X with the desired version number):
    ```
    wget https://github.com/steve-community/steve/archive/steve-X.X.X.tar.gz
    tar xzvf steve-X.X.X.tar.gz
    cd steve-X.X.X
    ```

3. Configure SteVe **before** building:

    The basic configuration is defined in [application-prod.properties](src/main/resources/application-prod.properties):
      - You _must_ change [database configuration](src/main/resources/application-prod.properties#L7-L13)
      - You _must_ change [the host](src/main/resources/application-prod.properties#L28) to the correct IP address of your server
      - You _must_ change [web interface credentials](src/main/resources/application-prod.properties#L15-L18)
      - You _can_ access the application via HTTPS, by [enabling it and setting the keystore properties](src/main/resources/application-prod.properties#L36-L41)

    For advanced configuration please see the [Configuration wiki](https://github.com/steve-community/steve/wiki/Configuration)

4. Build SteVe:

    To compile SteVe simply use Maven. A runnable `war` file containing the application and configuration will be created in the subdirectory `steve/target`.

    ```
    # ./mvnw package -Pprod,mysql
    ```

    To build against MariaDB instead, use:

    ```
    # ./mvnw package -Pprod,mariadb
    ```

5. Run SteVe:

    To start the application run (please do not run SteVe as root):

    ```
    # java -jar target/steve.war
    ```

# Docker

If you prefer to build and start this project via docker (you can skip the steps 1, 4 and 5 from above), this can be done as follows: `docker compose up -d`

Because the docker compose file is written to build the project for you, you still have to change the project configuration settings from step 3.
Instead of changing the [application-prod.properties](src/main/resources/application-prod.properties), you have to change the [application-docker.properties](src/main/resources/application-docker.properties). There you have to change all configurations which are described in step 3.
The database password for the user "steve" has to be the same as you have configured it in the docker compose file.

With the default docker compose configuration, the web interface will be accessible at: `http://localhost:8180`

# Kubernetes

First build your image, and push it to a registry your K8S cluster can access. Make sure the build args in the docker build command are set with the same database configuration that the main deployment will use.

`docker build --build-arg DB_HOST= --build-arg DB_PORT= --build-arg DB_USERNAME= --build-arg DB_PASSWORD= --build-arg DB_DATABASE=  -f k8s/docker/Dockerfile -t <IMAGE_NAME> .`

`docker push <IMAGE_NAME>`


Then go to `k8s/yaml/Deployment.yaml` and change `### YOUR BUILT IMAGE HERE ###` to your image tag, and fill in the environment variables with the same database connection that you used at build time.

After this, create the namespace using `kubectl create ns steve` and apply your yaml with `kubectl apply -f k8s/yaml/Deployment.yaml` followed by `kubectl apply -f k8s/yaml/Service.yaml`


To access this publicaly, you'll also have to setup an ingress using something like nginx or traefik. 

# Ubuntu

You'll find a tutorial how to prepare Ubuntu for SteVe here: https://github.com/steve-community/steve/wiki/Prepare-Ubuntu-VM-for-SteVe

# AWS

You'll find a tutorial how to setup SteVe in AWS using Lightsail here: https://github.com/steve-community/steve/wiki/Create-SteVe-Instance-in-AWS-Lightsail

# First Steps

After SteVe has successfully started, you can access the web interface using the configured credentials under:

    http://<your-server-ip>:<port>/steve/manager
    

### Add a charge point

1. In order for SteVe to accept messages from a charge point, the charge point must first be registered. To add a charge point to SteVe select *Data Management* >> *Charge Points* >> *Add*. Enter the ChargeBox ID configured in the charge point and confirm.

2. The charge points must be configured to communicate with following addresses. Depending on the OCPP version of the charge point, SteVe will automatically route messages to the version-specific implementation.
    - SOAP: `http://<your-server-ip>:<port>/steve/services/CentralSystemService`
    - WebSocket/JSON: `ws://<your-server-ip>:<port>/steve/websocket/CentralSystemService`

As soon as a heartbeat is received, you should see the status of the charge point in the SteVe Dashboard.

*Have fun!*

Screenshots
-----
1. [Home](website/screenshots/home.png)
1. [Connector Status](website/screenshots/connector-status.png)
1. [Data Management - Charge Points](website/screenshots/chargepoints.png)
1. [Data Management - OCPP Tags](website/screenshots/ocpp-tags.png)
1. [Data Management - Users](website/screenshots/users.png)
1. [Data Management - Charging Profiles](website/screenshots/charging-profiles.png)
1. [Data Management - Reservations](website/screenshots/reservations.png)
1. [Data Management - Transactions](website/screenshots/transactions.png)
1. [Events and Certificates - Security Events](website/screenshots/events-security.png)
1. [Events and Certificates - Status Events](website/screenshots/events-status.png)
1. [Events and Certificates - Installed Certificates](website/screenshots/certiticates-installed.png)
1. [Events and Certificates - Signed Certificates](website/screenshots/certiticates-signed.png)
1. [Operations - OCPP v1.2](website/screenshots/ocpp12.png)
1. [Operations - OCPP v1.5](website/screenshots/ocpp15.png)
1. [Operations - OCPP v1.6](website/screenshots/ocpp16.png)
1. [Settings](website/screenshots/settings.png)
1. [APIs](website/screenshots/apis.png)

OpenAPI spec
-----
An export of the actual OpenAPI spec for APIs is available [here](api-docs.json).
To explore it interactively, open it in the [Live Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/steve-community/steve/refs/heads/master/api-docs.json).

GDPR
-----
If you are in the EU and offer vehicle charging to other people using SteVe, keep in mind that you have to comply to the General Data Protection Regulation (GDPR) as SteVe processes charging transactions, which can be considered personal data.

Are you having issues?
-----
See the [FAQ](https://github.com/steve-community/steve/wiki/FAQ)

Acknowledgments
-----
[goekay](https://github.com/goekay) thanks to
- [JetBrains](https://jb.gg/OpenSourceSupport) who support this project by providing a free All Products Pack license, and
- ej-technologies GmbH who support this project by providing a free license for their [Java profiler](https://www.ej-technologies.com/products/jprofiler/overview.html).
>>>>>>> master
