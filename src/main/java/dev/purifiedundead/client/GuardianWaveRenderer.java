package dev.purifiedundead.client;
import dev.purifiedundead.entity.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
public final class GuardianWaveRenderer extends EntityRenderer<GuardianWaveEntity> {
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("purified_undead","textures/entity/ferin_slash_white.png");
 private static final java.util.List<FerinSlashMesh.Quad> MESH=FerinSlashMesh.sample(1,3);
 public GuardianWaveRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
 @Override public void render(GuardianWaveEntity e,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light){
  poses.pushPose();poses.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-e.getYRot()));poses.mulPose(com.mojang.math.Axis.XP.rotationDegrees(e.getXRot()));poses.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(e.roll()));
  var out=buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));var m=poses.last().pose();
  for(var q:MESH){v(out,m,q.a());v(out,m,q.b());v(out,m,q.c());v(out,m,q.d());}poses.popPose();
 }
 private static void v(VertexConsumer out,Matrix4f m,FerinSlashMesh.Vertex v){out.vertex(m,(float)(v.right()*.22),(float)((v.up()-1)*.22),(float)((v.forward()-4)*.16)).color(v.red(),v.green(),v.blue(),v.alpha()).uv(.5F,.5F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,1,0).endVertex();}
 @Override public boolean shouldRender(GuardianWaveEntity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBox().inflate(2));}
 @Override public ResourceLocation getTextureLocation(GuardianWaveEntity e){return TEXTURE;}
}
