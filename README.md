# mpaas

#### 介绍
ghost framework mpaas 一套完全模块化的Paas平台基础架构，在这里可以是IaaS、SaaS等等，只要你写相应的模块或插件功能就有对应的服务功能，完美演绎国际化化模块paas中台概念，ci/cd、develop插件模块化，一套充满想象的模块化基础架构...。

模块化与插件热加载热卸载，热卸载部分还没完成！，能热热加载就能热卸载，项目总体模块化架构已经非常清晰。

有太多的想法跟思路，但是一个人确实效率有限！

项目虽然拆分了很多spring生态代码但是完全不是spring架构技术，是一套全新的架构思路。

在IOC方面选择放弃SPI全称Service Provider Interface，以为使用SPI会让IOC变得不灵活。

根目录下 doc\项目流程.vsdx有文档，但是可能部分没有及时同步代码变动。

#### 部分代码来源

一部分代码拆分spring而来，一部分代码拆分tomcat webSocket而来。
拆分好了spring的jpa和aop代码，还没调试，jpa跟aop代码涉及量较大，还有其它模块或插件在spring生态中拆分出来的代码也还没调试！

#### 软件架构

目前完成部件（但是还有很多细节需要完善）：
1.  内核与基础框架
2.  IOC
3.  Maven插件 ghost.framework.maven.plugin
4.  SSH管理前后端插件 ghost.framework.web.angular1x.ssh.plugin
5.  data.jdbc.template插件 ghost.framework.data.jdbc.template.plugin
6.  hibernate插件 ghost.framework.data.hibernate.plugin
7.  前端容器资源插件 ghost.framework.web.angular1x.container.plugin
8.  webSocket插件 ghost.framework.web.socket.plugin
9.  web解析模块 ghost.framework.web.module
10. web服务模块 ghost.framework.undertow.web.module
11. mvc插件 ghost.framework.web.mvc.plugin
12. mvc thymeleaf模板插件 ghost.framework.web.mvc.thymeleaf.plugin 基本测试可以解析模板
13. web session插件 ghost.framework.web.session.data.jdbc.plugin
其它小部件插件就不再描述，后面完善再补充
内容好很乱，后面一遍遍再完善...

#### Jdk

V1.8

#### 前端架构

之前前端也考虑用single-spa的vue来实现，但是总体还是不太合适，前后端一体化实现还是AngularJS 1.x最合适，目前！

1. AngularJS v1.4.7
2. angular-ui-bootstrap v0.12.1
3. query v3.5.1
4. CodeMirror 
5. Xterm.js

#### 运行调试

1.  现在只是雏形··下载代码跑一下，看看代码！
2.  ghost.framework.platform 项目作为测试启动项目
3.  application.properties 配置 ghost.framework.datasource.url，ghost.framework.datasource.username， 
    ghost.framework.datasource.password三个数据库参数
    ghost.framework.web.default.server.port修改下端口
4.  ghost.framework.platform.PlatformApplication的私库修改为您自己的库注解 @MavenDepositorys 修改下，或者使用默认带的私库，可能速度 
    有点慢！
5.  maven deploy下完成后就可以启动运行看效果
#### 使用说明

1.  后面spring生态有的功能都会有，spring生态没有的功能也会有，缺联合贡献者！
2.  xxxx
3.  xxxx

#### 参与贡献

1.  项目招募联合贡献者，交流QQ：27048384，交流群QQ：29044972，微信手机：13715848993
2.  联合贡献者前期完善内容aop切面插件、transaction事务插件、jpa插件、mongodb插件、mybatis插件、redis插件、nginx插件、swagger插件、脚本管理插件等等。
3.  联合贡献者全程做技术交流，期望联合贡献者有周末时间接触交流，交流地点深圳。
4.  目标能把项目做成一个完善的开源paas项目。
