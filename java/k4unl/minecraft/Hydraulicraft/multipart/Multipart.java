package k4unl.minecraft.Hydraulicraft.multipart;

import java.util.List;

import k4unl.minecraft.Hydraulicraft.api.IHydraulicMachine;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicTransporter;
import k4unl.minecraft.Hydraulicraft.lib.config.Ids;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import k4unl.minecraft.Hydraulicraft.lib.helperClasses.Location;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class Multipart implements IPartFactory{
	public static ItemPartHose itemPartHose; 
	
	public Multipart(){
		MultiPartRegistry.registerParts(this, new String[] { 
                Names.blockHydraulicHose[0].unlocalized,
                Names.blockHydraulicHose[1].unlocalized,
                Names.blockHydraulicHose[2].unlocalized});
		
		itemPartHose = new ItemPartHose(Ids.blockHydraulicHose.act);
	}

	@Override
	public TMultiPart createPart(String id, boolean client) {
		if(id.equals(Names.blockHydraulicHose[0].unlocalized) || id.equals(Names.blockHydraulicHose[1].unlocalized) || id.equals(Names.blockHydraulicHose[2].unlocalized)){
			return new PartHose();
		}
		return null;
	}
	
	public static TileMultipart getMultipartTile(IBlockAccess access, Location pos){
        TileEntity te = access.getBlockTileEntity(pos.getX(), pos.getY(), pos.getZ());
        return te instanceof TileMultipart ? (TileMultipart) te : null;
    }

    public static TMultiPart getMultiPart(IBlockAccess w, Location bc, int part){
        TileMultipart t = getMultipartTile(w, bc);
        if (t != null)
            return t.partMap(part);

        return null;
    }
    
    public static boolean hasTransporter(TileMultipart mp){
    	boolean ret = false;
    	List<TMultiPart> t = mp.jPartList();
		for (TMultiPart p: t) {
			if(ret == false){
				if(p instanceof IHydraulicTransporter){
					ret = true;
				}
			}
		}
		return ret;
    }
    
    public static IHydraulicTransporter getTransporter(TileMultipart mp){
    	boolean ret = false;
    	List<TMultiPart> t = mp.jPartList();
		for (TMultiPart p: t) {
			if(ret == false){
				if(p instanceof IHydraulicTransporter){
					return (IHydraulicTransporter)p;
				}
			}
		}
		return null;
    }
}