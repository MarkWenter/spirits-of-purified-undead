package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.entity.ContractWispEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Three small gradient rings, using the existing white texture and vanilla emissive pipeline. */
public final class ContractWispRenderer extends EntityRenderer<ContractWispEntity> {
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath("purified_undead", "textures/entity/ferin_slash_white.png");
    private static final int SEGMENTS = 16;
    private static final float[] COS = new float[SEGMENTS+1], SIN = new float[SEGMENTS+1];
    static { for(int i=0;i<=SEGMENTS;i++){ COS[i]=(float)Math.cos(i*Math.PI*2/SEGMENTS); SIN[i]=(float)Math.sin(i*Math.PI*2/SEGMENTS); } }
    public ContractWispRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=0; }
    @Override public void render(ContractWispEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
        if(entity.owner()==null || entity.owner().isInvisible()) return;
        float strength = entity.visualLight()/15F;
        float breath = 1F + .06F*(float)Math.sin((entity.tickCount+partial)*.08);
        poses.pushPose(); poses.mulPose(entityRenderDispatcher.cameraOrientation());
        VertexConsumer out=buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        Matrix4f matrix=poses.last().pose();
        float size=(.75F+.25F*strength)*breath;
        ring(out,matrix,0,.055F*size,239,181,173, (int)(150+105*strength),200);
        ring(out,matrix,.055F*size,.16F*size,178,112,115, (int)(110+65*strength),36);
        ring(out,matrix,.16F*size,.30F*size,126,103,110,36,0);
        poses.popPose();
    }
    private static void ring(VertexConsumer out,Matrix4f m,float inner,float outer,int r,int g,int b,int a,int edge) {
        for(int i=0;i<SEGMENTS;i++) {
            vertex(out,m,COS[i]*inner,SIN[i]*inner,r,g,b,a);
            vertex(out,m,COS[i]*outer,SIN[i]*outer,r,g,b,edge);
            vertex(out,m,COS[i+1]*outer,SIN[i+1]*outer,r,g,b,edge);
            vertex(out,m,COS[i+1]*inner,SIN[i+1]*inner,r,g,b,a);
        }
    }
    private static void vertex(VertexConsumer out,Matrix4f m,float x,float y,int r,int g,int b,int a) {
        out.vertex(m,x,y,0).color(r,g,b,a).uv(.5F,.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,0,1).endVertex();
    }
    @Override public ResourceLocation getTextureLocation(ContractWispEntity entity) { return WHITE; }
}
