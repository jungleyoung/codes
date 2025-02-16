# git常见操作

设置代理
~~~
git config --global http.proxy http://127.0.0.1:10808
git config --global https.proxy https://127.0.0.1:10808
~~~
取消代理
~~~
# 取消全局 HTTP 代理配置
git config --global --unset http.proxy

# 取消全局 HTTPS 代理配置
git config --global --unset https.proxy
~~~