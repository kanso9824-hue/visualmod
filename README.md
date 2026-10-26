# Visual Mod (Fabric 1.21.11)

Client-side mod: trigger bot (random delay + crits), aim assist, fullbright, ESP (glow).
Menu: press **J**. Config: `.minecraft/config/visualmod.json`.

Use only in singleplayer / on your own server. Cheats on public servers get you banned.

## Build via GitHub
1. Create a repo, push this folder.
2. Open the **Actions** tab, wait for the `build` workflow.
3. Download artifact `visualmod`, put the jar (not `-sources`) into `.minecraft/mods` with Fabric API.

Local: `gradle build` (Gradle 9+, JDK 21) -> `build/libs/`.
