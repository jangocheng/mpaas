# mpaas

https://gitee.com/ghost-framework/mpaas

https://github.com/guoshucan/mpaas

#### Introduction
ghost framework mpaas A set of fully modularized Paas platform infrastructure, here can be IaaS, SaaS, etc., as long as you write the corresponding module or plug-in function, there will be corresponding service functions, perfect interpretation of the international modular paas concept , Ci/cd, develop plug-in modularization, a set of imaginative modular infrastructure...

Modules and plug-ins are hot-loaded and hot-unloaded, the hot-unloading part has not been completed yet! , Can be thermally loaded and thermally unloaded. The overall modular structure of the project is very clear.

There are too many ideas and ideas, but one person is really limited in efficiency!

Although the project has split a lot of spring ecological code, it is not a spring architecture technology at all, it is a new set of architectural ideas.

New RESTful and MVC analytical architecture system standards.

In terms of IOC, I chose to abandon SPI (full name Service Provider Interface), because using SPI will make IOC inflexible.

There are documents in the root directory doc\project process.vsdx, but some code changes may not be synchronized in time.

#### Part of the code source

Part of the code split comes from spring, and part of the code split comes from tomcat webSocket.
After splitting the spring jpa and aop codes, they have not been debugged yet. The jpa and aop codes involve a large amount, and there are other modules or plug-ins that split the code in the spring ecosystem and have not yet been debugged!

#### Software Architecture

Currently completed parts (but there are still many details to be improved):
1. Core and basic framework.
2. IOC.
3. Maven plugin ghost.framework.maven.plugin.
4. SSH management front and back end plug-in ghost.framework.web.angular1x.ssh.plugin.
5. Data.jdbc.template plug-in ghost.framework.data.jdbc.template.plugin.
6. The hibernate plugin ghost.framework.data.hibernate.plugin.
7. The front-end container resource plug-in ghost.framework.web.angular1x.container.plugin.
8. The webSocket plugin ghost.framework.web.socket.plugin.
9. The web analysis module ghost.framework.web.module.
10. The web service module ghost.framework.undertow.web.module.
11. The mvc plugin ghost.framework.web.mvc.plugin.
12. Mvc thymeleaf template plugin ghost.framework.web.mvc.thymeleaf.plugin basic test can parse templates.
13. web session plugin ghost.framework.web.session.data.jdbc.plugin.

Other widget plug-ins will not be described anymore, they will be perfected and then added later, and will be perfected again and again later...

#### Version

Jdk V1.8

Current project 1.0-SNAPSHOT

#### Front-end architecture

Previously, the front-end was also considered to be implemented with single-spa's vue, but overall it is still not suitable. The integration of the front-end and the back-end is still the most suitable for AngularJS 1.x, currently!

1. AngularJS v1.4.7
2. angular-ui-bootstrap v0.12.1
3. query v3.5.1
4. CodeMirror
5. Xterm.js

#### Run debugging

1. Now it's just a prototype... Download the code and run it, and take a look at the code!
2. The ghost.framework.platform project is used as a test startup project
3. Application.properties configuration ghost.framework.datasource.url, ghost.framework.datasource.username,
    ghost.framework.datasource.password three database parameters
    ghost.framework.web.default.server.port modify the port
4. Create two blank databases ghost_session and ghost-framework
5. The private library of ghost.framework.platform.PlatformApplication is modified to your own library. Note @MavenDepositorys is modified to your own private library, and the root directory pom.xml is modified
    Private library configuration.
    If you have not built a private library, you can test the private library configuration file under the project maven private library configuration file as a test, then you can directly download the code and configure the maven file, which may be a bit slow!
6. After maven deploy is packaged, you can start and run to see the effect
#### Instructions for use

1. There will be some functions in the spring ecology, and there will be functions not in the spring ecology. There is a lack of joint contributors!
2. xxxx
3. xxxx

#### Participate in contribution

1. The project recruits joint contributors, exchange QQ: 27048384, exchange group QQ: 29044972, WeChat mobile phone: 13715848993
2. Joint contributors in the early stage to improve the content aop aspect plugin, transaction transaction plugin, jpa plugin, mongodb plugin, mybatis plugin, redis plugin, nginx plugin, swagger plugin, script management plugin, idea plugin, etc.
3. The joint contributors will conduct technical exchanges throughout the whole process. It is expected that the joint contributors will have weekend time to contact and exchange, and the exchange will be in Shenzhen.
4. The goal is to make the project a complete open source modular paas project.

####  business model
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E6%80%BB%E4%BD%93%E5%95%86%E4%B8%9A%E6%A8%A1%E5%BC%8F.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/C%E3%80%81B%E3%80%81D%E7%AB%AF%E7%9A%84%E5%95%86%E4%B8%9A%E6%A8%A1%E5%BC%8F.jpg)

#### Architecture model

![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E6%9E%B6%E6%9E%84%E6%A8%A1%E5%9E%8B.png)

#### Start the process

![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E5%90%AF%E5%8A%A8%E6%B5%81%E7%A8%8B.png)

#### Type Bean process (a little bit different from the code has not been changed)

![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E7%B1%BB%E5%9E%8B%E6%B3%A8%E8%A7%A3Bean%E6%B5%81%E7%A8%8B.jpg)

#### Example effect picture

![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/ssh%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E5%B7%A6%E8%BE%B9%E5%AF%BC%E8%88%AA%E8%8F%9C%E5%8D%95%E5%AE%B9%E5%99%A8.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E7%AE%A1%E7%90%86%E5%91%98%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E8%84%9A%E6%9C%AC%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E8%AE%BE%E7%BD%AE%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E9%A1%B6%E9%83%A8%E5%AF%BC%E8%88%AA%E8%8F%9C%E5%8D%95%E4%B8%AD%E9%97%B4%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E9%A1%B6%E9%83%A8%E5%AF%BC%E8%88%AA%E8%8F%9C%E5%8D%95%E5%8F%B3%E8%BE%B9%E6%8F%92%E4%BB%B6%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E9%A1%B6%E9%83%A8%E5%AF%BC%E8%88%AA%E8%8F%9C%E5%8D%95%E5%B7%A6%E8%BE%B9%E6%95%88%E6%9E%9C.jpg)
![Image text](https://gitee.com/ghost-framework/mpaas/raw/master/img/%E9%A1%B6%E9%83%A8%E5%AF%BC%E8%88%AA%E8%8F%9C%E5%8D%95%E6%8F%92%E4%BB%B6%E5%AE%B9%E5%99%A8.jpg)

#### Example effect preview

[Preview address](http://mpaas.easy-cloud.online:8888)
