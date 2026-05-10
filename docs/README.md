# InterfaceX 官方网站

这是 InterfaceX IntelliJ IDEA 插件的官方静态网站。

## 🚀 快速开始

### 本地预览

如果你想在本地预览网站，可以使用以下方法：

#### 方法 1: 使用 Python HTTP 服务器
```bash
cd docs
python3 -m http.server 8080
```
然后在浏览器中访问 `http://localhost:8080`

#### 方法 2: 使用 Node.js http-server
```bash
npm install -g http-server
cd docs
http-server -p 8080
```

#### 方法 3: 直接在浏览器中打开
直接双击 `index.html` 文件即可在浏览器中查看（部分功能可能受限）。

## 📁 项目结构

```
docs/
├── index.html      # 主页面
├── styles.css      # 样式文件
├── script.js       # JavaScript 交互逻辑
└── .gitignore      # Git 忽略配置
```

## 🎨 自定义网站

### 修改内容

1. **更新插件信息**: 编辑 `index.html` 中的文本内容
2. **修改样式**: 编辑 `styles.css` 文件
3. **添加功能**: 在 `script.js` 中添加新的 JavaScript 代码

### 更换图标和截图

将你的图片文件放在项目的适当位置，然后更新 HTML 中的引用路径。

## 🌐 部署到 GitHub Pages

### 自动部署（推荐）

网站已通过 GitHub Actions 配置自动部署：

1. **启用 GitHub Pages**:
   - 进入仓库的 Settings → Pages
   - 在 "Source" 下选择 "GitHub Actions"
   - 保存设置

2. **触发部署**:
   - 推送代码到 `main` 或 `master` 分支
   - 或者手动触发工作流（Actions → Deploy Static Website → Run workflow）

3. **访问网站**:
   - 部署完成后，你的网站将在 `https://YOUR_USERNAME.github.io/interfacex/` 可用

### 手动部署

如果你想手动部署：

1. 构建网站文件
2. 将 `docs/` 目录的内容推送到 `gh-pages` 分支
3. 在 Settings → Pages 中选择 `gh-pages` 分支作为源

## 🔧 GitHub Actions 工作流

工作流配置文件位于 `.github/workflows/deploy-website.yml`

### 触发条件

- 推送到 `main` 或 `master` 分支，且以下文件发生变化：
  - `docs/**`
  - `README.md`
  - `CHANGELOG.md`
  - `.github/workflows/deploy-website.yml`
- 手动触发（workflow_dispatch）

### 工作流程

1. **Checkout**: 检出代码
2. **Setup Node.js**: 设置 Node.js 环境
3. **Install dependencies**: 安装依赖（如果有 package.json）
4. **Build website**: 构建网站
5. **Upload artifact**: 上传构建产物
6. **Deploy to GitHub Pages**: 部署到 GitHub Pages

## 📝 注意事项

1. **插件 ID**: 记得在 `index.html` 中将 `YOUR_PLUGIN_ID` 替换为实际的 JetBrains 插件 ID
2. **GitHub 用户名**: 将链接中的 `kaylves` 确认是否为正确的用户名
3. **资源路径**: 确保所有图片资源的引用路径正确
4. **CHANGELOG.md**: 网站会自动加载根目录的 CHANGELOG.md 文件

## 🐛 问题排查

### 网站无法访问

1. 检查 GitHub Pages 是否已启用
2. 确认 GitHub Actions 工作流是否成功运行
3. 查看 Actions 标签页中的部署日志

### 样式或脚本未加载

1. 检查浏览器控制台是否有错误
2. 确认文件路径是否正确
3. 清除浏览器缓存后重试

### CHANGELOG 未显示

1. 确认根目录存在 `CHANGELOG.md` 文件
2. 检查浏览器网络请求是否成功加载该文件
3. 查看浏览器控制台的错误信息

## 📞 支持

如有问题，请提交 [GitHub Issue](https://github.com/kaylves/interfacex/issues)

## 📄 许可证

MIT License
