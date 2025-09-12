# 修复配置覆盖问题

## 问题描述
用户反馈：添加多个小智配置时，列表只显示一个配置项，新增配置项是覆盖而不是新增。

## 问题分析

### 根本原因
通过分析日志和代码，发现问题在于**数据保存和刷新的时序问题**：

1. **异步保存问题**：`saveXiaozhiConfigsToStorage()`方法使用了`apply()`进行异步保存
2. **刷新时机问题**：`refreshXiaozhiConfigs()`在保存完成前就被调用
3. **数据覆盖**：刷新时从存储重新加载数据，但此时存储中的数据还是旧的

### 具体流程
```
1. 用户添加新配置
2. addXiaozhiConfig() 被调用
3. 配置添加到内存列表
4. saveXiaozhiConfigsToStorage() 开始异步保存 (apply())
5. ConfigSelectorActivity.onResume() 被调用
6. refreshXiaozhiConfigs() 被调用
7. loadXiaozhiConfigs() 从存储重新加载数据
8. 由于apply()是异步的，存储中的数据还是旧的
9. 新添加的配置被覆盖
```

## 修复方案

### 1. 修改保存方法使用同步提交
将`apply()`改为`commit()`，确保数据立即保存到存储：

```kotlin
// 修改前
sharedPreferences.edit()
    .putString(KEY_XIAOZHI_CONFIGS, configsJson)
    .apply()  // 异步保存

// 修改后
sharedPreferences.edit()
    .putString(KEY_XIAOZHI_CONFIGS, configsJson)
    .commit()  // 同步保存
```

### 2. 同时修复Dify配置的保存方法
确保Dify配置也有相同的问题修复：

```kotlin
// 修改前
sharedPreferences.edit()
    .putString(KEY_DIFY_CONFIGS, configsJson)
    .apply()

// 修改后
sharedPreferences.edit()
    .putString(KEY_DIFY_CONFIGS, configsJson)
    .commit()
```

### 3. 添加详细调试日志
在关键位置添加日志，帮助诊断问题：

```kotlin
// ConfigEditActivity.kt
android.util.Log.d("ConfigEdit", "ConfigEditActivity初始化")
android.util.Log.d("ConfigEdit", "configType: $configType")
android.util.Log.d("ConfigEdit", "configId: $configId")
android.util.Log.d("ConfigEdit", "isEditing: $isEditing")

// 保存时
android.util.Log.d("ConfigEdit", "生成的配置ID: $newConfigId")
android.util.Log.d("ConfigEdit", "原始configId: $configId")
android.util.Log.d("ConfigEdit", "isEditing: $isEditing")
```

## 修复的文件

### ConfigViewModel.kt
- `saveXiaozhiConfigsToStorage()`: 将`apply()`改为`commit()`
- `saveDifyConfigsToStorage()`: 将`apply()`改为`commit()`

### ConfigEditActivity.kt
- 添加了详细的调试日志
- 改进了配置ID生成逻辑的日志记录

## 测试验证

### 测试步骤
1. **清除现有配置**
   - 删除所有小智配置
   - 确保列表为空

2. **添加第一个配置**
   - 配置名称：`配置1`
   - WebSocket地址：`ws://192.168.1.100:8080`
   - MAC地址：自动获取
   - Token：`token1`
   - 保存后应该显示1个配置

3. **添加第二个配置**
   - 配置名称：`配置2`
   - WebSocket地址：`ws://192.168.1.200:8080`
   - MAC地址：自动获取
   - Token：`token2`
   - 保存后应该显示2个配置

4. **验证配置列表**
   - 应该看到两个不同的配置项
   - 每个配置都有独立的编辑和删除按钮
   - 配置信息正确显示

### 预期结果
- ✅ 能够添加多个配置
- ✅ 每个配置都有唯一的ID
- ✅ 配置列表正确显示所有配置
- ✅ 编辑和删除功能正常
- ✅ 数据持久化正常

## 技术细节

### apply() vs commit()
- **apply()**: 异步保存，立即返回，不阻塞主线程
- **commit()**: 同步保存，等待保存完成后返回

### 为什么使用commit()
在配置管理的场景中，我们需要确保数据立即保存，因为：
1. 用户可能立即进行其他操作
2. 应用可能被系统回收
3. 需要确保数据一致性

### 性能考虑
虽然`commit()`是同步的，但在配置管理场景中：
1. 配置数据量很小
2. 保存频率不高
3. 数据一致性比性能更重要

## 相关日志标签

修复后可以通过以下日志标签监控：
- `ConfigViewModel`: 配置数据操作
- `ConfigEdit`: 配置编辑操作
- `ConfigSelector`: 配置列表显示

### 关键日志示例
```
ConfigEdit: ConfigEditActivity初始化
ConfigEdit: configType: xiaozhi
ConfigEdit: configId: null
ConfigEdit: isEditing: false
ConfigEdit: 生成的配置ID: [新UUID]
ConfigEdit: 添加小智配置
ConfigViewModel: 开始保存小智配置到存储，数量: 2
ConfigViewModel: 小智配置已保存到存储
```

## 总结

通过将异步保存改为同步保存，解决了配置覆盖的问题。现在用户应该能够正常添加多个小智配置，每个配置都会正确保存和显示。
