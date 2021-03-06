package com.Da_Technomancer.crossroads.tileentities.heat;

import javax.annotation.Nullable;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.EnergyConverters;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import com.Da_Technomancer.crossroads.fluids.BlockDistilledWater;
import com.Da_Technomancer.crossroads.items.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SaltReactorTileEntity extends TileEntity implements ITickable{

	private FluidStack content = null;
	private FluidStack dContent = null;
	private static final int WATER_USE = 200;
	private static final int CAPACITY = 16 * WATER_USE;
	private boolean init = false;
	private double temp;
	private ItemStack inventory = null;

	@Override
	public void update(){
		if(worldObj.isRemote){
			return;
		}

		if(!init){
			temp = EnergyConverters.BIOME_TEMP_MULT * getWorld().getBiomeForCoordsBody(pos).getFloatTemperature(getPos());
			init = true;
		}

		if(temp >= -272 && dContent != null && dContent.amount >= WATER_USE && CAPACITY - (content == null ? 0 : content.amount) >= WATER_USE && inventory != null && inventory.getItem() == ModItems.dustSalt){
			--temp;
			if((dContent.amount -= WATER_USE) <= 0){
				dContent = null;
			}

			if(--inventory.stackSize <= 0){
				inventory = null;
			}

			if(content == null){
				content = new FluidStack(FluidRegistry.WATER, WATER_USE);
			}else{
				content.amount += WATER_USE;
			}
			markDirty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		content = FluidStack.loadFluidStackFromNBT(nbt);
		if(nbt.hasKey("dCon")){
			dContent = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("dCon"));
		}

		this.init = nbt.getBoolean("init");
		this.temp = nbt.getDouble("temp");

		if(nbt.hasKey("inv")){
			inventory = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		if(content != null){
			content.writeToNBT(nbt);
		}
		if(dContent != null){
			nbt.setTag("dCon", dContent.writeToNBT(new NBTTagCompound()));
		}

		nbt.setBoolean("init", this.init);
		nbt.setDouble("temp", this.temp);

		if(inventory != null){
			nbt.setTag("inv", inventory.writeToNBT(new NBTTagCompound()));
		}

		return nbt;
	}

	private final IFluidHandler innerFluidHandler = new InnerFluidHandler();
	private final IFluidHandler waterFluidHandler = new WaterFluidHandler();
	private final IFluidHandler dWaterFluidHandler = new DWaterFluidHandler();
	private final IHeatHandler heatHandler = new HeatHandler();
	private final IItemHandler itemHandler = new ItemHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			if(facing == null){
				return (T) innerFluidHandler;
			}

			if(facing != EnumFacing.UP){
				return (T) waterFluidHandler;
			}

			return (T) dWaterFluidHandler;
		}

		if(capability == Capabilities.HEAT_HANDLER_CAPABILITY && (facing == EnumFacing.DOWN || facing == null)){
			return (T) heatHandler;
		}
		
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) itemHandler;
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != EnumFacing.DOWN){
			return true;
		}

		if(capability == Capabilities.HEAT_HANDLER_CAPABILITY && (facing == EnumFacing.DOWN || facing == null)){
			return true;
		}
		
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}
		
		return super.hasCapability(capability, facing);
	}

	private class ItemHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot == 0 ? inventory : null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot != 0 || stack == null || stack.getItem() != ModItems.dustSalt){
				return stack;
			}
			
			int amount = Math.min(16 - (inventory == null ? 0 : inventory.stackSize), stack.stackSize);
			
			if(!simulate){
				inventory = new ItemStack(ModItems.dustSalt, amount + (inventory == null ? 0 : inventory.stackSize));
				markDirty();
			}
			
			return amount == stack.stackSize ? null : new ItemStack(ModItems.dustSalt, stack.stackSize - amount);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			return null;
		}
	}
	
	private class InnerFluidHandler implements IFluidHandler{

		@Override
		public IFluidTankProperties[] getTankProperties(){
			return new FluidTankProperties[] {new FluidTankProperties(content, CAPACITY, false, true), new FluidTankProperties(dContent, CAPACITY, true, false)};
		}

		@Override
		public int fill(FluidStack resource, boolean doFill){
			if(resource == null || resource.getFluid() != BlockDistilledWater.getDistilledWater()){
				return 0;
			}

			int maxFill = Math.min(resource.amount, CAPACITY - (dContent == null ? 0 : dContent.amount));
			if(doFill){
				if(dContent == null){
					dContent = new FluidStack(BlockDistilledWater.getDistilledWater(), maxFill);
				}else{
					dContent.amount += maxFill;
				}
			}

			return maxFill;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain){
			if(content == null || resource == null || resource.getFluid() != FluidRegistry.WATER){
				return null;
			}

			int maxDrain = Math.min(resource.amount, content.amount);
			if(doDrain && (content.amount -= maxDrain) <= 0){
				content = null;
			}

			return new FluidStack(FluidRegistry.WATER, maxDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain){
			if(content == null || maxDrain == 0){
				return null;
			}

			maxDrain = Math.min(maxDrain, content.amount);
			if(doDrain && (content.amount -= maxDrain) <= 0){
				content = null;
			}

			return new FluidStack(FluidRegistry.WATER, maxDrain);
		}
	}

	private class WaterFluidHandler implements IFluidHandler{

		@Override
		public IFluidTankProperties[] getTankProperties(){
			return new FluidTankProperties[] {new FluidTankProperties(content, CAPACITY, false, true)};
		}

		@Override
		public int fill(FluidStack resource, boolean doFill){
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain){
			if(content == null || resource == null || resource.getFluid() != FluidRegistry.WATER){
				return null;
			}

			int maxDrain = Math.min(resource.amount, content.amount);
			if(doDrain && (content.amount -= maxDrain) <= 0){
				content = null;
			}

			return new FluidStack(FluidRegistry.WATER, maxDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain){
			if(content == null || maxDrain == 0){
				return null;
			}

			maxDrain = Math.min(maxDrain, content.amount);
			if(doDrain && (content.amount -= maxDrain) <= 0){
				content = null;
			}

			return new FluidStack(FluidRegistry.WATER, maxDrain);
		}
	}

	private class DWaterFluidHandler implements IFluidHandler{

		@Override
		public IFluidTankProperties[] getTankProperties(){
			return new FluidTankProperties[] {new FluidTankProperties(dContent, CAPACITY, true, false)};
		}

		@Override
		public int fill(FluidStack resource, boolean doFill){
			if(resource == null || resource.getFluid() != BlockDistilledWater.getDistilledWater()){
				return 0;
			}

			int maxFill = Math.min(resource.amount, CAPACITY - (dContent == null ? 0 : dContent.amount));
			if(doFill){
				if(dContent == null){
					dContent = new FluidStack(BlockDistilledWater.getDistilledWater(), maxFill);
				}else{
					dContent.amount += maxFill;
				}
			}

			return maxFill;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain){
			return null;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain){
			return null;
		}
	}

	private class HeatHandler implements IHeatHandler{
		private void init(){
			if(!init){
				temp = EnergyConverters.BIOME_TEMP_MULT * getWorld().getBiomeForCoordsBody(pos).getFloatTemperature(getPos());
				init = true;
			}
		}

		@Override
		public double getTemp(){
			init();
			return temp;
		}

		@Override
		public void setTemp(double tempIn){
			init = true;
			temp = tempIn;
		}

		@Override
		public void addHeat(double heat){
			init();
			temp += heat;

		}
	}
}
