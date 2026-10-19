# Before the Terminal · 终点站抵达之前

**English** | [简体中文](README.zh-CN.md)

Find allies, conceal your role, and survive a train journey through lies and truths, all the way to the final stop.

Before the Terminal is a multiplayer social deduction mod for Minecraft, requiring Wathe and HarpyModLoader.

**Current version: 0.7.0-h1.4.** Extensive testing with human players is still needed. Issue reports are welcome; please include versions, logs and reproduction steps.

## Gameplay

- **Roles**: 92 roles across three main categories—passengers, murderers and neutrals—and nine subcategories: law enforcement, action, information and support passengers; principal and accomplice murderers; and lone, outsider and mad neutrals. Passengers and murderers oppose each other. Lone neutrals win by fulfilling their own objectives, while outsiders form their own factions.

- **Factions**: Passengers, murderers, lone neutrals and outsiders. Outsiders include expanding factions such as the Cult, Coven, Remnant, Legion, Glutton, Astral, Gardener and Cold Wave.

- **Modes**: Rounds for 6–24 players across five modes: Script, Draft, Duel, Debate and Reminiscence. Each round also features a randomly selected encounter that introduces special rules.

- **Systems**: Sanity, madness, executions, wrongful kills, drunkenness, talents, abilities, epilogues and mechanisms.

- **Traits**: Each player receives three traits. Passengers, murderers, lone neutrals and outsiders all have faction-specific traits; relationship traits are assigned between pairs of players.

- **Maps**: Data-pack driven worlds supporting multiple maps, compatible with the Spark format.

- **Advancements**: Wreaths are awarded at the end of each round: Laurel, Spider Lily, Iris, Lily, Gladiolus and Artichoke. Nearly a hundred custom advancements span five tabs: Before the Terminal, Until We Meet Again, And Then There Were None, Uninvited Guests and Wandering the Crossroads.

## Playing

Requires Minecraft **1.21.1**, Java **21** and Fabric Loader **0.17.2+**. Install BTT and its required dependencies on both client and server, using matching versions.

| Dependency | Version |
| --- | --- |
| Fabric API | For 1.21.1; development baseline 0.116.7+1.21.1 |
| [Wathe](https://modrinth.com/mod/XoU7Lf7D) | 1.4.1-1.21.1 |
| [HarpyModLoader](https://modrinth.com/mod/harpymodloader) | 1.2.5-h1.4 |
| Ratatouille | 1.4.3-1.21.1 |
| YetAnotherConfigLib | 3.8.0+1.21.1-fabric or a compatible newer release |
| Simple Voice Chat | Required for voice communication |

**Maps are not included.** Supply a map and its data pack separately. For example, the Harpy Express map requires rooms and spawn points to be added to its data pack. Community maps are recommended. Place a control panel at the front of the train and a lookout seat at the rear, along with one or two sets each of chandelier and wire mechanisms in the corridors; these are required for normal gameplay.

Right-click the compass to select the mode, script and encounter. Use `/maps` to select a registered map. Pull the whistle to start a round, or use `/wathe:start btt:before_the_terminal <map_identifier>` as an administrator.

Default keys: **G** for abilities, **E** for inventory and available shops, and **K** for the guide. Use `/forceRole` and `/forceModifier` to specify roles and traits for the next round.

## Building

Requires JDK 21. The Gradle Wrapper uses Gradle 9.2.0 with Loom 1.14.10.

Download these development dependencies from their official projects and place them in `libs/` at the repository root, keeping these filenames:

- `wathe-1.4.1-1.21.1.jar`
- `harpymodloader-1.2.5-h1.4.jar`
- `midnightlib-1.5.7-fabric.jar` (development runtime only; bundled by Wathe in normal installations)

Gradle downloads the remaining dependencies. On Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Use `./gradlew` on Linux/macOS. Accept the Minecraft EULA before running a dedicated server for the first time. The mod is built as `build/libs/BeforetheTerminal-0.7.0-h1.4.jar`; the `-sources.jar` contains the corresponding source code.

## Feedback

Report issues through [GitHub Issues](https://github.com/chu-teng/BeforetheTerminal/issues). Include BTT, Wathe and HML versions, game mode, roles, reproduction steps and relevant logs.

## Credits and licensing

BTT code and documentation are licensed under the [GNU LGPL version 3 or later](LICENSE.txt). Copyright © 2025–2026 hae-si and Before the Terminal contributors. Third-party content retains its own licensing.

Gameplay draws inspiration from Town of Salem, Blood on the Clocktower and Goose Goose Duck.

Sound effects use CC0-1.0. Optional community epilogue music resource packs may be used. Full license texts are packaged as `LICENSE_btt`, together with this file and the Chinese README.

| Project | Author | License and use |
| --- | --- | --- |
| Wathe | doctor4t | All Rights Reserved; base game, downloaded separately |
| HarpyModLoader | agmass | LGPL-3.0; role and trait APIs |
| Ratatouille | doctor4t | All Rights Reserved; Wathe dependency |
| YetAnotherConfigLib | isXander | LGPL-3.0-or-later; configuration |
| Fabric API | FabricMC | Apache-2.0; platform APIs |
| Cardinal Components API | Ladysnake | MIT; bundled by Wathe |
| Simple Voice Chat | maxhenkel | All Rights Reserved; voice support |
| Gradle Wrapper | Gradle contributors | Apache-2.0; build tooling, original license notices retained |

### Adapted code

Last reviewed: 2026-10-18. Ordinary comments were removed from the public source snapshot on that date; required license notices were retained.

The following files contain code adapted from [NoellesRoles](https://github.com/agmass/NoellesRoles), by agmass, licensed under LGPL-3.0. These adaptations remain licensed under LGPL-3.0. Modifications include package and mod ID changes, removal of unused branches, renamed identifiers and adjustments to BTT mechanics. Paths are relative to `dev/hae/btt/`; `client/` refers to the client source tree.

- `component/AbilityState.java` (owner synchronization updated 2026-10-09), `component/DisguiseState.java` (timed disguise updated 2026-10-11), `component/ScavengerState.java` (display threshold updated 2026-10-10).
- `component/CurseState.java`, from `voodoo/VoodooPlayerComponent`; `component/ExecutionerState.java`, from `executioner/ExecutionerPlayerComponent`; `component/CorpseState.java`, from `coroner/BodyDeathReasonComponent`. Updated 2026-10-12 for null targets, reset state, and initial/technical role fields.
- `client/mixin/game/StowawayHidePsychosisItems.java`.
- Parts of `client/mixin/game/ExecutionerHudMixin.java` (package references and historical comments updated 2026-10-14).
- `client/spirit/SpiritCamera.java` and `client/renderer/DoorHighlightRenderer.java`.
- `mixin/game/FakeRevolverMixin.java`, from `mixin/jester/FakeRevolverMixin` (adapted item check and documentation, 2026-09-29).
