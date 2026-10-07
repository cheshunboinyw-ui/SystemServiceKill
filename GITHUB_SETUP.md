# GitHub Actions 自动构建指南

## 📦 这个项目已经准备好自动构建了

项目已配置 GitHub Actions，上传到 GitHub 后会自动编译 APK。

## 🚀 上传到 GitHub 的步骤

### 方法1：使用 GitHub 网页端（最简单，推荐）

1. **创建 GitHub 仓库**
   - 打开浏览器，访问 https://github.com/new
   - 仓库名填：`SystemServiceKiller`
   - 设为 **Public**（公开）或 **Private**（私有）都可以
   - **不要**勾选 "Add a README file"
   - 点击 **Create repository**

2. **上传项目文件**
   - 在新仓库页面，点击 **uploading an existing file**
   - 选择本地文件上传：
     * 从手机的 `白子虬/SystemServiceKiller/` 目录
     * 选择**所有文件和文件夹**
   - 等待上传完成
   - 点击 **Commit changes**

3. **触发自动构建**
   - 上传后，GitHub Actions 会自动开始构建
   - 点击仓库顶部的 **Actions** 标签
   - 看到黄色圆圈表示正在构建，绿色勾表示完成

4. **下载 APK**
   - 构建完成后，点击具体的构建记录
   - 滚动到底部，找到 **Artifacts** 区域
   - 点击 **app-debug** 下载 APK
   - 解压 ZIP 得到 `app-debug.apk`

---

### 方法2：使用 Git 命令行（需要 Termux）

如果你会用 Git，可以用命令行：

1. **安装 Git（如果没有）**
```bash
pkg install git
```

2. **配置 Git**
```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"
```

3. **进入项目目录**
```bash
cd /storage/emulated/0/白子虬/SystemServiceKiller
```

4. **初始化并推送**
```bash
# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: System Service Killer"

# 添加远程仓库（替换成你的 GitHub 用户名）
git remote add origin https://github.com/你的用户名/SystemServiceKiller.git

# 推送
git push -u origin master
```

5. **查看构建**
   - 推送后，访问 https://github.com/你的用户名/SystemServiceKiller/actions
   - 等待构建完成
   - 下载 Artifacts 中的 APK

---

## 📥 构建完成后

1. GitHub Actions 会生成两个 APK（如果构建成功）：
   - **app-debug.apk** - 调试版本（总是会生成）
   - **app-release-unsigned.apk** - 发布版本（可能失败，因为没有签名）

2. **下载 app-debug.apk** 安装即可使用

3. 如果需要签名的 Release 版本，需要配置签名密钥（进阶操作）

---

## 🔧 进阶：配置自动签名（可选）

如果想要发布版本自动签名，需要：

1. 生成签名密钥
2. 上传到 GitHub Secrets
3. 修改 build.gradle 配置

详细步骤见：https://developer.android.com/studio/publish/app-signing

---

## ⚠️ 注意事项

1. **首次构建时间**
   - 首次构建需要下载依赖，大约需要 5-10 分钟
   - 后续构建会更快（有缓存）

2. **构建失败怎么办**
   - 点击 Actions 标签查看错误日志
   - 通常是依赖下载问题，重新运行即可
   - 点击失败的构建，右上角有 **Re-run jobs** 按钮

3. **私有仓库**
   - 如果设为 Private，只有你能看到和下载
   - GitHub Actions 对私有仓库也免费（有额度限制）

---

## 📱 手机直接操作

如果没有电脑，可以：

1. **用手机浏览器**访问 GitHub
2. **切换到桌面模式**（浏览器菜单）
3. 按照「方法1」的步骤上传文件

或者使用 GitHub 手机 App（更方便）：
- 下载：https://play.google.com/store/apps/details?id=com.github.android

---

## 🆘 需要帮助？

如果遇到问题：
1. 截图错误信息
2. 复制 Actions 日志
3. 在项目 Issues 提问

---

**祝你构建成功！🎉**
