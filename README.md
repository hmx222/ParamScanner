### ParamScanner

#### 功能

- 自动收集请求参数

- 提供GET/POST请求方法的参数爆破
- 融合了[Jscanner](https://github.com/hmx222/JScanner)（发现502等参数码的链接后，来到此工具进行处理）



#### 安装

**需要的burp的最低版本：2023.12**（因为使用了Montoya API 进行开发滴）

在release当中下载最新版本，直接进行安装即可，出现Good Luck 代表安装成功

cmd情况下打开release当中的exe文件，成功监听2334端口。

访问本地2335端口打开web页面进行操作即可。

#### 注意

由于现在对于POST当中JSON格式的请求数据暂时未处理好，所以暂时不要对JSON格式的数据包进行处理，且暂时支持GET，POST请求！！！



#### 构建

- Java版本：Java17
- maven版本：3.9.8
- Montoya API 版本：2024.11

载入pom文件的依赖后执行：`mvn clean package`即可



#### TODO

不断在优化算法，减少性能开支。


#### 遇到问题

提交issue即可

