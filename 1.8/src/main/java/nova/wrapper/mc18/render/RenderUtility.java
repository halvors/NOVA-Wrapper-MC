package nova.wrapper.mc18.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nova.core.component.renderer.ItemRenderer;
import nova.core.component.renderer.StaticRenderer;
import nova.core.render.texture.BlockTexture;
import nova.core.render.texture.ItemTexture;
import nova.core.render.texture.Texture;
import nova.internal.core.Game;
import nova.wrapper.mc18.wrapper.block.forward.FWBlock;
import nova.wrapper.mc18.wrapper.item.FWItem;
import nova.wrapper.mc18.wrapper.render.FWEmptyModel;
import nova.wrapper.mc18.wrapper.render.FWSmartBlockModel;
import nova.wrapper.mc18.wrapper.render.FWSmartItemModel;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POLYGON_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glShadeModel;

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
public class RenderUtility {

	public static final ResourceLocation particleResource = new ResourceLocation("textures/particle/particles.png");

	public static final RenderUtility instance = new RenderUtility();

	//NOVA Texture to MC TextureAtlasSprite
	private final HashMap<Texture, TextureAtlasSprite> textureMap = new HashMap<>();

	// Cruft needed to generate default item models
	protected static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
	protected static final FaceBakery FACE_BAKERY = new FaceBakery();
	// Ugly D:
	protected static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize(
			"{\"" +
			"elements\":[{" +
			"  \"from\": [0, 0, 0], " +
			"  \"to\": [16, 16, 16], " +
			"  \"faces\": {" +
			"      \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}" +
			"  }}]," +
			"  \"display\": {\n" +
			"      \"thirdperson\": {\n" +
			"          \"rotation\": [ -90, 0, 0 ],\n" +
			"          \"translation\": [ 0, 1, -3 ],\n" +
			"          \"scale\": [ 0.55, 0.55, 0.55 ]\n" +
			"      },\n" +
			"      \"firstperson\": {\n" +
			"          \"rotation\": [ 0, -135, 25 ],\n" +
			"          \"translation\": [ 0, 4, 2 ],\n" +
			"          \"scale\": [ 1.7, 1.7, 1.7 ]\n" +
			"      }\n" +
			"}}");

	/**
	 * Enables blending.
	 */
	public static void enableBlending() {
		glShadeModel(GL_SMOOTH);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	/**
	 * Disables blending.
	 */
	public static void disableBlending() {
		glShadeModel(GL_FLAT);
		glDisable(GL_LINE_SMOOTH);
		glDisable(GL_POLYGON_SMOOTH);
		glDisable(GL_BLEND);
	}

	public static void enableLighting() {
		RenderHelper.enableStandardItemLighting();
	}

	/**
	 * Disables lighting and turns glow on.
	 */
	public static void disableLighting() {
		RenderHelper.disableStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
	}

	public static void disableLightmap() {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public static void enableLightmap() {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public TextureAtlasSprite getTexture(Texture texture) {
		if (textureMap.containsKey(texture)) {
			return textureMap.get(texture);
		}

		//Fallback to MC texture
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.domain + ":" + texture.getPath().replaceFirst("textures/", "").replace(".png", ""));
	}

	/**
	 * Handles NOVA texture registration.
	 *
	 * @param event Event
	 */
	@SubscribeEvent
	public void preTextureHook(TextureStitchEvent.Pre event) {
		if (event.map == Minecraft.getMinecraft().getTextureMapBlocks()) {
			Game.render().blockTextures.forEach(t -> registerIcon(t, event));
			Game.render().itemTextures.forEach(t -> registerIcon(t, event));
			//TODO: This is HACKS. We should create custom sprite sheets for entities.
			Game.render().entityTextures.forEach(t -> registerIcon(t, event));
		}
	}

	public void registerIcon(Texture texture, TextureStitchEvent.Pre event) {
		String resPath = (texture instanceof BlockTexture ? "blocks" : texture instanceof ItemTexture ? "items" : "entities") + "/" + texture.resource;
		textureMap.put(texture, event.map.registerSprite(new ResourceLocation(texture.domain, resPath)));
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) {
		//Register all blocks
		Game.blocks().registry.forEach(blockFactory -> {
			Object blockObj = Game.natives().toNative(blockFactory.getDummy());
			if (blockObj instanceof FWBlock) {
				FWBlock block = (FWBlock) blockObj;
				ResourceLocation blockRL = (ResourceLocation) net.minecraft.block.Block.blockRegistry.getNameForObject(block);
				Item itemFromBlock = Item.getItemFromBlock(block);
				ResourceLocation itemRL = (ResourceLocation) Item.itemRegistry.getNameForObject(itemFromBlock);
				ModelResourceLocation blockLocation = new ModelResourceLocation(blockRL, "normal");
				ModelResourceLocation itemLocation = new ModelResourceLocation(itemRL, "inventory");
				if (block.block.has(StaticRenderer.class)) {
					event.modelRegistry.putObject(blockLocation, new FWSmartBlockModel(block.block, true));
				} else {
					event.modelRegistry.putObject(blockLocation, new FWEmptyModel());
				}
				event.modelRegistry.putObject(itemLocation, new FWSmartBlockModel(block.block, true));
			}
		});

		//Register all items
		Game.items().registry.forEach(itemFactory -> {
			Object stackObj = Game.natives().toNative(itemFactory.getDummy());
			if (stackObj instanceof ItemStack) {
				Item itemObj = ((ItemStack) stackObj).getItem();
				if (itemObj instanceof FWItem) {
					FWItem item = (FWItem) itemObj;
					ResourceLocation objRL = (ResourceLocation) Item.itemRegistry.getNameForObject(item);
					ModelResourceLocation itemLocation = new ModelResourceLocation(objRL, "inventory");

					nova.core.item.Item dummy = item.getItemFactory().getDummy();

					if (dummy.has(ItemRenderer.class)) {
						Optional<Texture> texture = dummy.get(ItemRenderer.class).texture;

						if (texture.isPresent()) {
							MODEL_GENERATED.textures.put("layer0", texture.get().getResource());
							MODEL_GENERATED.name = itemLocation.toString();

							// This is the key part, it takes the texture and makes the "3d" one wide voxel model
							ModelBlock itemModel = ITEM_MODEL_GENERATOR.makeItemModel(new FakeTextureMap(dummy), MODEL_GENERATED);

							// This was taken from ModelBakery and simplified for the generation of our Items
							SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(itemModel).setTexture(getTexture(texture.get()));
							for (BlockPart blockpart : (Iterable<BlockPart>) itemModel.getElements()) {
								for (EnumFacing enumfacing : (Iterable<EnumFacing>) blockpart.mapFaces.keySet()) {
									BlockPartFace blockpartface = (BlockPartFace) blockpart.mapFaces.get(enumfacing);
									BakedQuad bakedQuad = FACE_BAKERY.makeBakedQuad(blockpart.positionFrom, blockpart.positionTo, blockpartface, getTexture(texture.get()), enumfacing, ModelRotation.X0_Y0, blockpart.partRotation, false, blockpart.shade);

									if (blockpartface.cullFace == null || !TRSRTransformation.isInteger(ModelRotation.X0_Y0.getMatrix())) {
										builder.addGeneralQuad(bakedQuad);
									} else {

										builder.addFaceQuad(ModelRotation.X0_Y0.rotate(blockpartface.cullFace), bakedQuad);
									}
								}
							}
							event.modelRegistry.putObject(itemLocation, builder.makeBakedModel());
						} else {
							event.modelRegistry.putObject(itemLocation, new FWSmartItemModel(dummy));
						}
					}
				}
			}
		});
	}

	public void preInit() {
		//Load models
		Game.render().modelProviders.forEach(m -> {
			ResourceLocation resource = new ResourceLocation(m.domain, "models/" + m.name + "." + m.getType());
			try {
				IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
				m.load(res.getInputStream());
			} catch (IOException e) {
				throw new RuntimeException("IO Exception reading model format", e);
			}
		});
	}

	private class FakeTextureMap extends TextureMap {
		private final nova.core.item.Item item;
		public FakeTextureMap(nova.core.item.Item item) {
			super("");
			this.item = item;
		}

		@Override
		public TextureAtlasSprite getAtlasSprite(String iconName) {
			if (item.has(ItemRenderer.class)) {
				ItemRenderer itemRenderer = item.get(ItemRenderer.class);
				if (itemRenderer.texture.isPresent()) {
					return RenderUtility.instance.getTexture(itemRenderer.texture.get());
				}
			}
			return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}
	}
}
