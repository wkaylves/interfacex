# InterfaceX 标签系统优化 - 实施报告

## 改动总览

共修改 **14 个文件**，**+899 / -257 行**

---

## 改动 1: 多标签组合过滤

> `activeTagFilter: String` → `Set<String>`，AND 语义

### 涉及文件

| 文件 | 改动 |
|------|------|
| `InterfaceXSimpleTreeStructure.java` | 字段 `String activeTagFilter` → `LinkedHashSet<String> activeTagFilters`；新增 `toggleTagFilter()`；`applyTagFilter()` 改为委托 `toggleTagFilter`；`clearTagFilter()` 改为 `clear()`；新增 `getActiveTagFilters()` 返回不可变 Set |
| `InterfaceXNavigator.java` | 新增 `toggleTagFilter()` 和 `getActiveTagFilters()` 方法 |
| `TagOperationDialog.java` | 双击标签调用 `toggleTagFilter` 替代 `applyTagFilter`（切换语义） |

### 过滤逻辑

```
无过滤时 → 显示所有 TagNode + 默认节点
有过滤时 → 对每个 filter tag 收集服务列表 → 取交集（AND） → 只保留同时满足所有标签的接口
```

---

## 改动 2: 多标签交叉显示

> 一个接口有多个标签时，出现在每个标签节点下

### 涉及文件

| 文件 | 改动 |
|------|------|
| `InterfaceXSimpleTreeStructure.java` | `CategoryNode.buildChildren()` 中遍历接口的所有标签，将 ServiceNode 添加到每个对应 tag 的列表 |

### 关键代码变更

```java
// 改动前：只取第一个标签
String firstTag = tags.get(0).getTagName();
tagToServicesMap.computeIfAbsent(firstTag, k -> new ArrayList<>()).add(serviceNode);

// 改动后：遍历所有标签
for (TagEntity tag : tags) {
    tagToServicesMap.computeIfAbsent(tag.getTagName(), k -> new ArrayList<>()).add(serviceNode);
}
```

---

## 改动 3: 批量打标签

> 树中多选接口后右键可批量打标签/移除标签

### 涉及文件

| 文件 | 改动 |
|------|------|
| `InterfaceXPopupMenu.java` | `openTagDialog()` 使用 `getSelectedNodes()` 收集所有选中 ServiceNode；菜单文字动态切换 "标签..." / "批量标签..." |
| `TagOperationDialog.java` | 构造函数支持 `List<InterfaceItem>`；`loadTags()` 计算 applied/partialApplied 状态；`applyTag()`/`removeTag()`/`addNewTag()` 遍历所有 items 批量操作 |

### TagItem 状态

| 状态 | 含义 |
|------|------|
| `applied = true` | 标签已应用到**所有**选中接口 |
| `partialApplied = true` | 标签只应用到**部分**选中接口（显示为 ◐ 半选状态） |
| 两者都 false | 标签未应用到任何选中接口 |

---

## 改动 4: 全局标签搜索

> Tool Window 顶部增加搜索栏

### 涉及文件

| 文件 | 改动 |
|------|------|
| `InterfaceXNavigatorPanel.java` | 新增 `createTagSearchPanel()` 方法；树上方增加 `JBTextField` 搜索栏 + "×" 清除按钮；`DocumentListener` 防抖 200ms；模糊匹配已有标签名后调用 `toggleTagFilter` |

### 搜索行为

```
输入文字 → 防抖 200ms → 查询所有标签名 → 模糊匹配
  ├─ 匹配 0 个 → clearTagFilter()
  ├─ 匹配 1 个 → toggleTagFilter(精确匹配)
  └─ 匹配多个 → toggleTagFilter(完全匹配优先，否则第一个)
点击 "×" → 清空输入 + clearTagFilter()
```

---

## 改动 5: tagValue 支持

> 支持标签携带可选的值信息（如 `priority:high`）

### 涉及文件

| 文件 | 改动 |
|------|------|
| `TagQuickSelectorPanel.java` | `addTagToCurrent()` 解析 `tagName:value` 格式；placeholder 更新为 "添加标签(支持 name:value)..."；TagChip 显示 `name:value` |
| `TagOperationDialog.java` | `addNewTag()` 支持 `tagName:value` 格式；`parseTagInput()` 工具方法；placeholder 更新 |

### 输入格式

```
priority        → tagName=priority, tagValue=null
priority:high   → tagName=priority, tagValue=high
version: v2     → tagName=version, tagValue=v2
```

---

## 改动 6: 标签排序支持

> 标签在树中可按自定义顺序显示

### 涉及文件

| 文件 | 改动 |
|------|------|
| `TagEntity.java` | 新增 `sortOrder` 字段 |
| `SchemaManager.java` | V2 迁移：`ALTER TABLE tag ADD COLUMN sort_order INTEGER DEFAULT 0` |
| `TagDao.java` | `insert()` 增加 `sort_order`；`mapRow()` 增加 `sortOrder` 映射；新增 `updateSortOrder()` |
| `StorageBackend.java` | 新增 `updateTagSortOrder()` 接口方法 |
| `SqliteStorageBackend.java` | 实现 `updateTagSortOrder()` |
| `XmlStorageBackend.java` | 实现 `updateTagSortOrder()`；`saveTag()`/`mapTagElement()` 适配 sortOrder |
| `StorageAdapter.java` | 新增 `updateTagSortOrder()` 透传方法 |
| `InterfaceXSimpleTreeStructure.java` | `buildChildren()` 收集每个标签的 sortOrder 并排序 |
| `TagOperationDialog.java` | TagItem 增加 `sortOrder` 字段；`loadTags()` 按 sortOrder 排序；首次使用时自动初始化排序值；新增 "↑/↓" 上移/下移按钮 |

### 排序逻辑

```
首次加载（所有 sortOrder=0）→ 按字母顺序自动初始化（间隔 10）
手动调整 → 交换相邻标签的 sortOrder 值
树构建 → 按 sortOrder 升序排列 TagNode
```

---

## 验证方案

| 场景 | 验证步骤 |
|------|----------|
| 多标签交叉显示 | 给同一接口打 2+ 标签，验证它出现在每个标签节点下 |
| 多标签过滤 | 依次点击多个标签 chip，验证树只显示同时拥有这些标签的接口 |
| 批量打标签 | Ctrl+多选接口 → 右键 → 批量标签 → 验证所有接口都被打上标签 |
| 全局搜索 | 在搜索栏输入标签名 → 验证树过滤到对应标签 |
| tagValue | 添加标签时输入 `priority:high` → 验证 chip 显示 `priority:high`，tooltip 显示值 |
| 标签排序 | 在标签对话框点击 ↑/↓ → 验证树中标签顺序同步变化 |