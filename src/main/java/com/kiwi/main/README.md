### JscannerPro

此工具其实是[Jscanner](https://github.com/hmx222/JScanner)的升级版本，主要升级功能如下：

- 核心由Python开发变为Java开发
- 此工具是Burp的插件
- 新增加了参数自动收集与爆破的功能（手动选择）
- 对之前的算法进行了升级（也保留了原来工具的特性）
- GUI操作，更加直观与方便操作



#### 安装

**需要的burp的最低版本：2023.7**（因为使用了Montoya API 进行开发滴）

在release当中下载最新版本，直接进行安装即可



#### 构建

- Java版本：Java17
- maven版本：3.9.8
- Montoya API 版本：2024.11

载入pom文件的依赖后执行：`mvn clean package`即可



#### 遇到问题

在issue当中开始轰炸即可

