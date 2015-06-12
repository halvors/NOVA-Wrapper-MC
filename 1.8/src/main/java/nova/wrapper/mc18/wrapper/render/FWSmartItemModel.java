package nova.wrapper.mc18.wrapper.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartItemModel;
import nova.core.component.renderer.ItemRenderer;
import nova.core.item.Item;
import nova.core.render.model.Face;
import nova.core.render.model.Vertex;
import nova.core.render.texture.ItemTexture;
import nova.internal.core.Game;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Generates a smart model based on a NOVA Model
 * @author Calclavia
 */
public class FWSmartItemModel extends FWSmartModel implements ISmartItemModel, IFlexibleBakedModel {

	private final Item item;
	protected static final ModelBlock MODEL_DEFAULT;
	private static final ItemModelGenerator itemModelGenerator = new ItemModelGenerator();

	static {
		MODEL_DEFAULT = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
	}

	public FWSmartItemModel(Item item) {
		super();
		this.item = item;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		Item item = Game.natives().toNova(stack);

		if (item.has(ItemRenderer.class)) {
			return new FWSmartItemModel(item);
		}

		return this;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		if (item.has(ItemRenderer.class)) {
			BWModel model = new BWModel();
			ItemRenderer renderer = item.get(ItemRenderer.class);
			renderer.onRender.accept(model);

			return modelToQuads(model);
		}

		//TODO: This should be in NOVA Core
		Optional<ItemTexture> texture = item.getTexture();
		if (texture.isPresent()) {
			BWModel model = new BWModel();
			Face face = model.createFace();
			face.drawVertex(new Vertex(0, 0, 0, 0, 0));
			face.drawVertex(new Vertex(0, 0, 1, 0, 1));
			face.drawVertex(new Vertex(1, 0, 1, 1, 1));
			face.drawVertex(new Vertex(1, 0, 0, 1, 0));
			face.bindTexture(texture.get());
			model.drawFace(face);
			return modelToQuads(model);
		}

		return Collections.emptyList();
	}

	@Override
	public boolean isGui3d() {
		return false;//item.has(ItemRenderer.class);
	}
}