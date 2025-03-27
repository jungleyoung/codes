## oracle笔记

### oracle目录
~~~oracle
--查看表空间位置
SELECT tablespace_name, file_name FROM dba_data_files;
--查看目录位置
SELECT DIRECTORY_NAME ,DIRECTORY_PATH FROM DBA_DIRECTORIES ;
--创建新目录
create directory dumpdir as '/home/oracle/data';
Grant read,write on directory dumpdir to system;
~~~
~~~shell
chown -R oracle:oinstall /home/oracle/data
~~~


### 重建用户

~~~oracle
--删除用户
DROP USER EBANK CASCADE;
--删除表空间以及物理文件
DROP TABLESPACE EBANK_DATA INCLUDING CONTENTS AND DATAFILES;
--重建用户
create bigfile tablespace EBANK_DATA datafile '/oracle/oradata/ebankdb/ebank_data.dbf' size 500M autoextend on;
create user ebank identified by ebank default tablespace EBANK_DATA;
grant create session to ebank;
grant unlimited tablespace to ebank;
grant create table to ebank;
grant drop any table to ebank;
grant insert any table to ebank;
grant update any table to ebank;
~~~

~~~shell
#重新导入数据
impdp system/oracle directory=IMP dumpfile=ebank_20230626-2130.dmp remap_schema=EBANK:ebank remap_tablespace=EBANK_DATA:EBANK_DATA logfile=imp_ebank_20230626-2130.log full=y
~~~

### 设置密码有效期

~~~oracle
-- 密码有效期
SELECT *
FROM dba_profiles
WHERE profile = 'DEFAULT'
  AND resource_name = 'PASSWORD_LIFE_TIME';
ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;
alter user 用户名 identified by 密码;
~~~

### 设置连接数
~~~oracle
--连接数
alter system set processes = 3000 scope = spfile;
~~~

### 设置DBlink

~~~oracle
SELECT *
FROM user_sys_privs
WHERE PRIVILEGE LIKE UPPER('%DATABASE LINK%')
  AND USERNAME = 'CMS';
grant CREATE PUBLIC DATABASE LINK, DROP PUBLIC DATABASE LINK to CMSCS;
create public database link link2bedc
    connect to bedc identified by bedc
    using '(DESCRIPTION =(ADDRESS_LIST =(ADDRESS =(PROTOCOL = TCP)(HOST = 10.0.124.101)(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME = testdb)))';
~~~

### 切换实例
~~~shell
#打印当前实例
echo $ORACLE_SID 
#切换实例
export ORACLE_SID=ebankdb 
~~~

### 删除表空间
~~~oracle
drop tablespace UNDOTBS1 including contents and datafiles;
~~~

### 归档日志
~~~shell
rman target /
#清理归档
crosscheck archivelog all;
DELETE ARCHIVELOG ALL COMPLETED BEFORE 'SYSDATE-1';
delete expired archivelog all;
ALTER DATABASE NOARCHIVELOG;
#关闭归档
alter database flashback off;
alter database noarchivelog;
alter database open;
~~~

### 压缩表空间
~~~oracle
--清空回收站表
PURGE TABLE bedc_cmd ;
--压缩表空间
ALTER TABLESPACE TBS_CF SHRINK SPACE;
ALTER DATABASE DATAFILE '/oracle/oradata/testdb/tbscf.dbf' RESIZE 300G;
~~~

### 表空间使用
~~~oracle
--查看表空间使用情况
SELECT tablespace_name,
       ROUND(SUM(bytes) / 1024 / 1024 / 1024, 2)              AS total_size_GB,
       ROUND(SUM(bytes - free_bytes) / 1024 / 1024 / 1024, 2) AS used_size_gb,
       ROUND(SUM(free_bytes) / 1024 / 1024 / 1024, 2)         AS free_size_gb
FROM (SELECT tablespace_name, bytes, 0 AS free_bytes
      FROM dba_data_files
      UNION ALL
      SELECT tablespace_name, 0 AS bytes, bytes AS free_bytes
      FROM dba_free_space)
GROUP BY tablespace_name;
~~~

### 查看最大表
~~~oracle
--查看最大表
SELECT segment_name,
       segment_type,
       ROUND(SUM(bytes) / 1024 / 1024 / 1024, 2) AS size_GB
FROM dba_segments
GROUP BY segment_name, segment_type
ORDER BY size_GB desc;
~~~

### 限制可连接ip
~~~shell
vim /oracle/11.2.0/db_1/network/admin/sqlnet.ora
# 开启 IP 地址检查
TCP.VALIDNODE_CHECKING = YES

# 允许连接的 IP 地址或子网，多个地址用逗号隔开 必须把oracle ip加入
TCP.INVITED_NODES = (10.0.124.*,10.0.125.*,10.0.14.172,10.0.14.246,10.0.118.67,10.0.14.26,10.0.18.58)

# 禁止连接的 IP 地址或子网
TCP.REJECTED_NODES = (192.168.3.0/24)
vim tnsnames.ora
主机名要对
~~~

### 监听
~~~shell
lsnrctl start
lsnrctl stop
#关闭数据库
shutdown immediate;
#启动数据库
startup;
~~~