# Bedrock Client (Android)
Launcher app + floating in-game module menu for Minecraft Bedrock.
## Build on GitHub
1. Create a repo, push these files.
2. Open Actions > "Build APK" (runs on push), download the `BedrockClient-debug` artifact.
3. Install the APK, allow "display over other apps", tap Launch Minecraft.
## Status
Module toggles are saved in SharedPreferences ("modules"). In-game effects need a native hook layer (next step).
