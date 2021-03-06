package com.Da_Technomancer.crossroads.items;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.MiscOperators;
import com.Da_Technomancer.crossroads.API.rotary.IRotaryHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Speedometer extends Item{

	public Speedometer(){
		String name = "speedometer";
		setUnlocalizedName(name);
		setRegistryName(name);
		GameRegistry.register(this);
		this.setCreativeTab(ModItems.tabCrossroads);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		TileEntity te = worldIn.getTileEntity(pos);

		if(te != null && te.hasCapability(Capabilities.ROTARY_HANDLER_CAPABILITY, facing.getOpposite())){
			if(!worldIn.isRemote){

				IRotaryHandler gear = te.getCapability(Capabilities.ROTARY_HANDLER_CAPABILITY, facing.getOpposite());

				playerIn.addChatComponentMessage(new TextComponentString("Speed: " + MiscOperators.betterRound(gear.getMotionData()[0], 3) + ", Energy: " + MiscOperators.betterRound(gear.getMotionData()[1], 3)));
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

}
