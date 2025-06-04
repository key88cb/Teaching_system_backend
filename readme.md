# 软件工程基础 教学管理系统A组 集成后端

## 这个库是怎么来的：
我fork了各个大组的后端 完成了依赖修改和子模块集成 请各组维护自己的现有后端即可（课程资源和课程安排小组请共同维护集成后端）
## 如何clone到本地：
```bash
 git clone --recursive https://github.com/key88cb/Teaching_system_backend.git
```
如果由于网络问题无法clone，请自行使用ssh,github desktop或下载压缩包(不建议使用魔法 建议使用ZJUWLAN(非secure))
## 库内有什么？
***注意：所有后端已被分配统一端口请各组对前端进行一定修改！***
1. 课程选择后端 文件夹： CourseSelection :后端分配端口 8083 应用名 BackendAppication
2. 课程资源+课程安排集成后端 文件夹名 InfoSystem : 后端分配端口 8081 应用名 App 
3. 在线测试后端 SE_onlinetesting/online-testing-backend : 后端分配端口 8082 应用名 OnlineTestingBackendAppcation
4. 信息管理后端 team_project_eduMS : 后端分配端口 8080 应用名 EducationMsApplication

```sql
-- 需要预先创建数据库：
create database IF NOT EXISTS couseselection;
create database IF NOT EXISTS inforsystem;

CREATE DATABASE IF NOT EXISTS online_test_db;
CHARACTER SET utf8mb4;
COLLATE utf8mb4_unicode_ci;
CREATE USER 'test_system_user'@'localhost' IDENTIFIED BY 'online_test@a758';
-- 授予用户对 'online_test_db' 数据库的所有权限。
GRANT ALL PRIVILEGES ON online_test_db.* TO 'test_system_user'@'localhost';
FLUSH PRIVILEGES;
```
---

