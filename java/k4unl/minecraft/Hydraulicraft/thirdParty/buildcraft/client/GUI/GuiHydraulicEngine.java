package k4unl.minecraft.Hydraulicraft.thirdParty.buildcraft.client.GUI;

import k4unl.minecraft.Hydraulicraft.Hydraulicraft;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicMachine;
import k4unl.minecraft.Hydraulicraft.client.GUI.HydraulicGUIBase;
import k4unl.minecraft.Hydraulicraft.containers.ContainerEmpty;
import k4unl.minecraft.Hydraulicraft.lib.Localization;
import k4unl.minecraft.Hydraulicraft.lib.config.Constants;
import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;
import k4unl.minecraft.Hydraulicraft.thirdParty.buildcraft.Buildcraft;
import k4unl.minecraft.Hydraulicraft.thirdParty.buildcraft.tileEntities.TileHydraulicEngine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GuiHydraulicEngine extends HydraulicGUIBase {
	private static ResourceLocation resLoc = new ResourceLocation(ModInfo.LID, "textures/gui/thirdPartyGen.png");
	private TileHydraulicEngine engine;
	
	public GuiHydraulicEngine(InventoryPlayer invPlayer, TileEntity ent) {
		super((IHydraulicMachine)ent, new ContainerEmpty(invPlayer), resLoc);
		
		engine = (TileHydraulicEngine)ent;
	}
	

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		drawHorizontalAlignedString(7, 3, xSize - 14, Buildcraft.blockHydraulicEngine.getLocalizedName(), true);
		drawVerticalProgressBar(40, 16, 54, 16, (float)engine.getPowerReceiver(engine.getFacing()).getEnergyStored(), (float)engine.getPowerReceiver(engine.getFacing()).getMaxEnergyStored(), Constants.COLOR_MJ, "Minecraft Joules", "MJ");
	
		int startY = 17;
		int step = (int)(Hydraulicraft.smallGuiFont.getLineHeight() / 3.2F);
		drawSmallerString(61, startY + (step * 0), EnumChatFormatting.GREEN + Localization.getString(Localization.GUI_GENERATING_ENTRY) + ":", false);
		drawSmallerString(65, startY + (step * 1), EnumChatFormatting.GREEN + "" + engine.createPower(true) + "MJ/t", false);
		drawSmallerString(61, startY + (step * 2), EnumChatFormatting.GREEN + Localization.getString(Localization.GUI_OUTPUT_ENTRY) + ":", false);
		drawSmallerString(65, startY + (step * 3), EnumChatFormatting.GREEN + "" + engine.getPowerToExtract() + "MJ/t", false);
		drawSmallerString(61, startY + (step * 4), EnumChatFormatting.GREEN + Localization.getString(Localization.GUI_USING_ENTRY) + ":", false);
		drawSmallerString(65, startY + (step * 5), EnumChatFormatting.GREEN + "" + engine.getPressureRequired() + "mBar/t", false);
		
		
		drawFluidAndPressure();
		checkTooltips(mouseX, mouseY);
	}
}
