### ParamScanner

#### 功能

- 自动收集请求参数

- 提供GET/POST请求方法的参数爆破
- 建议搭配[Jscanner](https://github.com/hmx222/JScanner)食用（发现502等参数码的链接后，来到此工具进行处理）



#### 安装

**需要的burp的最低版本：2023.7**（因为使用了Montoya API 进行开发滴）

在release当中下载最新版本，直接进行安装即可，出现Good Luck 代表安装成功



#### 使用

在数据包页面，右键 -- `Extensions`-- `ParamScanner` -- `Find the Parameters`

来到ParamScanner，选中你想要使用的请求方法，点击发送即可，**需要稍等一会**。



#### 注意

由于现在对于POST当中JSON格式的请求数据暂时未处理好，所以暂时不要对JSON格式的数据包进行处理，且暂时支持GET，POST请求！！！



#### 构建

- Java版本：Java17
- maven版本：3.9.8
- Montoya API 版本：2024.11

载入pom文件的依赖后执行：`mvn clean package`即可



#### TODO

目前正在逐渐融合JScanner的功能到burp插件当中，请各位耐心等待。



#### 遇到问题

提交issue即可

