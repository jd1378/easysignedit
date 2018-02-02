package com.javadmnjd.easysignedit.util;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class BlockPlace {

    public static void makePlayerPlace(Location<World> blockLoc, Player player, BlockState blockState) {
        blockLoc.getBlock().get(Keys.DIRECTION).ifPresent(direction -> blockLoc.getExtent().placeBlock(blockLoc.getBlockPosition(), blockState, direction, player.getProfile()));

    }

}
