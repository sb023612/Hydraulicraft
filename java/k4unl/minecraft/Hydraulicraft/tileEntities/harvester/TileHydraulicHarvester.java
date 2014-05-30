package k4unl.minecraft.Hydraulicraft.tileEntities.harvester;

import java.util.ArrayList;
import java.util.List;

import k4unl.minecraft.Hydraulicraft.api.IHarvesterTrolley;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicConsumer;
import k4unl.minecraft.Hydraulicraft.api.PressureTier;
import k4unl.minecraft.Hydraulicraft.blocks.HCBlocks;
import k4unl.minecraft.Hydraulicraft.lib.Localization;
import k4unl.minecraft.Hydraulicraft.lib.Log;
import k4unl.minecraft.Hydraulicraft.lib.config.Config;
import k4unl.minecraft.Hydraulicraft.lib.config.Constants;
import k4unl.minecraft.Hydraulicraft.lib.config.Names;
import k4unl.minecraft.Hydraulicraft.lib.helperClasses.Location;
import k4unl.minecraft.Hydraulicraft.lib.helperClasses.Seed;
import k4unl.minecraft.Hydraulicraft.tileEntities.TileHydraulicBase;
import k4unl.minecraft.Hydraulicraft.tileEntities.consumers.TileHydraulicPiston;
import k4unl.minecraft.Hydraulicraft.tileEntities.interfaces.IHarvester;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class TileHydraulicHarvester extends TileHydraulicBase implements IHydraulicConsumer, ISidedInventory, IHarvester {
	private ItemStack[] seedsStorage;
	private ItemStack[] outputStorage;
	
	private boolean isMultiblock;
	private int harvesterLength;
	private int harvesterWidth;
	private ForgeDirection facing = ForgeDirection.UNKNOWN;
	private boolean firstRun = true;
	
	private boolean isPlanting = false;
	private boolean isHarvesting = false;
	private boolean retracting = false;
	
	private int pistonMoving = -1;
	private int harvestLocationH;
	private int harvestLocationW;
	private int plantLocationH;
	private int plantLocationW;
	private ItemStack plantingItem;
	
	private List<Location> pistonList = new ArrayList<Location>();
	private List<Location> trolleyList = new ArrayList<Location>();
	
	
	private static final Block horizontalFrame = HCBlocks.hydraulicHarvesterSource;
	private static final Block verticalFrame = Blocks.fence;
	private static final Block piston = HCBlocks.hydraulicPiston;
	private static final Block endBlock = HCBlocks.hydraulicPressureWall;
	
	public TileHydraulicHarvester(){
		super(PressureTier.HIGHPRESSURE, 16);
		super.init(this);
		seedsStorage = new ItemStack[9];
		outputStorage = new ItemStack[9];
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		isMultiblock = tagCompound.getBoolean("isMultiblock");
		harvesterLength = tagCompound.getInteger("harvesterLength");
		harvesterWidth = tagCompound.getInteger("harvesterWidth");
		facing = ForgeDirection.getOrientation(tagCompound.getInteger("facing"));
		readPistonListFromNBT(tagCompound);
		for(int i = 0; i<9; i++){
			NBTTagCompound tc = tagCompound.getCompoundTag("seedsStorage"+i);
			seedsStorage[i] = ItemStack.loadItemStackFromNBT(tc);
			
			tc = tagCompound.getCompoundTag("outputStorage"+i);
			outputStorage[i] = ItemStack.loadItemStackFromNBT(tc);
		}
		
		//harvestLocationH = tagCompound.getInteger("harvestLocationH");
		//harvestLocationW = tagCompound.getInteger("harvestLocationW");
		isHarvesting = tagCompound.getBoolean("isHarvesting");
		
		//plantLocationH = tagCompound.getInteger("plantLocationH");
		//plantLocationW = tagCompound.getInteger("plantLocationW");
		isPlanting = tagCompound.getBoolean("isPlanting");
		
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isMultiblock", isMultiblock);
		tagCompound.setInteger("harvesterLength", harvesterLength);
		tagCompound.setInteger("harvesterWidth", harvesterWidth);
		tagCompound.setInteger("facing", facing.ordinal());
		writePistonListToNBT(tagCompound);

		for(int i = 0; i<9; i++){
			if(seedsStorage[i] != null){
				NBTTagCompound tc = new NBTTagCompound();
				seedsStorage[i].writeToNBT(tc);
				tagCompound.setTag("seedsStorage"+i, tc);
			}
			if(outputStorage[i] != null){
				NBTTagCompound tc = new NBTTagCompound();
				outputStorage[i].writeToNBT(tc);
				tagCompound.setTag("outputStorage"+i, tc);
			}
		}
		
		tagCompound.setInteger("harvestLocationH", harvestLocationH);
		tagCompound.setInteger("harvestLocationW", harvestLocationW);
		tagCompound.setBoolean("isHarvesting", isHarvesting);
		
		tagCompound.setInteger("plantLocationH", plantLocationH);
		tagCompound.setInteger("plantLocationW", plantLocationW);
		tagCompound.setBoolean("isPlanting", isPlanting);
		
	}
	
	public void writePistonListToNBT(NBTTagCompound tagCompound){
		NBTTagCompound tagList = new NBTTagCompound();
		
		tagList.setInteger("length", pistonList.size());
		int index = 0;
		for(Location l : pistonList){
			tagList.setIntArray(index + "", l.getLocation());
			index++;
		}
		tagCompound.setTag("pistonList", tagList);
	}
	
	public void writeTrolleyListToNBT(NBTTagCompound tagCompound){
		NBTTagCompound tagList = new NBTTagCompound();
		
		tagList.setInteger("length", trolleyList.size());
		int index = 0;
		for(Location l : trolleyList){
			tagList.setIntArray(index + "", l.getLocation());
			index++;
		}
		tagCompound.setTag("trolleyList", tagList);
	}
	
	public void readTrolleyListFromNBT(NBTTagCompound tagCompound){
		NBTTagCompound tagList = tagCompound.getCompoundTag("trolleyList");
		pistonList.clear();
		if(tagList != null){
			int length = tagList.getInteger("length");
			for(int i = 0; i < length; i++){
				Location l = new Location(tagList.getIntArray(i+""));
				trolleyList.add(l);
			}
		}
	}
	
	public void readPistonListFromNBT(NBTTagCompound tagCompound){
		NBTTagCompound tagList = tagCompound.getCompoundTag("pistonList");
		pistonList.clear();
		if(tagList != null){
			int length = tagList.getInteger("length");
			for(int i = 0; i < length; i++){
				Location l = new Location(tagList.getIntArray(i+""));
				pistonList.add(l);
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		//Every half second.. Or it should be..
		if(!worldObj.isRemote){
			if(worldObj.getTotalWorldTime() % 60 == 0){
				if(firstRun){
					firstRun = false;
					if(isMultiblock){
						convertMultiblock();
						pNetwork = null;
						getHandler().updateNetworkOnNextTick(0);
						getHandler().updateBlock();
					}
				}
				if(!isMultiblock){
					for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
						if(dir.equals(ForgeDirection.UP) || dir.equals(ForgeDirection.DOWN)) continue;
						if(checkMultiblock(dir)){
							this.facing = dir;
							isMultiblock = true;
							//Functions.showMessageInChat("Width of harvester("+dir+"): " + harvesterWidth);
							//Functions.showMessageInChat("Length of harvester("+dir+"): " + harvesterLength);
							convertMultiblock();
							pNetwork = null;
							getHandler().updateNetworkOnNextTick(0);
							break;
						}
					}
				}else{
					if(!checkMultiblock(facing)){
						//Multiblock no longer valid!
						isMultiblock = false;
						invalidateMultiblock();	
					}
				}
				
				if(isMultiblock){
					//TileHydraulicPiston p = getPistonFromList(0);
					//float pExtended = p.getExtendedLength();
					//p.extendTo(pExtended + 1);
				}
			}
		}
	}
	
	private TileHydraulicPiston getPistonFromList(int index){
		Location v = pistonList.get(index);
		return (TileHydraulicPiston) worldObj.getTileEntity(v.getX(), v.getY(), v.getZ());
	}
	
	private TileHydraulicPiston getPistonFromCoords(Location v){
		return (TileHydraulicPiston) worldObj.getTileEntity(v.getX(), v.getY(), v.getZ());
	}

	private TileHarvesterTrolley getTrolleyFromList(int index){
		if(index < trolleyList.size()){
			Location v = trolleyList.get(index);
			return (TileHarvesterTrolley) worldObj.getTileEntity(v.getX(), v.getY(), v.getZ());
		}else{
			return null;
		}
	}
	
	private TileHarvesterTrolley getTrolleyFromCoords(Location v){
		return (TileHarvesterTrolley) worldObj.getTileEntity(v.getX(), v.getY(), v.getZ());
	}
	
	@Override
	public float workFunction(boolean simulate, ForgeDirection from) {
		if(canRun()){
			if(pistonMoving != -1){
				if(pistonMoving <= trolleyList.size()){
					TileHarvesterTrolley t = getTrolleyFromList(pistonMoving);
					TileHydraulicPiston p = getPistonFromList(pistonMoving);
					if(p == null || t == null) {
						invalidateMultiblock();
						return 10F;
					}
					if(simulate == false){
						if(t.isWorking()){
							updateTrolleys();
						}else{
							isHarvesting = false;
							isPlanting = false;
							pistonMoving = -1;
						}
					}
					return 0.1F + p.workFunction(simulate, ForgeDirection.UP) * 2;
				}else{
					Log.error("PistonMoving (" + pistonMoving + ") > " + (trolleyList.size()-1));
				}
			}else if(worldObj.getTotalWorldTime() % 30 == 0){
				checkHarvest();
				if(!isHarvesting){
					checkPlantable();
				}
				return 0.1F;
			}
		}else{
			return 0F;
		}
		return 0F;
	}
	
	private void updateTrolleys(){
		for(Location l : trolleyList){
			TileHarvesterTrolley t = getTrolleyFromCoords(l);
			t.doMove();
		}
	}
	
	
	private boolean isPlaceForItems(ItemStack itemStack){
		//First of all:
		
		if(itemStack.getItem() instanceof ItemSeeds || itemStack.getItem() instanceof ItemReed){
			//Check all the locations!
			for(ItemStack st : seedsStorage){
				if(itemStack.stackSize > 0){
					if(st != null){
						if(st.isItemEqual(itemStack)){
							if(st.stackSize < 64){
								if(st.stackSize + itemStack.stackSize <= 64){
									return true;
								}else{
									itemStack.stackSize = 64 - st.stackSize;
								}
							}
						}
					}else{
						return true;
					}
				}
			}
		}
		for(ItemStack st : outputStorage){
			if(itemStack.stackSize > 0){
				if(st != null){
					if(st.isItemEqual(itemStack)){
						if(st.stackSize < 64){
							if(st.stackSize + itemStack.stackSize <= 64){
								return true;
							}else{
								itemStack.stackSize = 64 - st.stackSize;
							}
						}
					}
				}else{
					return true;
				}
			}
		}
		
		
		if(itemStack.stackSize > 0){
			return false;
		}else{
			return true;
		}
	}
	
	private Location getLocationInHarvester(int h, int w){
		return _getLocationInHarvester(h, w, facing);
	}
	
	private Location getLocationInHarvester(int h, int w, int y){
		Location l = _getLocationInHarvester(h, w, facing);
		l.setY(l.getY() + y);
		
		return l;
	}
	
	private Location getLocationInHarvester(int h, int w, int y, ForgeDirection dir){
		Location l = _getLocationInHarvester(h, w, dir);
		l.setY(l.getY() + y);
		
		return l;
	}
	
	private Location _getLocationInHarvester(int h, int w, ForgeDirection dir){
		
		int nsToAdd = dir.offsetZ * h;
		int ewToAdd = dir.offsetX * h;
		int nsSide = dir.offsetX * w;
		int ewSide = dir.offsetZ * w;
		int x = xCoord + ewToAdd - ewSide;
		int y = yCoord;
		int z = zCoord + nsToAdd + nsSide;
		
		return new Location(x, y, z);
	}
	
	public void putInInventory(ArrayList<ItemStack> toPut){
		for (ItemStack itemStack : toPut) {
			for(int i = 0; i < getSizeInventory(); i++){
				if(itemStack.stackSize > 0){
					if(canInsertItem(i, itemStack, -1)){
						ItemStack sis = getStackInSlot(i);
						if(sis == null){
							sis = itemStack.copy();
							itemStack.stackSize = 0;
							setInventorySlotContents(i, sis);
							break;
						}
						if((sis.stackSize + itemStack.stackSize) <= 64){
							sis.stackSize+= itemStack.stackSize;
							itemStack.stackSize = 0;
						}else{
							itemStack.stackSize = itemStack.stackSize % 64;
							sis.stackSize = 64;
						}
						setInventorySlotContents(i, sis);
					}
				}
			}
		}
	}
	
	private boolean canRun(){
		if(!isMultiblock) return false;
		if(Float.compare(getPressure(ForgeDirection.UNKNOWN), Constants.MIN_REQUIRED_PRESSURE_HARVESTER) < 0) return false;
		if(pistonList.size() == 0) return false;
		
		
		return true;
	}
	
	private boolean isOutputInventoryFull(){
		//Check all the output slots:
		boolean allFull = true;
		for(int i = 0; i< 9; i++){
			if(outputStorage[i] == null){
				allFull = false;
			}else{
				if(outputStorage[i].stackSize < 64){
					allFull = false;
				}
			}
		}
		return allFull;
	}

	@Override
	public void onBlockBreaks() {
		if(isMultiblock){
			invalidateMultiblock();
		}
		//Drop seeds.. duh
		for(int i = 0; i < seedsStorage.length; i++){
			dropItemStackInWorld(seedsStorage[i]);
			dropItemStackInWorld(outputStorage[i]);
		}
	}
	
	public void convertMultiblock(){
		//Build piston list.
		Location l = new Location(xCoord, yCoord + 3, zCoord);
		Location l2 = new Location(xCoord + getFacing().offsetX, yCoord + 2, zCoord + getFacing().offsetZ);
		int horiz = 0;
		
		//Check width:
		pistonList.clear();
		trolleyList.clear();
		while(getBlock(l).equals(piston)){
			l = getLocationInHarvester(0, horiz, 3);
			l2 = getLocationInHarvester(1, horiz, 2);
			
			TileEntity tilePiston = getBlockTileEntity(l);
			if(tilePiston instanceof TileHydraulicPiston){
				TileHydraulicPiston p = (TileHydraulicPiston)tilePiston;
				p.setIsHarvesterPart(true);
				p.setMaxLength((float)harvesterLength-1);
				p.setFacing(getFacing());
				//Location l = new Location(p.xCoord, p.yCoord, p.zCoord);
				pistonList.add(l);
				
				
				TileEntity tile = getBlockTileEntity(l2);
				if(tile instanceof TileHarvesterTrolley){
					TileHarvesterTrolley t = (TileHarvesterTrolley)tile;
					t.setFacing(facing);
					t.setIsHarvesterPart(true);
					t.setHarvester(this, horiz);
					trolleyList.add(l2);
				}
			}
			horiz+=1;
			if(horiz > 10){
				break;
			}
		}
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	
	public void invalidateMultiblock(){
		//Functions.showMessageInChat("Harvester invalidated!");
		facing = ForgeDirection.UNKNOWN;
		isMultiblock = false;
		for(Location l : pistonList){
			TileHydraulicPiston p = getPistonFromCoords(l);
			if(p!=null){
				p.setIsHarvesterPart(false);
				p.extendTo(0F);
			}
		}
		for(Location l : trolleyList){
			TileHarvesterTrolley t = getTrolleyFromCoords(l);
			if(t!=null){
				t.setIsHarvesterPart(false);
			}
		}
		pistonList.clear();
		trolleyList.clear();
		getHandler().updateBlock();
	}
	
	private Block getBlock(Location l){
		return worldObj.getBlock(l.getX(), l.getY(), l.getZ());
	}
	
	private TileEntity getTileEntity(Location l){
		return worldObj.getTileEntity(l.getX(), l.getY(), l.getZ());
	}
	
	private Block getBlock(int x, int y, int z){
		return worldObj.getBlock(x, y, z);
	}
	
	private int getBlockMetaFromCoord(Location l){
		return worldObj.getBlockMetadata(l.getX(), l.getY(), l.getZ());
	}
	private int getBlockMetaFromCoord(int x, int y, int z){
		return worldObj.getBlockMetadata(x, y, z);
	}
	private TileEntity getBlockTileEntity(Location l){
		return worldObj.getTileEntity(l.getX(), l.getY(), l.getZ());
	}

	
	
	private boolean checkMultiblock(ForgeDirection dir){
		//Log.info("------------ Now checking "+ dir + "-------------");
		//Go up, check for pistons etc
		if(!getBlock(xCoord, yCoord + 1, zCoord).equals(verticalFrame)) return false;
		if(!getBlock(xCoord, yCoord + 2, zCoord).equals(verticalFrame)) return false;
		if(!getBlock(xCoord, yCoord + 3, zCoord).equals(piston)) return false;
		Location l = new Location(xCoord, yCoord+3, zCoord);
		
		int horiz = 0;
		
		//Check width:
		int width = 0;
		while(getBlock(l).equals(piston) && width < 11){
			width+=1;
			horiz+=1;
			l = getLocationInHarvester(0, horiz, 3, dir);
		}
		
		if(width > 9 || width == 1){
			return false;
		}
		//Log.info(dir + " Width= " + width);
		
		
		int f = 0;
		horiz = 1;
		int length = 0;
		int firstLength = 0;
		for(f = 0; f < width; f++){
			if(f == 1){
				firstLength = length;
			}
			horiz = 1;
			length = 0;
			l = getLocationInHarvester(horiz, f, 3, dir);
			//Log.info("(" + dir + ": " + l.printCoords() + "; " + f + ") = " + getBlockId(l) + " W: " + width + " F");
			while(getBlock(l).equals(horizontalFrame) && getBlockMetaFromCoord(l) == 1){
				if(horiz == 1){
					//Check if there's a trolley right there!
					Location trolleyLocation = getLocationInHarvester(horiz, f, 2, dir);
					//Log.info("(" + dir + ": " + trolleyLocation.printCoords() + "; " + f + ") = " + getBlockId(trolleyLocation) + " W: " + width + " T");
					
					if(!(getTileEntity(trolleyLocation) instanceof IHarvesterTrolley)){
						return false;
					}
				}
				
				//Check if the frame is rotated:
				TileEntity tile = getBlockTileEntity(l);
				if(tile instanceof TileHarvesterFrame){
					TileHarvesterFrame fr = (TileHarvesterFrame) tile;
					//Log.info("(" + dir + ": " + x + ", " + y + ", " + z + "; " + f + ") = " + fr.getIsRotated());
					if(dir.equals(ForgeDirection.EAST) || dir.equals(ForgeDirection.WEST)){
						if(fr.getIsRotated()){
							return false;
						}
					}else{
						if(!fr.getIsRotated()){
							return false;
						}
					}
				}else{
					return false;
				}
				//Log.info("(" + dir + ": " + l.printCoords() + "; " + f + ") = " + getBlockId(l) + " W: " + width);
				length += 1;
				horiz += 1;
				
				l = getLocationInHarvester(horiz, f, 3, dir);
			}
			//So.. This should actually be the endBlock!
			//Log.info("(" + dir + ": " + l.printCoords() + "; " + f + ") = " + getBlockId(l) + " W: " + width);
			
			if(!getBlock(l).equals(endBlock)){
				return false;
			}
			
			if(f > 0 && firstLength != length){
				return false;
			}
		}
		length = firstLength;
		
		if(length == 0 || length < 4 || length > 9){
			return false;
		}
		
		
		//Check legs!
		horiz = 0;
		for(int vert = 0; vert <= 2; vert++){
			//Farmost left first.
			l = getLocationInHarvester(length+1, 0, vert, dir);
			//Log.info("(" + dir + ": " + l.printCoords() + ") = " + getBlockId(l) + " W: " + width);
			if(l.compare(xCoord, yCoord, zCoord)){
				
			}else{
				if(!getBlock(l).equals(verticalFrame)){
					return false;
				}
				
			}
			
			//Farmost right!
			l = getLocationInHarvester(length+1, width-1, vert, dir);
			//Log.info("(" + dir + ": " + l.printCoords() + ") = " + getBlockId(l) + " W: " + width);
			if(l.compare(xCoord, yCoord, zCoord)){
				
			}else{
				if(!getBlock(l).equals(verticalFrame)){
					return false;
				}
			}
			
			//Base right
			l = getLocationInHarvester(0, width-1, vert, dir);
			//Log.info("(" + dir + ": " + l.printCoords() + ") = " + getBlockId(l) + " W: " + width);
			if(l.compare(xCoord, yCoord, zCoord)){
				
			}else{
				if(!getBlock(l).equals(verticalFrame)){
					return false;
				}
			}
		}
		
		harvesterLength = length;
		harvesterWidth = width;
		//Log.info("Found at dir " + dir);
		return true;
	}

	@Override
	public int getSizeInventory() {
		//9 input, 9 output.
		return 18;
	}
	@Override
    public ItemStack getStackInSlot(int i){
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        if(i < 9){
        	return seedsStorage[i];
        }else if(i >= 9){
        	return outputStorage[i-9];
        }else{
        	return null;
        }
    }

    @Override
    public ItemStack decrStackSize(int i, int j){
        ItemStack inventory = getStackInSlot(i);
        
        if(inventory == null){
        	return null;
        }
        
        ItemStack ret = null;
        if(inventory.stackSize < j) {
            ret = inventory;
            inventory = null;
        } else {
            ret = inventory.splitStack(j);
            if(inventory.stackSize <= 0) {
            	if(i < 9){
            		seedsStorage[i] = null;
            	}else{
            		outputStorage[i-9] = null;
            	}
            }
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        return ret;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i){
        ItemStack stack = getStackInSlot(i);
        if(stack != null) {
            setInventorySlotContents(i, null);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack){
    	if(i < 9){
    		seedsStorage[i] = itemStack;
    		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    	}else{
    		outputStorage[i-9] = itemStack;
    		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    	}
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord, yCoord, zCoord) < 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack){
        if(i < 9) {
        	if(seedsStorage[i] == null){
	        	for(Seed s: Config.harvestableItems){
	        		if(s.getSeedId().equals(itemStack.getItem())){
	        			return true;
	        		}
	        	}
        	}else{
        		return (seedsStorage[i].getItem().equals(itemStack.getItem()) && seedsStorage[i].getItemDamage() == itemStack.getItemDamage());
        	}
            return itemStack.getItem() instanceof IPlantable;
        } else {
            return false;
        }
    }

    public void onInventoryChanged(){
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		if(i < 9){
			return isItemValidForSlot(i, itemStack);
		}else if(j == -1){
			if(getStackInSlot(i) != null){
				return getStackInSlot(i).isItemEqual(itemStack);
			}else{
				return true;
			}
		}else{
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		if(i >= 9){
			return true;
		}
		return false;
	}

	@Override
	public void onFluidLevelChanged(int old) {	}
	
	@Override
	public boolean canConnectTo(ForgeDirection side) {
		return isMultiblock;
	}


	@Override
	public boolean canWork(ForgeDirection dir) {
		return dir.equals(ForgeDirection.UP);
	}
	
	public boolean getIsMultiblock() {
		return isMultiblock;
	}
	
	public ForgeDirection getFacing(){
		return facing;
	}
	
	private void checkPlantable(){
		for(int w = 0; w < harvesterWidth; w++){
			TileHarvesterTrolley t = getTrolleyFromList(w);
			if(t == null){
				return;
			}
			int theSeedToPlant = t.canPlantSeed(seedsStorage, harvesterLength);
			if(theSeedToPlant != -1){
				ItemStack toPlant = seedsStorage[theSeedToPlant].copy();
				toPlant.stackSize = 1;
				seedsStorage[theSeedToPlant].stackSize--;
				if(seedsStorage[theSeedToPlant].stackSize <= 0){
					seedsStorage[theSeedToPlant] = null;
				}
				t.doPlant(toPlant);
				pistonMoving = w;
				isPlanting = true;
				break;
			}
		}
	}
	
	private void checkHarvest(){
		//For now, only crops!
		for(int w = 0; w < harvesterWidth; w++){
			TileHarvesterTrolley t = getTrolleyFromList(w);
			if(t == null){
				return;
			}
			ArrayList<ItemStack> dropped = t.checkHarvest(harvesterLength);
			if(dropped == null) continue;
			boolean placeForAll = true;
			for(ItemStack st: dropped){
				if(!isPlaceForItems(st)){
					placeForAll = false;
					break;
				}
			}
			if(placeForAll){
				t.doHarvest();
				isHarvesting = true;
				pistonMoving = w;
				break;
			}
		}
		return;
	}

	@Override
	public String getInventoryName() {
		return Localization.getLocalizedName(Names.blockHydraulicHarvester[0].unlocalized);
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void extendPistonTo(int piston, float length) {
		TileHydraulicPiston p = getPistonFromList(piston);
		p.extendTo(length);
	}
	
}
