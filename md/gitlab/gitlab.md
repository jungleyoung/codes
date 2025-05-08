# gitlbab笔记
1. 关闭、删除gitlab
~~~shell
docker stop gitlab
docker rm gitlab
rm -rf /srv/gitlab
~~~
2. 部署一个gitlab，与旧环境版本一致
~~~shell
docker run --detach --hostname 10.0.124.50 --publish 4443:443 --publish 80:80 --publish 1022:22 --publish 4567:4567 --publish 5000:5000 --publish 5001:5001 --name gitlab --restart always --volume /srv/gitlab/config:/etc/gitlab --volume /srv/gitlab/logs:/var/log/gitlab --volume /srv/gitlab/data:/var/opt/gitlab 10.0.124.23:8083/gitlab/gitlab-ce:13.12.0-ce.0
~~~

3. 停止新的gitlab
~~~shell
docker stop gitlab
~~~
4. 复制旧环境文件
~~~shell
scp -r /srv/gitlab root@10.0.124.50:/srv/
~~~
5. 启动gitlab
```shell
docker start gitlab
```
6. 更新权限
~~~shell
docker exec -it gitlab update-permissions
docker restart gitlab
~~~
7. 安装注册runner
~~~shell
docker run -d --name gitlab-runner --restart always -v /var/run/docker.sock:/var/run/docker.sock -v /srv/gitlab-runner/config:/etc/gitlab-runner 10.0.124.23:8083/gitlab/gitlab-runner:latest

gitlab-runner stop
gitlab-runner uninstall

#maven
10.0.17.163:8082/mvn
http://10.0.124.23:8082/ant:mvn3-jdk8
http://10.0.124.23:8082/mvn
10.0.124.23:8083/v2/mvn
vim  /srv/gitlab-runner/config/config.toml

#先停再改config.toml再启动
#修改为存在时不拉取

[[runners]]
name = "sonar2"
url = "http://10.0.124.50/"
token = "fJsc2VivwziuVmGgiyrP"
executor = "docker"
[runners.custom_build_dir]
[runners.cache]
[runners.cache.s3]
[runners.cache.gcs]
[runners.cache.azure]
[runners.docker]
tls_verify = false
image = "10.0.124.23:8082/mvn"
privileged = false
disable_entrypoint_overwrite = false
oom_kill_disable = false
disable_cache = false
volumes = ["/cache"]
pull_policy = ["if-not-present"]
shm_size = 0
~~~shell

#找回密码
gitlab-rails console
user = User.where(id:1).first
user.password='gitlab123'
user.save!

#打日志
gitlab-ctl tail

#runner
# 取消注册所有附加的注册者
gitlab-runner unregister --all-runners
# 要取消注册特定的runner，首先通过执行gitlab-runner list获取注册者的详细信息
gitlab-runner list
# 再根据信息注销，按注册令牌
gitlab-runner unregister --url http://gitlab.example.com/ --token t0k3n
# 按名字
gitlab-runner unregister --name hj_project



root
gitlabroot
git tag -l| awk '/^./$/ {print  $1}' | xargs git tag -d
git show-ref --tag | awk '/^.*$/ {print ":" $2}' | xargs git push origin
8ec29ffda5
token=_kTLvcr9_mnTJYNvsQM1


#### git硬回滚
~~~
找到提交前的哈希值
git checkout hashvalue
删除原分支（可选）
git branch -D oldxxxxx
删除远端分支（可选）
git push origin -d oldxxxxx
创建并切换新分支
git checkout -b newxxxxx
推送新分支
git push -u origin newxxxxxx
~~~