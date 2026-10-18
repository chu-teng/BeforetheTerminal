# 终点站抵达之前 · Before the Terminal

**简体中文** | [English](README.en.md)

在一列驶向终点站的快车上，寻找同伴、隐藏角色，并在谎言与真相之间活到最后。

BTT 是 Minecraft 的多人社交推理模组，作为独立游戏模式运行于 Wathe 与 HarpyModLoader 之上。

**当前版本：0.7.0-h1.4 · Alpha**。已通过构建、资源检查、双端启动及服务端回合生命周期检查；仍缺少充分的真人多人测试。欢迎提交问题，附上版本、日志与复现步骤。

## 玩法

- **近 100 个角色**：乘客、凶手、中立三类，分为执法、行动、信息、辅助、主犯、从犯、独行、外人、狂人九个子类别。
- **五种模式**：剧本、轮抽、对决、辩论、追忆；奇遇为每局带来额外规则。
- **三条特质**：通用、阵营专属及连接玩家的关系特质，共同改变你的处境。
- **理智与狂气**：角色技能、需求、处决、误杀、醉酒与尾声相互影响。
- **局后记录**：花环奖励与五页、89 个自定义进度。
- **多地图**：数据包驱动地图注册、切换与投票。

通常需要 **6–24 人**；辩论、追忆为 **6–18 人**。对决中只有乘客与凶手两大阵营；独行达成条件后永久醉酒并获得一把枪。追忆隐藏开幕与技能中的角色信息，通过纸条猜测自己的角色。

## 安装与开局

Minecraft **1.21.1**、Java **21**、Fabric Loader **0.17.2+**。客户端与服务器均需安装 BTT 及所需依赖，并保持版本一致。

| 依赖 | 版本 |
| --- | --- |
| Fabric API | 1.21.1 对应版本（开发基线 0.116.7+1.21.1） |
| [Wathe](https://modrinth.com/mod/XoU7Lf7D) | 1.4.1-1.21.1 |
| [HarpyModLoader](https://modrinth.com/mod/harpymodloader) | 1.2.5-h1.4 |
| Ratatouille | 1.4.3-1.21.1 |
| YetAnotherConfigLib | 3.8.0+1.21.1-fabric 或兼容更新版本 |
| Simple Voice Chat | 可选，推荐用于近距离语音 |

**不包含地图。** 需要自行准备兼容的 Wathe 地图及相应数据包。自制地图须配置操纵台、瞭望椅、吊灯与电线机关等游戏设施。

大厅右键指南针选择模式、剧本和奇遇；使用 `/maps` 选择已注册地图。拉响汽笛开始对局，或由管理员使用 `/wathe:start btt:before_the_terminal <地图标识符>`。

默认按 **G** 使用技能、**E** 打开背包及可用商店、**K** 阅读指南。局内提示显示于动作栏。管理员可用 `/forceRole` 与 `/forceModifier` 指定下一局角色和特质。

## 从源码构建

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

请到 [GitHub Issues](https://github.com/chu-teng/BeforetheTerminal/issues) 提交问题。写明 BTT、Wathe、HML 版本、游戏模式、角色、复现步骤，并附相关日志。发布前请移除日志中的私人信息。

## 致谢与许可

BTT 的代码和文档采用 [GNU LGPL v3 或更新版本](LICENSE.txt)。Copyright © 2025–2026 hae-si and Before the Terminal contributors。第三方内容保留各自许可。

玩法灵感来自《Town of Salem》《Blood on the Clocktower》《Goose Goose Duck》等作品。BTT 是独立的 Wathe 附加模组，并非 NoellesRoles 分支。

| 项目 | 作者 | 许可与用途 |
| --- | --- | --- |
| Wathe | doctor4t | All Rights Reserved；基础游戏，独立下载 |
| HarpyModLoader | agmass | LGPL-3.0；角色与特质 API |
| Ratatouille | doctor4t | All Rights Reserved；Wathe 依赖 |
| YetAnotherConfigLib | isXander | LGPL-3.0-or-later；配置 |
| Fabric API | FabricMC | Apache-2.0；平台 API |
| Cardinal Components API | Ladysnake | MIT；由 Wathe 内嵌提供 |
| Simple Voice Chat | maxhenkel | All Rights Reserved；可选近距离语音 |
| Gradle Wrapper | Gradle contributors | Apache-2.0；构建工具，保留其许可证声明 |

### 改编代码

最后核对：2026-10-18。公开源码于同日清理普通注释，保留必要许可声明。

以下文件包含改编自 [NoellesRoles](https://github.com/agmass/NoellesRoles)（agmass，LGPL-3.0）的代码，继续以 LGPL-3.0 授权。文件经过修改：调整包名与 modid、删除不适用分支、重命名，并按 BTT 机制修订。路径相对于 `dev/hae/btt/`；`client/` 对应客户端源码目录。

- `component/AbilityState.java`、`component/DisguiseState.java`、`component/ScavengerState.java`（DisguiseState 限时伪装修订：2026-10-11；AbilityState 本人同步：2026-10-09；ScavengerState 显示阈值修订：2026-10-10）
- `component/CurseState.java`（来源 NoellesRoles `voodoo/VoodooPlayerComponent`）、`component/ExecutionerState.java`（`executioner/ExecutionerPlayerComponent`）、`component/CorpseState.java`（`coroner/BodyDeathReasonComponent`）：均为 LGPL-3.0 改编；2026-10-12 修正空目标、重置状态与初始／技术角色字段。
- `client/mixin/game/StowawayHidePsychosisItems.java`
- `client/mixin/game/ExecutionerHudMixin.java`（部分分支；2026-10-14 更新死亡与结局领域包引用并清理历史注释，行为不变）
- `client/spirit/SpiritCamera.java`、`client/renderer/DoorHighlightRenderer.java`
- `mixin/game/FakeRevolverMixin.java`（改编自 NoellesRoles `mixin/jester/FakeRevolverMixin`；BTT 改动：判定物改为 `btt:fake_revolver`；2026-09-29）

### 资产与分发范围

短效果音采用 CC0-1.0。囚笼、遥控器等项目贴图由项目作者提供；四张基于 Wathe 的理智界面改色贴图经授权使用。文学及歌曲短引文的使用许可已经作者确认。上述内容的许可不扩展至第三方模组、地图或完整音乐。

本仓库与 jar 不附带第三方模组 jar、地图存档或尾声音乐。模型和原版声音通过资源标识符引用。可选尾声音乐资源包须另行取得，未启用时不影响游玩。许可证全文随 jar 以 `LICENSE_btt` 附带，本文件与英文版也随 jar 分发。
