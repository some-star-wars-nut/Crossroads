package com.Da_Technomancer.crossroads.items;

import java.util.List;

import com.Da_Technomancer.crossroads.ModConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RainIdol extends Item{
	
	private Property weatherControl = ModConfig.config.get("Misc", "Enable rain idol? (default true)", true);
	
	protected RainIdol(){
		String name = "rainIdol";
		setUnlocalizedName(name);
		setRegistryName(name);
		GameRegistry.register(this);
		this.setCreativeTab(ModItems.tabCrossroads);
		this.maxStackSize = 1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced){
		if(!weatherControl.getBoolean()){
			tooltip.add("This item has been disabled in the config. It will do nothing until re-enabled.");
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected){
		if(worldIn.isRemote || !weatherControl.getBoolean()){
			return;
		}
		
		if(isSelected){
			if(entityIn instanceof EntityPlayer){
				EntityPlayer play = (EntityPlayer) entityIn;
				if(!play.getEntityData().hasKey("rIdol")){
					play.getEntityData().setByte("rIdol", (byte) 0);
					play.getEntityData().setLong("rIdolTime", System.currentTimeMillis());
				}
				
				if(System.currentTimeMillis() - play.getEntityData().getLong("rIdolTime") > 2_000){
					play.getEntityData().setByte("rIdol", (byte) 0);
					play.getEntityData().setLong("rIdolTime", System.currentTimeMillis());
				}
				byte count = play.getEntityData().getByte("rIdol");

				if(!play.isSneaking() && play.motionY <= 0){
					if(Math.abs(count) % 2 == 1){
						count += count > 0 ? 1 : -1;
						play.getEntityData().setByte("rIdol", count);
					}
					return;
				}

				if(play.isSneaking()){
					if(count < 0){
						count = 0;
					}
					if(count % 2 == 0){
						if(++count >= 9){
							worldIn.getWorldInfo().setRaining(true);
							worldIn.getWorldInfo().setThundering(true);
							worldIn.playSound(null, play.posX, play.posY, play.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 20F, 1F);
							play.setHeldItem(EnumHand.MAIN_HAND, null);
							count = 0;
						}else{
							play.getEntityData().setLong("rIdolTime", System.currentTimeMillis());
						}
						play.getEntityData().setByte("rIdol", count);
					}
				}else{
					if(count > 0){
						count = 0;
					}
					if(count % 2 == 0){
						if(--count <= -9){
							worldIn.getWorldInfo().setRaining(false);
							worldIn.playSound(null, play.posX, play.posY, play.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 3F, 1F);
							play.setHeldItem(EnumHand.MAIN_HAND, null);
							count = 0;
						}else{
							play.getEntityData().setLong("rIdolTime", System.currentTimeMillis());
						}
						play.getEntityData().setByte("rIdol", count);
					}
				}
			}
		}
	}
}
