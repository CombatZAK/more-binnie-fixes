package com.mods.combatzak.binniefixes.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.mods.combatzak.binniefixes.BinnieFixes;

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
	
	/**
	 * Transforms the ModuleItem class so that the Flutterlyzer is no longer included in the recipe
	 * @param target ModuleItem class
	 * @return modified ModuleItem class
	 */
	private byte[] transformModuleItem(byte[] target) {
		//create class node to navigate the class
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(target);
		reader.accept(node, 0);
		
		for (MethodNode method : node.methods) { //iterate across all methods
			if (!method.name.equals("postInit")) continue; //only looking for the postInit method
			
			boolean lineTargetFound = false; //lets us know when we're on the right line
			int storesFound = 0; //number of array-storage operations located
			AbstractInsnNode instruction; //a cursor to the current instruction
			
			//start at the beginning of the method and go across all instructions
			for (instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext()) {
				if (!lineTargetFound && instruction.getType() == AbstractInsnNode.LINE && ((LineNumberNode)instruction).line == 121) lineTargetFound = true; //searching for line 121 the array declaration for the Analyst item
				if (!lineTargetFound) continue; //skip till we're at line 121
				
				//this only happens AT line 121
				if (instruction.getType() == AbstractInsnNode.INSN && instruction.getOpcode() == 83) storesFound++; //count the number of stores
				if (storesFound == 2) { //when we get to the second store...
					instruction = instruction.getNext().getNext(); //advance the cursor twice (past DUP to ICONST_2 - index)
					break;
				}
			}
			
			while (!(instruction.getType() == AbstractInsnNode.INSN && instruction.getOpcode() == Opcodes.AASTORE)) { //going to delete all the instructions that load the flutterlyzer into the array, stopping at the AASTORE instruction
				instruction = instruction.getNext(); //advance the cursor
				method.instructions.remove(instruction.getPrevious()); //delete the isntruction behind it
			} //this deletes everything between DUP and AASTORE
			
			method.instructions.insertBefore(instruction, new InsnNode(Opcodes.ICONST_2)); //readd the intval 2 to the stack - this is the array index
			String fieldName = BinnieFixes.obfuscated ? "field_151045_i" : "diamond"; //this chooses the right field name based on whether or not we're obfuscated
			method.instructions.insertBefore(instruction, new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/init/Items", fieldName, "Lnet/minecraft/item/Item;")); //add an instruction to get the static field field for a vanilla diamond and put it on the stack
			
			lineTargetFound = false; //reset the line target flag
			storesFound = 0; //reset the AASTORE count flag
			for (;instruction != null; instruction = instruction.getNext()) { //iterate across instructions from the current position
				if (!lineTargetFound && instruction.getType() == AbstractInsnNode.LINE && ((LineNumberNode)instruction).line == 130) lineTargetFound = true; //this time we're looking for line 30, the array for recipe components for the registry
				if (!lineTargetFound) continue; //skip back to the loop till we're at line 130
				
				//this only happens at line 130
				if (instruction.getType() == AbstractInsnNode.INSN && instruction.getOpcode() == Opcodes.AASTORE) storesFound++; //count all AASTORE instructions, we want the third element, so we need to find two of these
				if (storesFound == 2) { //when we found our second AASTORE...
					instruction = instruction.getNext().getNext(); //advance past the DUP instruction to ICONST_2
					break;
				}
			}
			
			//going to delete all the instructions that add the Lepidopterist Database to the array -- same code as for the analyst here
			while (!(instruction.getType() == AbstractInsnNode.INSN && instruction.getOpcode() == Opcodes.AASTORE)) { //we stop iterating when we find the third AASTORE instruction
				instruction = instruction.getNext(); //advance the cursor first
				method.instructions.remove(instruction.getPrevious()); //then delete the previous instruction
			}
			
			method.instructions.insertBefore(instruction, new InsnNode(Opcodes.ICONST_2)); //readd ICONST_2 to the stack (array index)
			method.instructions.insertBefore(instruction, new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/init/Items", fieldName, "Lnet/minecraft/item/Item;")); //get the static vanilla diamond item and put it on the stack
			
			break; //all changes are complete here, stop looking at methods
		}
		
		ClassWriter writer = new ClassWriter(0); //get a writer to serialize the modified class
		node.accept(writer); //serialize the class
		return writer.toByteArray(); //kick it back to ASM process
	}
}
