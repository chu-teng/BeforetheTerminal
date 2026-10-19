# 终点站抵达之前 · Before the Terminal

[English](README.md) | **简体中文**

在一列驶向终点站的快车上进行的社交推理游戏。

终点站抵达之前是 Minecraft 的多人社交推理模组，依赖 Wathe 与 HarpyModLoader。

**当前版本：0.7.0-h1.4**。等待充分的真人多人测试。欢迎提交问题，附上版本、日志与复现步骤。

## 玩法

- **角色**：乘客、凶手、中立三个角色类别，执法乘客、行动乘客、信息乘客、辅助乘客、主犯凶手、从犯凶手、独行中立、外人中立、狂人中立九个小类别。乘客与凶手互相对抗，独行中立按各自条件获胜，外人中立自成阵营。共 92 个角色。

- **阵营**：分为乘客、凶手、独行、外人等阵营，外人有教团、魔女会、残魂、军团、饕餮、星界、花匠、寒潮等扩张性的阵营。

- **模式**：6–24 人一局。剧本、轮抽、对决、辩论、追忆五个模式，每局随机产生的特殊规则——奇遇。

- **系统**：理智、狂气、处决、误杀、醉酒、天赋、技能、尾声、机关等系统。

- **特质**：每人三个特质，乘客、凶手、独行、外人皆有专属特质，关系特质在两个人之间分配。

- **地图**：数据包驱动的多地图存档，兼容 Spark 格式。

- **进度**：结局结算授予花环。月桂、彼岸、鸢尾、百合、剑兰、洋蓟。五个进度页，近百个自定义进度：终点站前、有缘再会、无人生还、不速之客、徘徊歧路。

## 游玩

Minecraft **1.21.1**、Java **21**、Fabric Loader **0.17.2+**。客户端与服务器均需安装 BTT 及所需依赖，并保持版本一致。

| 依赖                                                        | 版本                               |
| --------------------------------------------------------- | -------------------------------- |
| Fabric API                                                | 1.21.1 对应版本（开发基线 0.116.7+1.21.1） |
| [Wathe](https://modrinth.com/mod/XoU7Lf7D)                | 1.4.1-1.21.1                     |
| [HarpyModLoader](https://modrinth.com/mod/harpymodloader) | 1.2.5-h1.4                       |
| Ratatouille                                               | 1.4.3-1.21.1                     |
| YetAnotherConfigLib                                       | 3.8.0+1.21.1-fabric 或兼容更新版本      |
| Simple Voice Chat                                         | 必须，用于语音                          |

**不包含地图。** 需要自行准备地图及相应数据包，如使用哈比快车地图需要向数据包录入房间和出生点，推荐使用社区地图。你必须在车头和车尾分别放置操纵台和瞭望椅，在走廊各放置一到两组吊灯机关和电线机关，否则无法正常游玩。

右键指南针选择模式、剧本和奇遇。使用 `/maps` 选择已注册地图。拉响汽笛开始对局，或由管理员使用 `/wathe:start btt:before_the_terminal <地图标识符>`。

默认按 **G** 使用技能、**E** 打开背包及可用商店、**K** 阅读指南。可用 `/forceRole` 与 `/forceModifier` 指定下一局角色和特质。

## 构建

需要 JDK 21。Gradle Wrapper 使用 Gradle 9.2.0，Loom 1.14.10。

先从各项目官方渠道下载以下开发依赖，放入仓库根目录 `libs/`（请保持文件名）：

- `wathe-1.4.1-1.21.1.jar`
- `harpymodloader-1.2.5-h1.4.jar`
- `midnightlib-1.5.7-fabric.jar`（仅开发运行时补充；正式环境由 Wathe 内嵌提供）

其他依赖由 Gradle 下载。Windows：

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Linux/macOS 使用 `./gradlew`。专用服首次运行需接受 Minecraft EULA。产物位于 `build/libs/BeforetheTerminal-0.7.0-h1.4.jar`；`-sources.jar` 为对应源码。

## 反馈

请到 [GitHub Issues](https://github.com/chu-teng/BeforetheTerminal/issues) 提交问题。写明 BTT、Wathe、HML 版本、游戏模式、角色、复现步骤，并附相关日志。

## 致谢与许可

BTT 的代码和文档采用 [GNU LGPL v3 或更新版本](LICENSE.txt)。Copyright © 2025–2026 chu-teng and Before the Terminal contributors。第三方内容保留各自许可。

玩法灵感来自《Town of Salem》《Blood on the Clocktower》《Goose Goose Duck》等作品。

音效采用 CC0-1.0。可以使用社区的可选尾声音乐资源包。许可证全文随 jar 以 `LICENSE_btt` 附带，CREDIT.md 及双语 README 也随 jar 分发。

部分工具类改编自 [NoellesRoles](https://github.com/agmass/NoellesRoles)（agmass，LGPL-3.0），继续以 LGPL-3.0 授权。修改包括包名与 modid 调整、删除不适用分支、重命名及 BTT 机制修订，见[CREDIT.md](CREDIT.md)。

| 项目                      | 作者                  | 许可与用途                         |
| ----------------------- | ------------------- | ----------------------------- |
| Wathe                   | doctor4t            | All Rights Reserved；基础游戏，独立下载 |
| HarpyModLoader          | agmass              | LGPL-3.0；角色与特质 API            |
| Ratatouille             | doctor4t            | All Rights Reserved；Wathe 依赖  |
| YetAnotherConfigLib     | isXander            | LGPL-3.0-or-later；配置          |
| Fabric API              | FabricMC            | Apache-2.0；平台 API             |
| Cardinal Components API | Ladysnake           | MIT；由 Wathe 内嵌提供              |
| Simple Voice Chat       | maxhenkel           | All Rights Reserved；语音支持      |
| Gradle Wrapper          | Gradle contributors | Apache-2.0；构建工具，保留其许可证声明      |
