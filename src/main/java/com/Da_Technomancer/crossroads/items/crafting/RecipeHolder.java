package com.Da_Technomancer.crossroads.items.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.Da_Technomancer.crossroads.integration.JEI.FluidCoolingRecipe;
import com.Da_Technomancer.crossroads.integration.JEI.GrindstoneRecipe;
import com.Da_Technomancer.crossroads.integration.JEI.HeatingCrucibleRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

public final class RecipeHolder{

	/**
	 * CraftingStack is input, the array is the outputs. HAVE NO MORE THAN 3 ITEMSTACKS IN THE ARRAY.
	 * 
	 */
	public static final HashMap<ICraftingStack, ItemStack[]> grindRecipes = new HashMap<ICraftingStack, ItemStack[]>();

	/**
	 * Block is input, blockstate is the created block, Double1 is heat created,
	 * Double2 is the limit.
	 * 
	 */
	public static final HashMap<Block, Triple<IBlockState, Double, Double>> envirHeatSource = new HashMap<Block, Triple<IBlockState, Double, Double>>();

	/**
	 * Fluid is input, Integer is the amount required, ItemStack is output,
	 * Double1 is maximum temperature, and Double2 is heat added on craft.
	 * 
	 */
	public static final HashMap<Fluid, Pair<Integer, Triple<ItemStack, Double, Double>>> fluidCoolingRecipes = new HashMap<Fluid, Pair<Integer, Triple<ItemStack, Double, Double>>>();

	/**
	 * A list of all recipes, Item Array are the ingredients, and itemstack is
	 * output. A list for poisonous potato recipes and mashed potato recipes.
	 */
	protected static final ArrayList<Pair<ICraftingStack[], ItemStack>> mashedBoboRecipes = new ArrayList<Pair<ICraftingStack[], ItemStack>>();
	protected static final ArrayList<Pair<ICraftingStack[], ItemStack>> poisonBoboRecipes = new ArrayList<Pair<ICraftingStack[], ItemStack>>();

	public static final ArrayList<Object> JEIWrappers = new ArrayList<Object>();

	/*
	 * Converts the versions of the recipes used internally into fake recipes
	 * for JEI. Not called unless JEI is installed.
	 */
	public static void rebind(){
		for(Entry<ICraftingStack, ItemStack[]> rec : grindRecipes.entrySet()){
			JEIWrappers.add(new GrindstoneRecipe(rec));
		}
		for(Entry<Fluid, Pair<Integer, Triple<ItemStack, Double, Double>>> rec : fluidCoolingRecipes.entrySet()){
			JEIWrappers.add(new FluidCoolingRecipe(rec));
		}
		JEIWrappers.add(new HeatingCrucibleRecipe(true));
		JEIWrappers.add(new HeatingCrucibleRecipe(false));
	}

	public static ItemStack recipeMatch(boolean poisonous, ArrayList<EntityItem> itemEnt){
		if(itemEnt == null){
			return null;
		}

		ArrayList<ItemStack> items = new ArrayList<ItemStack>();

		for(EntityItem it : itemEnt){
			if(it.getEntityItem() == null || it.getEntityItem().stackSize != 1){
				return null;
			}
			items.add(it.getEntityItem());
		}

		if(items.size() != 3){
			return null;
		}

		if(poisonous){
			for(Pair<ICraftingStack[], ItemStack> craft : poisonBoboRecipes){
				ArrayList<ItemStack> itemCop = new ArrayList<ItemStack>();
				itemCop.addAll(items);

				for(ICraftingStack cStack : craft.getLeft()){
					for(ItemStack stack : items){
						if(itemCop.contains(stack) && cStack.softMatch(stack)){
							itemCop.remove(stack);
							break;
						}
					}

					if(itemCop.size() == 0){
						return craft.getRight();
					}
				}
			}
		}else{
			for(Pair<ICraftingStack[], ItemStack> craft : mashedBoboRecipes){
				ArrayList<ItemStack> itemCop = new ArrayList<ItemStack>();
				itemCop.addAll(items);

				for(ICraftingStack cStack : craft.getLeft()){
					for(ItemStack stack : items){
						if(itemCop.contains(stack) && cStack.softMatch(stack)){
							itemCop.remove(stack);
							break;
						}
					}
				}

				if(itemCop.size() == 0){
					return craft.getRight();
				}
			}
		}

		return null;
	}
}
