package com.veil.globalcustomnpcdata;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.controllers.*;

import java.io.File;
import java.io.IOException;

public class GlobalFileCopierHandler {
    private static int delayTicks = -1;
    private static World overworld;
    private static File sourceDir;
    private static File destDir;

    @SubscribeEvent
    public void onWorldCreate(WorldEvent.CreateSpawnPosition event) {
        if (event.world.isRemote || event.world.provider.dimensionId != 0) return;
        GlobalFileCopierHandler.sourceDir = MinecraftServer.getServer().getFile("customnpcs/global");
        GlobalFileCopierHandler.destDir = MinecraftServer.getServer().getFile("saves/"+MinecraftServer.getServer().getFolderName()+"/customnpcs");
        GlobalFileCopierHandler.overworld = event.world;
        GlobalFileCopierHandler.delayTicks = 5;

        // Copy the files a bit later so that they aren't overwritten by CustomNPC itself.
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (delayTicks > 0) {
            delayTicks--;
        } else if (delayTicks == 0) {
            delayTicks = -1;
            if (overworld != null) {
                try {
                    if (!sourceDir.exists()) {
                        sourceDir.mkdirs();
                    }

                    if (sourceDir.listFiles().length == 0) return;
                    System.out.println("Copying global CustomNpc files");

                    copyDirectory(sourceDir, destDir);

                    // Reload all the files
                    AnimationController.Instance.load();
                    // ClientCloneController.Instance.???
                    DialogController.Instance.load();
                    // LinkedNpcController.Instance.???
                    // PlayerDataController.Instance.??? Probably not necessary anyway
                    QuestController.Instance.load();
                    ScriptController.Instance.loadCategories();
                    ScriptController.Instance.loadPlayerScripts();
                    ScriptController.Instance.loadForgeScripts();
                    ScriptController.Instance.loadStoredData();
                    new SpawnController();
                    FactionController.getInstance().load();
                    MagicController.getInstance().load();
                    RecipeController.Instance.load();
                } catch (IOException e) {
                    System.out.println("An error occurred while trying to copy global CustomNpc files!");
                    e.printStackTrace();
                }
            }
        }
    }


    private void copyDirectory(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }

        for (File file : source.listFiles()) {
            File target = new File(dest, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, target);
            } else {
                java.nio.file.Files.copy(file.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
