package k4unl.minecraft.Hydraulicraft.thirdParty;

import java.util.List;

import k4unl.minecraft.Hydraulicraft.TileEntities.generator.TileHydraulicPump;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicGenerator;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicMachine;
import k4unl.minecraft.Hydraulicraft.baseClasses.MachineEntity;
import k4unl.minecraft.Hydraulicraft.lib.config.Ids;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;

public class WailaProvider implements IWailaDataProvider {

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		
		if(accessor.getTileEntity() instanceof IHydraulicMachine){
			IHydraulicMachine mEnt = (IHydraulicMachine) accessor.getTileEntity();
			
			int stored = mEnt.getHandler().getStored();
			int max = mEnt.getMaxStorage();
			
			float pressure = mEnt.getHandler().getPressure();
			float maxPressure = mEnt.getMaxPressure(mEnt.getHandler().isOilStored());
	
			currenttip.add("Fl: " + stored + "/" + max + " mBuckets (" + (int)(((float)stored / (float)max) * 100) + "%)");
			currenttip.add("Pr: " + pressure + "/" + maxPressure + " mBar (" + (int)(((float)pressure / (float)maxPressure) * 100) + "%)");
			
			if(mEnt instanceof IHydraulicGenerator){
				float gen = ((IHydraulicGenerator) mEnt).getGenerating();
				int maxGen = ((IHydraulicGenerator) mEnt).getMaxGenerating();
				currenttip.add("Gen: " + gen + "/" + maxGen);
			}			
		}
		return currenttip;
	}
	
	public static void callbackRegister(IWailaRegistrar registrar){
		registrar.registerHeadProvider(new WailaProvider(), IHydraulicMachine.class);
		registrar.registerBodyProvider(new WailaProvider(), IHydraulicMachine.class);
		registrar.registerTailProvider(new WailaProvider(), IHydraulicMachine.class);
		
		//registrar.registerBodyProvider(new WailaProvider(), Ids.blockHydraulicPump.act);
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

}
