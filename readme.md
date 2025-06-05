# 软件工程基础 教学管理系统A组 集成后端
**先不要向我申请协作权限，请把本库fork到你自己本地进行修改后向我发出pull request我会审核通过**
## 这个库是怎么来的：
我fork了各个大组的后端 完成了依赖修改和子模块集成 请各组在github上维护自己的现有后端即可（课程资源和课程安排小组请共同维护集成后端）
## 如何clone到本地：
```bash
 git clone --recursive https://github.com/key88cb/Teaching_system_backend.git
# 一定要加 --recursive!
```
- 如果由于网络问题无法clone，请自行使用ssh,github desktop或下载压缩包(不建议使用魔法 建议使用ZJUWLAN(非secure))
- 成功的clone？ 你会得到teaching_system_backend作为父文件夹，CourseSelection、InfoSystem、SE_onlinetesting、team_project_eduMS作为子文件夹的结构（其中，四个子文件夹均非空）
## 如何setup 
- 使用IDEA打开teaching_system_backend,打开 ``teaching_system_backend\pom.xml``,使用maven构建（或直接在IDEA右侧点选重新加载所有项目），而后可看到运行栏和服务栏出现更新（如果没有，大概率是JDK没有配置，请进入随便一个后端的后端app入口文件，配置JDK即可）
- 服务中：忽略SectionApplication,此部分与项目无关
## 库内有什么？
***注意：所有后端已被分配统一端口请各组对前端进行一定修改！***
1. 课程选择后端 文件夹： CourseSelection :后端分配端口 8083 应用名 BackendApplication
2. 课程资源+课程安排集成后端 文件夹名 InfoSystem : 后端分配端口 8081 应用名 App 
3. 在线测试后端 SE_onlinetesting/online-testing-backend : 后端分配端口 8082 应用名 OnlineTestingBackendApplication
4. 信息管理后端 team_project_eduMS : 后端分配端口 8080 应用名 EducationMsApplication
## 无法运行？
4个后端（其中有一个是两个系统集成后端）对数据库的要求各不相同，需要在其中把部分application.properties的root密码改成你自己的root密码，请分别查看：
- [课程选择后端](CourseSelection/src/main/resources/application.properties)
- [课程资源+课程安排集成后端](InfoSystem/src/main/resources/application.properties)
- [在线测试后端](SE_onlinetesting/online-testing-backend/src/main/resources/application.properties)
- [信息管理后端](team_project_eduMS/src/main/resources/application.properties)
- 正常结果：查看log除了EducationMSAppication因为类缺失错误无法运行以外,其余均可运行

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

