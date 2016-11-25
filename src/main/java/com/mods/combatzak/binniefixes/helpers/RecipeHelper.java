package com.mods.combatzak.binniefixes.helpers;

import binnie.botany.Botany;
import binnie.core.BinnieCore;
import binnie.core.Mods;
import binnie.extrabees.ExtraBees;
import binnie.extratrees.ExtraTrees;
import binnie.genetics.Genetics;
import binnie.genetics.item.GeneticsItems;
import cpw.mods.fml.common.registry.GameRegistry;
import forestry.plugins.PluginManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Helps (carefully) register recipes for Binnie's Mods
 * 
 * @author CombatZAK
 *
 */
public class RecipeHelper {
	/**
	 * Carefully registers the genetics recipes for Binnie's Mods which may or may not rely on lepidopterology
	 */
	public static void addGeneticsRecipes() {
		Item[] analystComponents = new Item[] { Mods.Forestry.item("beealyzer"), Mods.Forestry.item("treealyzer"), Items.diamond }; //start the base array without including flutterlyzer
		if (PluginManager.Module.LEPIDOPTEROLOGY.isEnabled()) //check if the lepidoperology plugin is enabled in forestry
			analystComponents[2] = Mods.Forestry.item("flutterlyzer"); //add the flutterlyzer in lieu of another diamond if necessary
		
		//use the same gross registration logic binnie did
		for (Item a : analystComponents)
			for (Item b : analystComponents)
				for (Item c : analystComponents)
					if (a != b && a != c && b != c)
						GameRegistry.addShapedRecipe(new ItemStack(Genetics.analyst), " a ", "bic", " d ", 'a', a, 'b', b, 'c', c, 'i', GeneticsItems.IntegratedCircuit.get(1), 'd', Items.diamond);
		
		Item[] registryComponents = new Item[] { ExtraBees.dictionary, ExtraTrees.itemDictionary, Items.diamond, Botany.database }; //get all the components of the the registry, starting with diamond instead of the lepidopterologist database
		if (PluginManager.Module.LEPIDOPTEROLOGY.isEnabled()) //check if the lepidopterology is enabled in forestry
			registryComponents[2] = ExtraTrees.itemDictionaryLepi; //replace the diamond with the lepidopterologist database if appropriate
		
		//use the same gross logic for the registry that binnie did
		if (BinnieCore.isBotanyActive() && BinnieCore.isExtraTreesActive() && BinnieCore.isExtraBeesActive())
			for (Item a : registryComponents)
				for (Item b : registryComponents)
					for (Item c : registryComponents)
						for (Item d : registryComponents)
							if (a != b && a != c && a != d && b != c && b != d && c != d)
								GameRegistry.addShapedRecipe(new ItemStack(Genetics.registry), " a ", "bic", " d ", 'a', a, 'b', b, 'c', c, 'd', d, 'i', GeneticsItems.IntegratedCircuit.get(1));
	}
}
