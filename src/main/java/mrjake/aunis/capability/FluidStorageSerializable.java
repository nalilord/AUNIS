package mrjake.aunis.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidStorageSerializable extends FluidTank implements INBTSerializable<NBTTagCompound> {

	public FluidStorageSerializable(Fluid fluid, int amount, int capacity) {
		super(new FluidStack(fluid, amount), capacity);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tagCompound = new NBTTagCompound();

		writeToNBT(tagCompound);

		return tagCompound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (nbt != null) {
			readFromNBT(nbt);
		}
	}

}
