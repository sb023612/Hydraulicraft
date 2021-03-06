package k4unl.minecraft.Hydraulicraft.client.renderers.items;

import k4unl.minecraft.Hydraulicraft.client.renderers.consumers.RendererHydraulicPiston;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class RendererHydraulicPistonItem implements IItemRenderer {

	private static RendererHydraulicPiston t = new RendererHydraulicPiston();
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch(type){
	        case ENTITY: {
	        	//GL11.glRotatef(90, 0, 0, 1);
	            render(-0.5F, 0.0F, -0.5F, 1.0F, 3);
	            return;
	        }
	        case EQUIPPED: {
	            //GL11.glRotated(180, 0, 1, 0);
	            //GL11.glTranslatef(0.5F, 0.0F, 0.0F);
	            render(-0.2F, 0.4F, 0.3F, 0.8F, 4);
	            return;
	        }
	        case EQUIPPED_FIRST_PERSON: {
	            //GL11.glRotated(180, 0, 1, 0);
	            render(-1.0F, 1F, 0.4F, 0.7F, 4);
	            return;
	        }
	        case INVENTORY: {
	            render(0.0F, -0.1F, 0.0F, 1.0F, 3);
	            return;
	        }
	        default:
	            return;
	    }
	}
	
	private void render(float x, float y, float z, float scale, int rotation){
		GL11.glScalef(scale, scale, scale);
		t.doRender(null, x, y, z, 0, rotation);
	}

}
