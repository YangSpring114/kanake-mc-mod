package com.xyazh.kanake;

import com.xyazh.kanake.common.ConfigLoader;
import com.xyazh.kanake.gen.GenOreHarmoniumCrystal;
import com.xyazh.kanake.init.LoopThread;
import com.xyazh.kanake.init.RegistryHandler;
import com.xyazh.kanake.network.*;
import com.xyazh.kanake.particle.ModParticles;
import com.xyazh.kanake.proxy.ProxyBase;
import com.xyazh.kanake.recipes.brewing.MyBrewing;
import com.xyazh.kanake.recipes.furnace.MyFurnace;
import com.xyazh.kanake.recipes.mono.MonoRecipes;
import com.xyazh.kanake.util.Reference;
import com.xyazh.kanake.world.ModWorlds;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = Kanake.MODID, name = Kanake.NAME, version = Kanake.VERSION,dependencies="before:baubles@[1.5.0,)")
public class Kanake
{
    public static final String MODID = "kanake";
    public static final String NAME = "Kannmein na kenndaimahou";
    public static final String VERSION = "0.1.13.beta";

    public static SimpleNetworkWrapper network;

    public static Logger logger;

    public static Random rand = new Random();

    @Mod.Instance
    public static Kanake instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static ProxyBase proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {


        LoopThread.creatThread();

        logger = event.getModLog();
        RegistryHandler.preInitRegistries(event);
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        ConfigLoader configLoader = new ConfigLoader(event);

        GameRegistry.registerWorldGenerator(new GenOreHarmoniumCrystal(),130);

        network.registerMessage(new KooriEntityHandler(), KooriEntityPacket.class, 3, Side.CLIENT);
        network.registerMessage(new ManaHandler(), ManaPacket.class, 4, Side.CLIENT);
        network.registerMessage(new PlayerManaHandler(), PlayerManaPacket.class, 5, Side.CLIENT);


        if(Side.CLIENT.equals(event.getSide())){
            preInitClient(event);
        }else {
            preInitServer(event);
        }

        ModWorlds.registerAllDim();

        MonoRecipes.addMonoRecipes();
        MyBrewing.addBrewingRecipes();
        MyFurnace.addFurnaceRecipes();
    }

    public void preInitClient(FMLPreInitializationEvent event){
        //network.registerMessage(new SpawnParticlesHandler(), SpawnParticlesPacket.class, 1, Side.CLIENT);
    }

    public void preInitServer(FMLPreInitializationEvent event){

    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        if(Side.CLIENT.equals(event.getSide())){
            ModParticles.registerParticles();
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RegistryHandler.postInitReg();
    }

    @EventHandler
    public static void serverInit(FMLServerStartingEvent event) {
        RegistryHandler.serverRegistries(event);
    }
}
