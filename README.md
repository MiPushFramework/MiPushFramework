# MiPushFramework

[![Build Status](https://travis-ci.org/MiPushFramework/MiPushFramework.svg?branch=master)](https://travis-ci.org/MiPushFramework/MiPushFramework)
[![License GPL-3.0](https://img.shields.io/badge/license-GPLv3.0-blue.svg)](https://github.com/MiPushFramework/MiPushFramework/blob/master/LICENSE)
![Min Android Version](https://img.shields.io/badge/android-lollipop-%23860597.svg)

在非 MIUI 系统上体验小米系统级推送。

![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_events.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_permissions.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/ask.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_settings.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_apps.jpg)

## 什么是小米系统级推送，为什么会有这个项目

小米推送是小米公司提供的推送服务，许多 App 都在使用（如酷安）。

它非常轻量，会在 MIUI 设备上自动启用系统推送，而非 MIUI 设备则在后台保持长连接。



### 系统级推送

类似 GCM，小米推送的系统级推送是在 MIUI 完成的。应用在启动时，会判断如果是 MIUI ROM 则向系统注册推送，推送工作都由系统完成，应用无需后台，更省电。

然而在非 MIUI，每个使用小米推送的应用都会在后台启动一个 `XMPushService`， 10个应用就有10个，20个就有20个服务.. 非常耗电耗内存费流量。



### 本项目的意义

本项目就是想让任何不用MIUI的用户都能用上小米的系统推送，这样既能保证推送，又保证了无需后台。


## 注意

* 请勿使用 黑域、绿色守护、Xposed一些模块 对 `Push` 做操作，可能导致推送不稳定
* 只有推送功能。其他完整功能（如查找手机）请使用 MIUI
* 服务本身不需要 Root、Xposed 支持，但是为了伪装为MIUI设备，建议使用伪装增强模块



## 优点

* 简单，安装非常简单
* 使用后，其他应用的 `XMPushService` 会自动禁用，就像在 MIUI，同时还能保证推送
* 完整事件记录，可以监控每个应用的 注册和推送
* 拦截小米推送产生的不必要唤醒，也能阻止它读取您的隐私




## 开始使用

安装步骤非常简单 ：

* 前往 [Releases 标签](https://github.com/MiPushFramework/MiPushFramework/releases)，下载最新的 Release APK ，并安装。
* 跟着向导进行设置。


## 反馈问题

遇到任何问题，请先看看 Issues 里面有没有人提过。（常见问题：无法收到推送）
如果没有找到答案，请为每个问题提交一份 Issue，并务必带上如下内容，以便开发者解决：

* 你的 ROM 是什么，Android 版本是什么
* 有没有使用框架等工具

同时，请使用 设置-获取日志 获取你的日志文件，写进 Issue。

## 日志

框架会自动记录日志，保存到私有目录。您可以前往 设置-高级配置 中清理。



## 参与项目

请参考 [Contribution Guideline](CONTRIBUTION.md)

## 已知问题

* 对于部分小众的ROM （如 360OS）导致无法正常工作的情况，我们只会竭尽全力保证推送的运行，其它不妨碍推送的「特殊适配」会被忽略。对于这些情况，建议您更换更好的 ROM 以获得最佳体验。
* 努比亚ROM应用（第三方使用 MiPush 的应用）可能不会自动禁用其 XMPushService 并启动服务，请尝试将框架设为系统应用
* 锤子 ROM 下，Push 可以正确收到通知，但是通知栏没有提示 #143
* 开发者学生党，开学了更新可能不太及时，请谅解
* 一些通知 Feature 可能无法使用（如通知都会显示为推送框架发出，而不是目标应用）

## 感谢

* @Rachel030219 提供文件
* Android Open Source Project, MultiType, greenDao, SetupWizardLibCompat, Condom, MaterialPreference，GreenDaoUpgradeHelper, epic, Log4a，helplib，RxJava RxAndroid，RxActivityResult，RxPermissions, hiBeaver
* 酷安 @PzHown @lmnm011223 @苏沐晨风丶（未采纳） 提供图标


# License

GPL v3，有些狗不遵守开源协议（非本项目），请**务必**遵守开源协议
