NSPick - Picareta Super for Spigot 1.8

Features:
- /nspick command to receive the special diamond pickaxe named "&4&lPICARETA SUPER" (displayed with dark red + bold).
- Enchantments: Silk Touch, Efficiency V, Unbreaking III. Mending is added only if the server's API provides it (Mending was introduced in later Minecraft versions; 1.8 servers likely won't have it).
- When mining with the pickaxe, it breaks a 3x3 (9 blocks) area centered on the broken block.

Building:
- This project uses Maven. You may need to install Spigot 1.8 into your local Maven repository (via BuildTools) or adjust the dependency to match your local setup.
- Run: mvn clean package
- Place the resulting jar in your server's plugins folder.

Notes:
- Mending is not available in Minecraft 1.8. The plugin attempts to add it only if present.
- The 3x3 break happens always when using the item. If you want it to require sneaking, edit the event handler to check p.isSneaking().

