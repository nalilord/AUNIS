package mrjake.aunis.item;

import mrjake.aunis.block.NaquadahGeneratorBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NaquadahGeneratorBlockItem extends ItemBlock {

	public NaquadahGeneratorBlockItem(Block block) {
		super(block);
		
		setRegistryName(NaquadahGeneratorBlock.BLOCK_NAME);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean updateItemStackNBT(NBTTagCompound nbt) {
		return super.updateItemStackNBT(nbt);
	}
}
