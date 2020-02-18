package mrjake.aunis.tileentity;

import cofh.redstoneflux.api.IEnergyReceiver;
import mrjake.aunis.Aunis;
import mrjake.aunis.capability.EnergyStorageSerializable;
import mrjake.aunis.capability.FluidStorageSerializable;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.renderer.generator.NaquadahGeneratorRenderer;
import mrjake.aunis.state.NaquadahGeneratorRendererState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tesr.RendererInterface;
import mrjake.aunis.tesr.RendererProviderInterface;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.api.tileentity.IEnergyInfo;

public class NaquadahGeneratorTile extends TileEntity implements ITickable, IEnergyProvider, IEnergyInfo, RendererProviderInterface, StateProviderInterface {

	@CapabilityInject(IEnergyStorage.class)
	public static final Capability<IEnergyStorage> ENERGY_HANDLER = null;

	private static final int POWER_MAX_TRANSFER = 1000;
	private static final int FLUID_DRAIN_AMOUNT = 500;
	private static final int POWER_BUFFER_DRAIN = 5;
	private static final int FLUID_TO_POWER_MUL = 10;

	private NaquadahGeneratorRenderer renderer;
	private NaquadahGeneratorRendererState rendererState = new NaquadahGeneratorRendererState();
	private TargetPoint targetPoint;

	private int powerBuffer = 0;

	@Override
	public void update() {
		if (!world.isRemote) {
			if (canProduceEnergy()) {
				powerBuffer = drainFluid() * FLUID_TO_POWER_MUL;
			}

			if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored() && powerBuffer >= POWER_BUFFER_DRAIN) {
				powerBuffer -= POWER_BUFFER_DRAIN;
				energyStorage.receiveEnergy(POWER_MAX_TRANSFER, false);
				rendererState.isActive = true;
			} else {
				rendererState.isActive = false;
			}

			transferEnergy();

			if (targetPoint != null) {
				rendererState.powerBuffer = powerBuffer;
				rendererState.energyStored = energyStorage.getEnergyStored();
				rendererState.fluidStored = fluidStorage.getFluidAmount();

				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_STATE, rendererState), targetPoint);
			}

			markDirty();
		}
	}

	private int drainFluid() {
		FluidStack fluid = fluidStorage.drain(FLUID_DRAIN_AMOUNT, true);

		if(fluid != null)
			return fluid.amount;
		else
			return 0;
	}

	private boolean canProduceEnergy() {
		return powerBuffer <= POWER_BUFFER_DRAIN && fluidStorage.getFluidAmount() > 0;
	}

	protected void transferEnergy() {
		for (int i = 0; i < 6 && energyStorage.getEnergyStored() > 0; i++) {
			TileEntity handler = this.getWorld().getTileEntity(pos.offset(EnumFacing.VALUES[i]));
			if (handler instanceof IEnergyReceiver) {
				energyStorage.modifyEnergyStored(-((IEnergyReceiver) handler).receiveEnergy(EnumFacing.VALUES[i].getOpposite(), Math.min(POWER_MAX_TRANSFER, energyStorage.getEnergyStored()), false));
			} else if (handler != null && handler.hasCapability(ENERGY_HANDLER, EnumFacing.VALUES[i].getOpposite())) {
				energyStorage.modifyEnergyStored(-handler.getCapability(ENERGY_HANDLER, EnumFacing.VALUES[i].getOpposite()).receiveEnergy(Math.min(POWER_MAX_TRANSFER, energyStorage.getEnergyStored()), false));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("rendererState", rendererState.serializeNBT());
		compound.setTag("energy", energyStorage.serializeNBT());
		compound.setTag("fluid", fluidStorage.serializeNBT());
		compound.setInteger("powerBuffer", powerBuffer);

		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		try {
			rendererState.deserializeNBT(compound.getCompoundTag("rendererState"));
		} catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.info("Exception at reading RendererState");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}

		if (compound.hasKey("energy"))
			energyStorage.deserializeNBT(compound.getCompoundTag("energy"));
		if (compound.hasKey("fluid"))
			fluidStorage.deserializeNBT(compound.getCompoundTag("fluid"));
		if (compound.hasKey("powerBuffer"))
			powerBuffer = compound.getInteger("powerBuffer");

		super.readFromNBT(compound);
	}

	@Override
	public void onLoad() {
		if (!world.isRemote) {
			targetPoint = new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		} else {
			renderer = new NaquadahGeneratorRenderer(this);
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_STATE));
		}
	}

	EnergyStorageSerializable energyStorage = new EnergyStorageSerializable(1000000, 50000) {
		protected void onEnergyChanged() {
			markDirty();
		};
	};

	FluidStorageSerializable fluidStorage = new FluidStorageSerializable(AunisFluids.moltenNaquadahRefined, 0, 5000) {
		protected void onContentsChanged() {
			markDirty();
		};
	};

	// ------------------------------------------------------------------------
	// Renderer

	@Override
	public RendererInterface getRenderer() {
		return renderer;
	}

	// ------------------------------------------------------------------------
	// Power buffer

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return rendererState;

			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new NaquadahGeneratorRendererState();

			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				renderer.setRendererState((NaquadahGeneratorRendererState) state);
				break;

			default:
				break;
		}
	}

	// ------------------------------------------------------------------------

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY ||	capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(new net.minecraftforge.energy.IEnergyStorage() {

				@Override
				public int receiveEnergy(int maxReceive, boolean simulate) {
					return 0;
				}

				@Override
				public int extractEnergy(int maxExtract, boolean simulate) {
					return energyStorage.extractEnergy(maxExtract, simulate);
				}

				@Override
				public int getEnergyStored() {
					return energyStorage.getEnergyStored();
				}

				@Override
				public int getMaxEnergyStored() {
					return energyStorage.getMaxEnergyStored();
				}

				@Override
				public boolean canExtract() {
					return true;
				}

				@Override
				public boolean canReceive() {
					return false;
				}
			});
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return (T) fluidStorage;
		else
			return super.getCapability(capability, facing);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		return energyStorage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int getInfoEnergyPerTick() {
		return POWER_MAX_TRANSFER;
	}

	@Override
	public int getInfoMaxEnergyPerTick() {
		return POWER_MAX_TRANSFER;
	}

	@Override
	public int getInfoEnergyStored() {
		return energyStorage.getEnergyStored();
	}
}
