# MiPushFramework

[![Build Status](https://travis-ci.org/MiPushFramework/MiPushFramework.svg?branch=master)](https://travis-ci.org/MiPushFramework/MiPushFramework)
[![License GPL-3.0](https://img.shields.io/badge/license-GPLv3.0-blue.svg)](https://github.com/MiPushFramework/MiPushFramework/blob/master/LICENSE)
![Min Android Version](https://img.shields.io/badge/android-lollipop-%23860597.svg)

Let supported push service run system-ly on every Android device
Simplified Chinese document (简体中文文档): [README](https://github.com/MiPushFramework/MiPushFramework/blob/master/README_zh-rCN.md)

![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_events.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_permissions.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/ask.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_settings.jpg)
![](https://raw.githubusercontent.com/MiPushFramework/MiPushFramework/master/art/tab_apps.jpg)

## What is the Xiaomi system push service and the purpose of this project

Xiaomi push service is a Push service (like GCM) provided by Xiaomi, and there are lots of Chinese apps using it.

It is really lightweight, and it will start the system push service on MIUI ROMs, but apps will start a background service on non-MIUI ROMs.

### System Push

Like GCM, Xiaomi System push service runs on MIUI ROMs. When third-party apps start, they will detect whatever the ROM is MIUI. If you are using MIUI, app push messages will be received by the system push service, and the third party app won't keep running in background for saving battery.

However, if you are using non-MIUI ROM, every app which is using Xiaomi Push service will start a service called  `XMPushService` in background, which is wasting battery, memory and data.

### The purpose of this project

The purpose of this project is to let non-MIUI users use Xiaomi System push service, which can keep push messages receiving without keeping running in background.

## Disadvantages

* Doesn't support MIUI.
* We don't recommend using background purifying tools (such as Brevent, Greenify and some Xposed modules) to admire `Push` app, because it may affect the stability of receiving push messages.
* It only supports the feature of Push, other features, for instance, device finding, is not available.

## Advantages

* Easy to use, the installation process is simple and it doesn't need Root, Xposed or ROM supports.
* After using, the `XMPushService` service of third-party apps will be disabled automatically, just like using MIUI.
* After disabling `XMPushService`, the push messages will be received properly.
* More adjustment items, you can adjust push permissions for every app.
* Full event logs, you can monitor push messages for each app.
* You can decide push permissions when a third party app is registering push.
* It will prevent useless broadcasts from Xiaomi Push SDK, and it will also prevent the SDK from reading your privacy.

## Usages

The installation process is simple:

* Goto [Releases](https://github.com/MiPushFramework/MiPushFramework/releases), and download the latest release APKs (2 in total) and install them.
* Setup by following the wizard.


## Giving feedback

If you have any questions, please take a look in issues and check that if it is duplicate. (e.g. Push messages are not received.) If you cannot find any solutions, you should submit issues for each question and attach the following information:

* What is your ROM and the version of Android
* Are you using tools like Xposed?

Meanwhile, please attach your logs by using Settings - Share logs.

## Logs

The framework will save logs to private folders automatically. You can delete them by entering Settings - Advanced configurations.


## Contributing

Take a look at [Contribution Guideline](CONTRIBUTION.md)

## Known issues

* For some ROM which abusing the  framework (like 360OS) that affect the normal running of push, we can only ensure the messages are received correctly, other features which do not affect the push receiving will be ignored. For this situation, we suggest you use better ROM to have better experience.
* Third party apps on Nubia ROM would not disable their `XMPushService` and start the system push service, please try converting the system push service to a system app.
* Push can receive messages but cannot display notifications on Smartisan ROM (#143)
* The developer is a full-time student, updating will be delayed when I'm having courses.
* Some features would not available, for instance, notifications will be notified from the Push service, not target app (`MIPushNotificationHelper#setTargetPackage`).

## Acknowledgements

* Files were offered by @Rachel030219
* Android Open Source Project, MultiType, GreenDao, SetupWizardLibCompat, Condom, MaterialPreference，GreenDaoUpgradeHelper, epic, Log4a，helplib，RxJava RxAndroid，RxActivityResult，RxPermissions
* Icons were made from Coolapk @PzHown @lmnm011223 @苏沐晨风丶(Not accepted)

# License

GPL v3, you **must** obey the license.
