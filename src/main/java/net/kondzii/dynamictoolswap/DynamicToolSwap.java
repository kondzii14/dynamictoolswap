package net.kondzii.dynamictoolswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class DynamicToolSwap implements ClientModInitializer {

    private boolean wasAttacking = false;
    private String lastWarnedBlock = "";

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            wasAttacking = false;
            lastWarnedBlock = "";
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            boolean isAttacking = client.options.attackKey.isPressed();

            // Only trigger on the first tick of pressing LMB
            if (isAttacking && !wasAttacking) {
                handleAttack(client, player);
            }

            wasAttacking = isAttacking;
        });
    }

    private void handleAttack(MinecraftClient client, ClientPlayerEntity player) {
        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            Block block = client.world.getBlockState(blockHit.getBlockPos()).getBlock();
            String blockName = Registries.BLOCK.getId(block).getPath();

            ToolType needed = getToolForBlock(blockName);
            if (needed == null) return;

            int minTier = getMinTierForBlock(blockName);
            equipBestTool(player, needed, minTier, blockName);

        } else if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity) {
                equipBestTool(player, ToolType.SWORD, 1, "");
            }
        }
    }

    private void equipBestTool(ClientPlayerEntity player, ToolType needed, int minTier, String blockName) {
        int bestSlot = -1;
        int bestScore = -1;

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            int score = scoreToolForType(stack.getItem(), needed);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        if (bestSlot == -1 || bestScore == 0) return;

        // Warn if tier too low
        if (!blockName.isEmpty() && !blockName.equals(lastWarnedBlock)) {
            int toolTier = bestScore / 10;
            if (minTier == 3 && toolTier < 4) {
                player.sendMessage(Text.literal("§c[DynamicToolSwap] §fHey! What the f*** are you doing? It needs an iron pickaxe!"), false);
                lastWarnedBlock = blockName;
            } else if (minTier == 2 && toolTier < 3) {
                player.sendMessage(Text.literal("§c[DynamicToolSwap] §fHey! Don't mine that with a wooden pickaxe!"), false);
                lastWarnedBlock = blockName;
            }
        }

        // Equip it
        if (bestSlot <= 8) {
            player.getInventory().selectedSlot = bestSlot;
        } else {
            player.getInventory().swapSlotWithHotbar(bestSlot);
        }
    }

    private ToolType getToolForBlock(String name) {
        if (name.contains("stone") || name.contains("ore") || name.contains("brick")
                || name.contains("cobble") || name.contains("concrete")
                || name.contains("copper") || name.contains("iron") || name.contains("gold")
                || name.contains("obsidian") || name.contains("glass")
                || name.contains("prismarine") || name.contains("terracotta")
                || name.contains("deepslate") || name.contains("tuff")
                || name.contains("calcite") || name.contains("diorite")
                || name.contains("granite") || name.contains("andesite")
                || name.contains("basalt") || name.contains("blackstone")
                || name.contains("netherrack") || name.contains("end_stone")
                || name.contains("sandstone") || name.contains("amethyst")
                || name.contains("anvil") || name.contains("gravel")
                || name.contains("nether_brick") || name.contains("rail")
                || name.contains("lantern") || name.contains("chain")
                || name.contains("bell") || name.contains("cauldron")) {
            return ToolType.PICKAXE;
        }
        if (name.equals("dirt") || name.equals("grass_block") || name.equals("podzol")
                || name.equals("mycelium") || name.equals("gravel") || name.equals("sand")
                || name.equals("red_sand") || name.equals("soul_sand") || name.equals("soul_soil")
                || name.equals("snow") || name.equals("snow_block") || name.equals("clay")
                || name.equals("farmland") || name.equals("dirt_path") || name.equals("mud")
                || name.equals("muddy_mangrove_roots") || name.equals("rooted_dirt")
                || name.contains("coarse")) {
            return ToolType.SHOVEL;
        }
        if (name.contains("log") || name.contains("wood") || name.contains("plank")
                || name.contains("stem") || name.contains("hyphae")
                || name.contains("bamboo") || name.contains("barrel")
                || name.contains("chest") || name.contains("fence")
                || name.contains("door") || name.contains("trapdoor")
                || name.contains("stairs") || name.contains("slab")
                || name.contains("sign") || name.contains("crafting_table")
                || name.contains("bookshelf") || name.contains("campfire")
                || name.contains("leaves") || name.contains("beehive")
                || name.contains("bee_nest") || name.contains("ladder")) {
            return ToolType.AXE;
        }
        if (name.contains("hay") || name.contains("nether_wart_block")
                || name.contains("shroomlight") || name.contains("nylium")
                || name.contains("sponge") || name.contains("target")
                || name.contains("dried_kelp")) {
            return ToolType.HOE;
        }
        return null;
    }

    private int getMinTierForBlock(String name) {
        if (name.contains("diamond_ore") || name.contains("emerald_ore")
                || name.contains("ancient_debris") || name.contains("deepslate_diamond")
                || name.contains("deepslate_emerald")) return 3;
        if (name.contains("iron_ore") || name.contains("deepslate_iron")
                || name.contains("gold_ore") || name.contains("deepslate_gold")
                || name.contains("lapis_ore") || name.contains("deepslate_lapis")) return 2;
        return 1;
    }

    private int scoreToolForType(Item item, ToolType needed) {
        String itemId = Registries.ITEM.getId(item).getPath();
        String toolName = switch (needed) {
            case PICKAXE -> "pickaxe";
            case SHOVEL  -> "shovel";
            case AXE     -> "axe";
            case SWORD   -> "sword";
            case HOE     -> "hoe";
        };
        if (!itemId.contains(toolName)) return 0;
        if (itemId.startsWith("netherite_")) return 60;
        if (itemId.startsWith("diamond_"))   return 50;
        if (itemId.startsWith("iron_"))      return 40;
        if (itemId.startsWith("golden_"))    return 35;
        if (itemId.startsWith("stone_"))     return 30;
        if (itemId.startsWith("wooden_"))    return 20;
        return 0;
    }

    private enum ToolType {
        PICKAXE, SHOVEL, AXE, SWORD, HOE
    }
}
