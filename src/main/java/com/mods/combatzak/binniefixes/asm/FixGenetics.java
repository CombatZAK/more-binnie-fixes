package com.mods.combatzak.binniefixes.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.mods.combatzak.binniefixes.BinnieFixes;
import com.mods.combatzak.binniefixes.helpers.RecipeHelper;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Class transformer that changes the Binnie Genetics mod so it doesn't crash
 * 
 * @author CombatZAK
 *
 */
public class FixGenetics implements IClassTransformer {

	/**
	 * Executes the transformation on the Genetics mod
	 */
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("binnie.genetics.item.ModuleItem")) //target class found
			return transformModuleItem(basicClass); //modify it and return the edited class
		
		return basicClass; //not target class, bail out
	}
	
	private void testReturn(byte[] basicClass) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(node, 0);
		
		for (MethodNode method : node.methods) {
			if (!method.name.equals("func_77624_a")) continue;
			
			AbstractInsnNode instruction;
			boolean lineFound = false;
			for (instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
				if (instruction.getType() == AbstractInsnNode.LINE && ((LineNumberNode)instruction).line == 54) 
					lineFound = true; 
			}
		}
	}
	
	/**
	 * Transforms the ModuleItem class so that the Flutterlyzer is no longer included in the recipe
	 * @param target ModuleItem class
	 * @return modified ModuleItem class
	 * @throws Exception 
	 */
	private byte[] transformModuleItem(byte[] target) {
		//create class node to navigate the class
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(target);
		reader.accept(node, 0);
		
		for (MethodNode method : node.methods) { //iterate across all methods
			if (!method.name.equals("postInit")) continue; //only looking for the postInit method
			
			int storesFound = 0; //number of array-storage operations located
			AbstractInsnNode instruction; //a cursor to the current instruction
			
			//start at the beginning of the method and go across all instructions
			for (instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
				if (instruction.getType() == AbstractInsnNode.LINE && ((LineNumberNode)instruction).line == 121) break; //searching for line 121 the array declaration for the Analyst item
			}
			
			method.instructions.insert(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mods/combatzak/binniefixes/helpers/RecipeHelper", "addGeneticsRecipes", Type.getMethodDescriptor(Type.VOID_TYPE), false));
			
			method.instructions.insert(instruction.getNext(), new InsnNode(Opcodes.RETURN));
			break;
		}
		
		ClassWriter writer = new ClassWriter(0); //get a writer to serialize the modified class
		node.accept(writer); //serialize the class
		return writer.toByteArray(); //kick it back to ASM process
	}
}
