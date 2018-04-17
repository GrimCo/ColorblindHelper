package grimco.colorblindhelper.transform;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import static org.objectweb.asm.Opcodes.ALOAD;

import static org.objectweb.asm.Opcodes.IFEQ;

import static org.objectweb.asm.Opcodes.ILOAD;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;


public class ColorblindHelperTransformer implements IClassTransformer
{
	
	public static final Logger logger = LogManager.getLogger("colorblindhelper");
	
	protected static class ObfAwareName
	{
		final String deobf, srg;
		
		public ObfAwareName(String deobf, String srg)
		{
			this.deobf = deobf;
			this.srg = srg;
		}
		
		public String getName()
		{
			return ColorblindHelperPlugin.isRuntineDeobfEnabled ? srg : deobf;
		}
		
		public boolean equals(String obj)
		{
			if(obj!=null)
			{
				return obj.equals(deobf) || obj.equals(srg);
			}
			
			return false;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof String)
			{
				return obj.equals(deobf) || obj.equals(srg);
			}
			else if(obj instanceof ObfAwareName)
			{
				return ((ObfAwareName) obj).deobf.equals(deobf) && ((ObfAwareName)obj).srg.equals(srg);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return super.hashCode();
		}
	}
	
	protected static abstract class Transform
	{
		abstract void transform(Iterator<MethodNode> methods);
	}
	
	
	static final String renderItemClass = "net.minecraft.client.renderer.RenderItem";
	static final ObfAwareName renderItemOverlayIntoGUIMethod = new ObfAwareName("renderItemOverlayIntoGUI", "func_180453_a");
	static final ObfAwareName renderItemDisplayName = new ObfAwareName("renderItemOverlayIntoGUI", "func_184391_a");
	
	
	private final boolean inDev = System.getProperty("INDEV") != null;
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		
		if (inDev && FMLLaunchHandler.side() == Side.SERVER) {
			// Eclipse's compiler suffers from https://bugs.openjdk.java.net/browse/JDK-6695379
			// Filter out methods that are known to be effected from this in a declared development environment only.
			// When compiled with a proper compiler, this will not be needed.
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);
			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode methodNode = methods.next();
				if (methodNode.name.equals("getClientGuiElement") && methodNode.desc.contains("GuiScreen")) {
					methods.remove();
				}
			}
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(cw);
			basicClass = cw.toByteArray();
		}
		
		// Item Overlay Rendering hook
		if (transformedName.equals(renderItemClass)) {
			return transform(basicClass, renderItemClass, renderItemDisplayName, new Transform() {
				@Override
				void transform(Iterator<MethodNode> methods) {
					int done = 0;
					while (methods.hasNext()) {
						MethodNode m = methods.next();
						if (renderItemOverlayIntoGUIMethod.equals(m.name)) {
							
							InsnList toAdd = new InsnList();
							toAdd.add(new VarInsnNode(ALOAD, 2));
							toAdd.add(new VarInsnNode(ILOAD, 3));
							toAdd.add(new VarInsnNode(ILOAD, 4));
							toAdd.add(new MethodInsnNode(INVOKESTATIC, "grimco/colorblindhelper/transform/ColorblindHelperMethods", "renderItemOverlayIntoGUI",
									"(Lnet/minecraft/item/ItemStack;II)V", false));
							
							boolean primed = false, onframe = false, applied = false;
							Label target = null;
							for (int i = 0; i < m.instructions.size(); i++) {
								AbstractInsnNode next = m.instructions.get(i);
								
								// (1) find "if (stack.getItem().showDurabilityBar(stack)) {"
								if (!primed && target == null && next.getOpcode() == INVOKEVIRTUAL && next instanceof MethodInsnNode) {
									if ("showDurabilityBar".equals(((MethodInsnNode) next).name)) { // Forge method, never obf'ed
										primed = true;
									}
								}
								
								// (2) where is the matching "}"?
								if (primed && next.getOpcode() == IFEQ && next instanceof JumpInsnNode) {
									target = ((JumpInsnNode) next).label.getLabel();
									primed = false;
								}
								
								// (3) insert our callback there
								if (target != null && next instanceof LabelNode&& ((LabelNode) next).getLabel() == target) {
									onframe = true;
									continue;
								}
								if (onframe && next instanceof FrameNode) {
									m.instructions.insert(next, toAdd);
									done++;
									applied = true;
									break;
								}
							}
							if (!applied) {
								logger.info("Transforming failed. Applying ersatz patch...");
								m.instructions.insert(toAdd);
								logger.warn("Ersatz patch applied, things may break!");
								done++;
							}
							break;
						}
					}
					if (done != 2) {
						logger.info("Transforming failed.");
					}
				}
			});
		}
		
		return basicClass;
	}
	
	protected final static byte[] transform(byte[] classBytes, String className, ObfAwareName methodName, Transform transformer)
	{
		logger.info("Transforming Class [" + className + "], Method [" + methodName.getName() + "]");
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(classBytes);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> methods = classNode.methods.iterator();
		
		transformer.transform(methods);
		
		ClassWriter cw = new ClassWriter(0);
		classNode.accept(cw);
		logger.info("Transforming " + className + " Finished.");
		return cw.toByteArray();
	}
}
