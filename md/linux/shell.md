## 常用shell命令

### 批量重命名
~~~shell
for file in *20241206*.zip; do echo mv "$file" "${file//20241206_/20241212_1206-}"; done
for file in *20241206*.zip; do mv "$file" "${file//20241206_/20241212_1206-}"; done
~~~
### sftp
~~~shell
sftp -oIdentityFile=keyPath -oPort=port user@host
~~~

### yum镜像
~~~shell
vim  /etc/yum.repos.d/nexus-centos.repo
#修改为镜像地址
[base]
name=CentOS-7 - Base - mirrors.aliyun.com
failovermethod=priority
baseurl=http://10.0.124.23:8081/repository/yum7ali/centos/7/os/x86_64/
enabled=1
gpgcheck=0
 
#released updates s
[updates]
name=CentOS-7 - Updates - mirrors.aliyun.com
failovermethod=priority
baseurl=http://10.0.124.23:8081/repository/yum7ali/centos/7/updates/x86_64/
enabled=1
gpgcheck=0

#additional packages that may be useful
[extras]
name=CentOS-7 - Extras - mirrors.aliyun.com
failovermethod=priority
baseurl=http://10.0.124.23:8081/repository/yum7ali/centos/7/extras/x86_64/
enabled=1
gpgcheck=0

#additional packages that extend functionality of existing packages
[centosplus]
name=CentOS-7 - Plus - mirrors.aliyun.com
failovermethod=priority
baseurl=http://10.0.124.23:8081/repository/yum7ali/centos/7/centosplus/x86_64/
enabled=1
gpgcheck=0

vim /etc/yum.repos.d/xdn.repo
enabled=0
~~~

### ssh私钥登录
~~~shell
#将xxx.pub追加到~/.ssh/authorized_keys

#赋予权限
chown -R username:username /home/username/.ssh
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

#接下来就可以用私钥登录服务器
~~~