# MiPushFramework

[![Build Status](https://travis-ci.org/Trumeet/MiPushFramework.svg?branch=master)](https://travis-ci.org/Trumeet/MiPushFramework)
[![License GPL](https://img.shields.io/badge/license-GPL-blue.svg)](https://github.com/Trumeet/MiPushFramework/blob/master/LICENSE)
![Min Android Version](https://img.shields.io/badge/android-lollipop-%23860597.svg)

在任何非 MIUI 设备上体验小米系统级推送。

![](https://raw.githubusercontent.com/Trumeet/MiPushFramework/master/art/tab_events.jpg)
![](https://raw.githubusercontent.com/Trumeet/MiPushFramework/master/art/tab_permissions.jpg)
![](https://raw.githubusercontent.com/Trumeet/MiPushFramework/master/art/ask.jpg)
![](https://raw.githubusercontent.com/Trumeet/MiPushFramework/master/art/tab_settings.jpg)
![](https://raw.githubusercontent.com/Trumeet/MiPushFramework/master/art/tab_apps.jpg)

## 什么是小米系统级推送，为什么会有这个项目

小米推送是小米公司提供的推送服务。就我个人看来非常喜欢这个服务，而且许多App都在使用（如酷安）。

它非常轻量，会在 MIUI 设备上自动启用系统推送，而非 MIUI 设备则在后台保持长连接。



### 系统级推送

类似 GCM，小米推送的系统级推送是在 MIUI 完成的。应用在启动时，会判断如果是 MIUI ROM 则向系统注册推送，推送工作都由系统完成，应用无需后台，更省电。

然而在非 MIUI，每个使用小米推送的应用都会在后台启动一个 `XMPushService`， 10个应用就有10个，20个就有20个服务.. 非常耗电耗内存费流量。



### 本项目的意义

本项目就是想让任何不用MIUI的用户都能用上小米的系统推送，这样既能保证推送，又保证了无需后台。


## 缺陷

* 不支持 MIUI。
* 在 Android O 上，可能由于 **后台限制** 导致推送服务中断，请尝试对 `Push` 取消后台限制
* 如果使用了绿色守护小米推送处方会无法启动推送服务
* 不建议使用 黑域、绿色守护、Xposed一些模块 对 `Push` 做操作，可能导致推送不稳定
* 只有推送功能。其他完整功能（如查找手机）请体验 MIUI
* 对 `Push` 启用电池优化会无法后台运行



## 优点

* 简单，安装非常简单，无需 Root、Xposed、ROM 支持
* 使用后，其他应用的`XMPushService`会自动禁用，就像在 MIUI
* `XMPushService`禁用后，还能保证推送。
* 更多设置项，可以针对每一款应用设置不同的推送权限
* 完整事件记录，可以监控每个应用的 注册和推送
* 可以在应用程序注册推送时选择是否允许（类似 iOS）
* 拦截小米推送产生的不必要唤醒，也能阻止它读取您的隐私



## 开始使用

安装步骤非常简单 ：

* 前往 Release 标签，下载最新的 Release APK（非 `xmsf_service.apk`），并安装。
* 跟着向导进行设置


## 反馈问题

遇到任何问题，请先看看 Issues 里面有没有人提过。（常见问题：无法收到推送）
如果没有找到答案，请为每个问题提交一份 Issue，并务必带上如下内容，以便开发者解决：

* 你的 ROM 是什么，Android 版本是什么
* 有没有使用框架等工具

同时，请使用 设置-获取日志 获取你的日志文件，写进 Issue。

## 日志

框架会自动记录日志，保存到私有目录。暂时不会自动清理。设置-高级配置 中已提供清理按钮。



## 参与项目

* 欢迎提交 PR、Issues 帮助这个项目更好。
* 代码规范: Alibaba Java Coding Guidelines / Google Java 编程规范
* 尽量丰富注释和文档
* Git commit message 规范：[Angular](https://github.com/angular/angular.js/blob/master/DEVELOPERS.md#-git-commit-guidelines)
* 构建（Build）本项目所需的配置文件，参见样例文件 local.properties.example。

## 已知问题

* 努比亚ROM应用（第三方使用 MiPush 的应用）可能不会自动禁用其XMPushService并启动服务，请尝试将框架设为系统应用
* 锤子ROM下，push可以正确收到通知，但是通知栏没有提示
* 开发者学生党，开学了更新可能不太及时，请谅解
* 一些通知 Feature 可能无法使用（如通知都会显示为推送框架发出，而不是目标应用 `MIPushNotificationHelper#setTargetPackage`）

## 感谢

* @Rachel030219 提供文件
* Android Open Source Project, MultiType, greenDao, SetupWizardLibCompat, Condom, MaterialPreference，GreenDaoUpgradeHelper, epic, Log4a，helplib，RxJava RxAndroid，RxActivityResult，RxPermissions
* 酷安 @PzHown @lmnm011223 @`苏沐晨风丶`（未采纳） 提供图标

# License

GPL v3，有些狗不遵守开源协议（非本项目），防君子不防小人，请**务必**遵守开源协议！！！
