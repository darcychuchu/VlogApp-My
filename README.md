# Android API数据映射应用

## 项目概述

这是一个Android应用项目，主要功能是实现一个可配置的API到数据库映射系统。用户可以在设置页面添加JSON类型的API和key，系统会将API数据解析并映射到本地SQLite数据库中，然后通过列表和详情页面展示这些数据。

## 项目架构

### 技术栈

- **开发语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构模式**：MVVM (Model-View-ViewModel)
- **依赖注入**：Hilt
- **网络请求**：Retrofit + OkHttp
- **数据库**：SQLite (通过自定义LocalDataHelper实现)
- **导航**：Jetpack Navigation Compose

### 项目结构

```
com.vlog.my
├── MainActivity.kt                 # 主活动
├── MyApplication.kt                # 应用程序类
├── data
│   ├── ApiResponse.kt              # API响应模型
│   ├── model
│   │   ├── ArticlesItems.kt                # 通用项目数据模型
│   │   ├── ArticlesCategories.kt           # 分类数据模型
│   │   └── UserScripts.kt            # API配置数据模型
│   └── repository
│       ├── ItemsRepository.kt      # 项目数据仓库
│       └── CategoriesRepository.kt # 分类数据仓库
├── di
│   ├── LocalDataHelper.kt           # 数据库助手
│   └── NetworkModule.kt            # 网络模块
├── navigation
│   └── VlogNavigation.kt           # 导航组件
├── parser
│   ├── ScriptParser.kt                # API解析器接口
│   ├── ArticlesScriptParser.kt            # JSON API解析器实现
│   └── ArticlesMappingConfig.kt            # 映射配置模型
└── screens
    ├── home
    │   ├── HomeScreen.kt           # 首页界面
    │   └── HomeViewModel.kt        # 首页视图模型
    ├── settings
    │   ├── SettingsScreen.kt       # 设置界面
    │   ├── ApiConfigScreen.kt      # API配置界面
    │   └── SettingsViewModel.kt    # 设置视图模型
    ├── list
    │   ├── ItemListScreen.kt       # 项目列表界面
    │   └── ItemListViewModel.kt    # 项目列表视图模型
    └── detail
        ├── ItemDetailScreen.kt     # 项目详情界面
        └── ItemDetailViewModel.kt  # 项目详情视图模型
```

## 数据库设计

### 表结构

#### 1. items表

```sql
CREATE TABLE items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    pic TEXT,
    content TEXT,
    category_id INTEGER,
    tags TEXT
);
```

#### 2. categories表

```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    parent_id INTEGER
);
```

#### 3. api_configs表

```sql
CREATE TABLE api_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    api_key TEXT,
    mapping_config TEXT NOT NULL
);
```

## 开发计划

### 阶段1：基础架构搭建

- [x] 创建项目基础结构
- [x] 设置导航系统
- [x] 实现基本UI界面（首页和设置页面）
- [x] 创建数据库助手类，实现items和categories表
- [x] 创建数据模型类（Items, Categories, ApiConfig）

### 阶段2：设置页面和API配置

- [x] 实现设置页面UI
- [x] 创建API配置界面
- [x] 实现API配置的增加、编辑和删除功能
- [x] 实现API配置的本地存储

### 阶段3：数据解析器

- [x] 设计通用解析器接口
- [x] 实现JSON解析器
- [x] 创建字段映射配置界面
- [x] 实现API数据到数据库的映射逻辑
- [x] 实现API数据获取功能


- [x] 扩展数据解析器API功能 （ 尝试了简化处理，发现和原设想有偏离，比如扩展ApiConfig，原url，更改为 listUrl,和 cateUrl，然后在mappingConfig，配置元对应listUrl，这样做个简单处理，主要我当前测试两个api里面，一个是有cate，一个是没有cate，独立的url，导致后续测试，会出现异常情况）      需要继续实现：扩展数据解析器功能，允许用户添加多个API配置。这涉及修改ApiConfigScreen和数据库结构，确保每个API配置都有唯一标识，并且能够正确关联items和categories表中的数据。需要在数据库中添加source_api_id字段，用于标识数据来源的API，同时修改解析器逻辑，确保从不同API获取的数据能够正确区分和存储。我将更新ApiConfigScreen界面，添加多API管理功能，并修改数据库结构和解析逻辑。



### 阶段4：数据展示

- [x] 实现项目列表页面
- [x] 实现项目详情页面
- [x] 实现数据库查询和展示
- [ ] 优化UI和用户体验（1，编辑api配置页面，需要填写完api 的 url，就可以对itemsMapping、categoriesMapping 的"urlTypeField" 值做对应设置，并完成对路径rootpath的配置，设置完毕后就自动保存，2，配置字段映射，需要判断，是否配置了urlTypeField和rootpath的配置才能开启，未配置，不能开启，3，打开字段映射配置页面后，如果在items表映射，应该先去请求前面的配置去请求api，需要获取对应url和路径，或者全部api的名称，更改页面配置为选择名称的方式，不用去手动填写与打字的配置，这样操作会更简单便捷一些，让用户能够快速配置，而且不会出错）



### 阶段5：实现分享机制

- [x] 实现数据库和json小程序绑定
- [ ] 实现可以打包分享到api接口

    /// post api Url = API_BASE_URL + /app-shared
    @PostMapping("/app-shared","/app-shared/")
    fun appSharedPostApi(
        @PathVariable("username") usernameKey: String,//用户名
        @RequestParam("token") tokenKey: String,//用户的token
        @RequestParam("appFile") appFile: MultipartFile,//小程序数据库文件
        @RequestParam("title") title: String?,//小程序标题
        @RequestParam("description") description: String?,//小程序简介
        @RequestParam("tags") tags: String?,//小程序标签
        @RequestParam("shareContent") shareContent: String?, //配置文件
        request: HttpServletRequest
    ): ApiResponse<Any> {
    }



- [ ] 实现可以下载api接口的小程序
- [ ] 实现简单的json的版本迭代和更新功能

### 阶段6：测试和优化

- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能优化
- [ ] 用户体验改进

## 当前进度

目前已完成阶段1、阶段2和阶段3，4的所有任务。具体完成情况如下：

1. **阶段1（基础架构搭建）**：已完成项目基础结构的创建、导航系统的设置、基本UI界面的实现、数据库助手类的创建和数据模型类的定义。

2. **阶段2（设置页面和API配置）**：已完成设置页面UI的实现、API配置界面的创建、API配置的增加/编辑/删除功能以及API配置的本地存储。

3. **阶段3（数据解析器）**：已完成设计通用解析器接口、实现JSON解析器、创建字段映射配置界面、实现API数据到数据库的映射逻辑，以及实现API数据获取功能。用户现在可以在API配置界面点击"获取数据"按钮，系统会根据配置从API获取数据并保存到本地数据库中。

下一步将进入阶段4，实现项目列表页面和详情页面，以及数据库查询和展示功能。