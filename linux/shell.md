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