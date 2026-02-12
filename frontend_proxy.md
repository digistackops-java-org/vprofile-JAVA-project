# Nginx Setup for Proxy
### Install Nginx for Reverse Proxy
```
sudo yum install nginx -y
```
Start the Service
```
sudo systemctl start nginx
sudo systemctl enable nginx
```
### Create Proxy File 
```
cat <<EOT > vproapp
upstream vproapp {

 server app01:8080;

}

server {

  listen 80;

location / {

  proxy_pass http://vproapp;

}

}

EOT
```
### Move that file and Enable Nginx as LB
```
sudo mv vproapp /etc/nginx/sites-available/vproapp
sudo rm -rf /etc/nginx/sites-enabled/default
sudo ln -s /etc/nginx/sites-available/vproapp /etc/nginx/sites-enabled/vproapp
```
starting nginx service and firewall
```
sudo systemctl start nginx
sudo systemctl enable nginx
sudo systemctl restart nginx
```
