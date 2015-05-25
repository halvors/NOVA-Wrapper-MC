/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nova.wrapper.mc1710.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import nova.core.game.Game;
import nova.core.recipes.crafting.ShapedCraftingRecipe;

/**
 * @author Stan
 */
public class ShapedRecipeOre extends ShapedOreRecipe {
	private final ShapedCraftingRecipe recipe;

	public ShapedRecipeOre(Object[] contents, ShapedCraftingRecipe recipe) {
		super((ItemStack) Game.instance.nativeManager.toNative(recipe.getNominalOutput().get()), contents);

		this.recipe = recipe;
	}

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		return recipe.matches(MCCraftingGrid.get(inventory));
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		return ((ItemStack) Game.instance.nativeManager.toNative(recipe.getCraftingResult(MCCraftingGrid.get(inventory)))).copy();
	}
}
