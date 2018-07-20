# 应用注册问题
遇到这种情况请按照下列顺序排查

## 列表中没有应用注册
请打开各种支持mipush的app，系统会自动注册

## 只有部分应用注册
你可能需要安装magisk模块或者使用xposed模块

### magisk或者build.prop修改
默认不修改手机厂家，适合小米手机使用

### xposed修改
模拟miui同时模拟手机厂家为xiaomi，适合非小米手机

## 最佳实践
同时使用magisk和xposed