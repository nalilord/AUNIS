package mrjake.aunis.block;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.tileentity.NaquadahGeneratorTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class NaquadahGeneratorBlock extends Block {

    private static final String blockName = "naquadah_generator_block";

    public NaquadahGeneratorBlock() {
        super(Material.ROCK);

        setRegistryName(Aunis.ModID + ":" + blockName);
        setTranslationKey(Aunis.ModID + "." + blockName);

        setSoundType(SoundType.STONE);
        setCreativeTab(Aunis.aunisCreativeTab);

        setDefaultState(blockState.getBaseState().withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.NORTH));

        setLightOpacity(0);

        setHardness(3.0f);
        setHarvestLevel("pickaxe", 3);
    }

    // ------------------------------------------------------------------------
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AunisProps.FACING_HORIZONTAL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AunisProps.FACING_HORIZONTAL).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AunisProps.FACING_HORIZONTAL, EnumFacing.byHorizontalIndex(meta & 0x03));
    }

    // ------------------------------------------------------------------------
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!world.isRemote && tile != null && tile instanceof NaquadahGeneratorTile) {
            NaquadahGeneratorTile generatorTile = (NaquadahGeneratorTile) tile;

            ItemStack itemStack = player.getHeldItem(hand);
            if (!itemStack.isEmpty() && itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

                boolean interacted = generatorTile.onFluidContainerInteract(player, hand, fluidHandlerItem, itemStack);

                if (interacted) {
                    if (!world.isRemote) {
                        world.playSound(null, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1f, 1f);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(AunisProps.FACING_HORIZONTAL, placer.getHorizontalFacing().getOpposite()), 2); // 2 - send update to clients
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        NaquadahGeneratorTile tile = world.getTileEntity(pos) instanceof NaquadahGeneratorTile ? (NaquadahGeneratorTile) world.getTileEntity(pos) : null;
        if (tile != null) {
            ItemStack stack = new ItemStack(this);
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setTag("BlockEntityTag", tile.getTileData());
            stack.setTagCompound(tagCompound);
            drops.add(stack);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest) return true;
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool)
    {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    // ------------------------------------------------------------------------
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public NaquadahGeneratorTile createTileEntity(World world, IBlockState state) {
        return new NaquadahGeneratorTile();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.30, 0, 0.30, 0.70, 0.45, 0.70);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return new AxisAlignedBB(0.30, 0, 0.30, 0.70, 0.45, 0.70);
    }

}
