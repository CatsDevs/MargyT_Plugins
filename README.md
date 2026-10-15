# MargyT Plugins — Custom Background

This repository builds a `.mtp` plugin for MargyT using GitHub Actions.

## Build on GitHub

1. Open the **Actions** tab.
2. Select **Build MargyT plugin**.
3. If no run started after a commit, choose **Run workflow**.
4. Open the completed run and download the **CustomBackgroundPlugin-mtp** artifact ZIP.
5. Extract the `.mtp` file from that ZIP.

## Install

In TikTok with MargyT installed, open **Settings and privacy → MargyT → Plugins → Install a plugin**, then choose the `.mtp` file and enable it.

## Current behavior and limitations

- Offers several dark-colour presets and a custom six-digit HEX value.
- Recolours only near-black colours received by MargyT's public `onColour(int)` hook.
- The public hook does not tell a plugin whether a colour is a background, text, or icon colour. This conservative filter avoids changing most foreground colours, but it cannot recolour every background in TikTok.
- Requires plugin API 4 because it uses the current screen helper for the HEX dialog.

Only install plugin files you trust; MargyT plugins run inside TikTok with the app's permissions.
