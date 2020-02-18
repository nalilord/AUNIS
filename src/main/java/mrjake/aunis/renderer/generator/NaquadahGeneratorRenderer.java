package mrjake.aunis.renderer.generator;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.renderer.ItemRenderer;
import mrjake.aunis.state.NaquadahGeneratorRendererState;
import mrjake.aunis.tesr.RendererInterface;
import mrjake.aunis.tileentity.NaquadahGeneratorTile;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class NaquadahGeneratorRenderer implements RendererInterface {

	NaquadahGeneratorRendererState state = new NaquadahGeneratorRendererState();

	private World world;
	private BlockPos pos;
	private EnumFacing facing;

	private ItemRenderer itemRenderer;
	private ItemStack renderedItemStack;

	private ResourceLocation genActiveResourceLocation = new ResourceLocation("aunis:textures/tesr/generator/naquadah/naquadah_generator_on.png");

	private long creationTime;
	private float scale = 1;

	public NaquadahGeneratorRenderer(NaquadahGeneratorTile te) {
		this.world = te.getWorld();
		this.pos = te.getPos();

		creationTime = world.getTotalWorldTime();
		facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);

		renderedItemStack = new ItemStack(AunisItems.crystalControlDhd);
		itemRenderer = new ItemRenderer(renderedItemStack);
	}

	@Override
	public void render(double x, double y, double z, float partialTicks) {
		Model naquadahGenerator = ModelLoader.getModel( EnumModel.NAQUADAHGENERATOR_MODEL );

		if(naquadahGenerator != null) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glScalef(scale, scale, scale);
			GL11.glTranslated(x, y, z);

			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			if(facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH)
				GL11.glRotatef(-180F, 0F, 1F, 0F);
			else
				GL11.glRotatef(90F, 0F, 1F, 0F);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

			if(state.isActive)
				Minecraft.getMinecraft().getTextureManager().bindTexture(genActiveResourceLocation);
			else
				EnumModel.NAQUADAHGENERATOR_MODEL.bindTexture();

			naquadahGenerator.render();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
	}

	public void setRendererState(NaquadahGeneratorRendererState state) {
		this.state = state;
	}
}
