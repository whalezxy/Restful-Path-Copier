# Restful Path Copier (IntelliJ IDEA Plugin)

## 功能
- 在 Spring Controller 的方法映射注解处（`@RequestMapping`/`@GetMapping`/`@PostMapping` 等）显示左侧 gutter 图标。
- 点击图标后自动拼接：类上的 `@RequestMapping` + 方法上的映射路径。
- 将完整路径复制到系统剪贴板。

## 支持注解
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@PatchMapping`

## 本地打包（生成可安装 ZIP）
> 由于不同机器网络与 Gradle 环境不同，建议直接在 IDEA 中执行。

1. 用 IntelliJ IDEA 打开本项目目录。
2. 等待 Gradle 同步完成。
3. 在 Gradle 面板执行任务：
   - `Tasks -> intellij platform -> buildPlugin`
4. 成功后安装包位置：
   - `build\distributions\restful-path-plugin-1.0.0.zip`

## 插件安装
1. 打开 IDEA：`Settings -> Plugins -> ⚙ -> Install Plugin from Disk...`
2. 选择上面的 zip 文件并安装。
3. 重启 IDEA。

## 使用方式
1. 打开 Spring Controller Java 文件。
2. 在有映射注解的方法左侧 gutter 点击复制图标。
3. 完整请求路径会被复制到剪贴板。
