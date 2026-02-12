# Database Setup
Create "t2.micro" EC2 Instance and open port "3306" for DB 

## Install MYSQL DB
```
sudo yum update -y
sudo wget https://dev.mysql.com/get/mysql80-community-release-el9-1.noarch.rpm
sudo dnf install mysql80-community-release-el9-1.noarch.rpm -y
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023
sudo dnf install mysql-community-client -y
sudo dnf install mysql-community-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo systemctl status mysqld
```

## Setup MYSQL DB

#### Allow any Host connect to DB
```
sudo vi /etc/my.cnf
```
ADD these Under [mysqld]
```
bind-address = 0.0.0.0
```
Restart MYSQL DB
```
sudo systemctl restart mysqld
```
Get your temporary root Password
```
sudo grep 'temporary password' /var/log/mysqld.log
```
Setup your root Password
```
sudo mysql_secure_installation
```
Login to your MYSQL
```
mysql -u root -p
```
Test it is working or Not
```
SELECT VERSION();
```
### Create one Databse Admin User for our DB 
These user can login to DB to do Tasks and used 
```
CREATE USER '<user-name>'@'Host-IP' IDENTIFIED BY 'Password-HERE';
GRANT ALL PRIVILEGES ON <DB-Name>.* TO '<user-name>'@'Host-IP';
FLUSH PRIVILEGES;
```

```
CREATE USER 'dbadmin'@'%' IDENTIFIED BY 'Admin@123';
GRANT ALL PRIVILEGES ON *.* TO 'dbadmin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```
#### Create Accounts DB 
```
CREATE DATABASE IF NOT EXISTS accounts;
```
#### Create Application user (if not exists)
```
CREATE USER IF NOT EXISTS 'appuser'@'%' IDENTIFIED BY 'P@55Word';
```
#### Grant Priviliges to Application user to DB accounts
```
GRANT ALL PRIVILEGES ON accounts.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```
##### verify the Application user Pemissions
```
SHOW GRANTS FOR 'appuser'@'%';
```


# MEMCACHE Setup
Create "t2.micro" EC2 Instance for MEMCACHE and open port "11111" for MEMCACHE 

### Install MEMCACHE
```
sudo dnf install memcached -y
sudo systemctl start memcached
sudo systemctl enable memcached
sudo systemctl status memcached
```

### Setup MEMCACHE
```
sudo vim /etc/sysconfig/memcached
```
By default memcached allow localhost "127.0.0.0" so we need to replace it with "0.0.0.0"

#### Restart MEMCACHE

```
sudo systemctl restart memcached
```


# RABBITMQ Setup
## Must use Redhat 'RHEL-9' & "t2.small" EC2 Instance for RABBITMQ and open port "5672" for RABBITMQ 
Make sure you are on RHEL 9:
```
cat /etc/redhat-release
```
Minimum Recommend RAM 2GB
```
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```
Install required system tools
```
sudo dnf install -y curl gnupg2 ca-certificates
```
#### Step 1: Add BOTH official repos (Erlang + RabbitMQ)
```
sudo tee /etc/yum.repos.d/rabbitmq.repo > /dev/null <<'EOF'
[modern-erlang]
name=modern-erlang-el9
baseurl=https://yum1.rabbitmq.com/erlang/el/9/$basearch
        https://yum2.rabbitmq.com/erlang/el/9/$basearch
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://github.com/rabbitmq/signing-keys/releases/download/3.0/cloudsmith.rabbitmq-erlang.E495BB49CC4BBE5B.key

[rabbitmq-el9]
name=rabbitmq-el9
baseurl=https://yum1.rabbitmq.com/rabbitmq/el/9/noarch
        https://yum2.rabbitmq.com/rabbitmq/el/9/noarch
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://github.com/rabbitmq/signing-keys/releases/download/3.0/cloudsmith.rabbitmq-server.9F4587F226208342.key
EOF
```

#### Step 2: Import signing keys (official)
```
sudo rpm --import https://github.com/rabbitmq/signing-keys/releases/download/3.0/rabbitmq-release-signing-key.asc
sudo rpm --import https://github.com/rabbitmq/signing-keys/releases/download/3.0/cloudsmith.rabbitmq-erlang.E495BB49CC4BBE5B.key
sudo rpm --import https://github.com/rabbitmq/signing-keys/releases/download/3.0/cloudsmith.rabbitmq-server.9F4587F226208342.key
```
#### Step 3: Clean and refresh DNF cache
```
sudo dnf clean all -y
sudo dnf makecache -y
```
Verify repos:
```
sudo dnf repolist | grep -E "rabbitmq|erlang"
```
You see output
    "modern-erlang"
    "rabbitmq-el9"
#### Step 4: Install Erlang + RabbitMQ
```
sudo dnf install -y erlang rabbitmq-server
```
#### Step 5: Start RabbitMQ
```
sudo systemctl enable rabbitmq-server
sudo systemctl start rabbitmq-server
sudo systemctl status rabbitmq-server
```
## If you want UI option {optional}
```
sudo rabbitmq-plugins enable rabbitmq_management
sudo systemctl restart rabbitmq-server
```
open in browser
```
http://<EC2-PUBLIC-IP>:15672
```

## Create Admin User (important for remote access)

By default guest only works locally.
```
sudo rabbitmqctl add_user <username> <StrongPassword>
sudo rabbitmqctl set_user_tags <username> administrator
sudo rabbitmqctl set_permissions -p / <username> ".*" ".*" ".*"
```
```
sudo rabbitmqctl add_user admin P@55Word
sudo rabbitmqctl set_user_tags admin administrator
sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

```
restart RabbitMQ Server
```
sudo systemctl restart rabbitmq-server
```
