package me.nova.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import me.nova.NovaMod;
import me.nova.entity.BlackCubeEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

/**
 * 通过 1.21.10 的原生 GPU 管线执行回忆过渡，世界深度和颜色均使用独立副本。
 *
 * <p>在世界绘制后调用 {@link #captureDepth}，在 GUI 实际提交前调用 {@link #render}。
 * 全部入口限客户端渲染线程，退出客户端时调用 {@link #close()}。
 */
public final class BlackCubePostProcessor {
    /** 注册到原版着色器资源生命周期的唯一后处理管线。 */
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POST_EFFECT_PROCESSOR_SNIPPET)
                    .withLocation(NovaMod.id("pipeline/black_cube_memory"))
                    .withVertexShader(NovaMod.id("post/screen_ghost"))
                    .withFragmentShader(NovaMod.id("post/screen_ghost"))
                    .withUniform("MemoryData", UniformType.UNIFORM_BUFFER)
                    .withSampler("Scene")
                    .withSampler("SceneDepth")
                    .withCull(false)
                    .withoutBlend()
                    .build());
    /** 保存最终场景颜色的副本，不含 HUD。 */
    private static Framebuffer scene;
    /** 在手持物清除深度之前保存的世界深度副本。 */
    private static Framebuffer worldDepth;
    /** 每帧更新的 std140 参数缓冲。 */
    private static GpuBuffer uniforms;
    /** 逆投影与视图矩阵，将深度还原为相对摄像机的世界位置。 */
    private static Matrix4f inverseWorld = new Matrix4f();
    /** 摄像机相对本次仪式固定原点的偏移。 */
    private static Vec3d cameraOffset = Vec3d.ZERO;
    /** 本体在屏幕中的水平焦点。 */
    private static float focusX = 0.5F;
    /** 本体在屏幕中的垂直焦点。 */
    private static float focusY = 0.5F;
    /** 本帧是否已捕获有效深度。 */
    private static boolean depthReady;

    private BlackCubePostProcessor() {
    }

    /** 取消旧深度的有效标记，例如断开世界或本帧世界捕获开始时调用。 */
    public static void invalidateDepth() {
        depthReady = false;
    }

    /** 根据实际窗口尺寸分配独立附件，重建前先释放旧资源。 */
    private static void ensureTargets(Framebuffer main) {
        if (scene == null || scene.textureWidth != main.textureWidth || scene.textureHeight != main.textureHeight) {
            close();
            scene = new SimpleFramebuffer("novamod memory color", main.textureWidth, main.textureHeight, false);
            scene.setFilter(FilterMode.LINEAR);
            worldDepth = new SimpleFramebuffer("novamod memory depth", main.textureWidth, main.textureHeight, true);
            uniforms = RenderSystem.getDevice().createBuffer(() -> "novamod memory parameters",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 128);
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(worldDepth.getDepthAttachment(), 1.0);
        }
    }

    /**
     * 在世界绘制后保存深度，不依赖 OpenGL framebuffer 编号。
     *
     * <p>例如世界渲染尾部调用 {@code captureDepth(view, projection, origin, cube)}。
     *
     * @param view 摄像机视图旋转矩阵
     * @param projection 本帧含 FOV、受伤及走路晃动的投影矩阵
     * @param origin 激活时的固定世界原点
     * @param cube 拉扯焦点实体
     */
    public static void captureDepth(Matrix4f view, Matrix4f projection, Vec3d origin, BlackCubeEntity cube) {
        RenderSystem.assertOnRenderThread();
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer main = client.getFramebuffer();
        ensureTargets(main);
        if (main.getDepthAttachment() == null) {
            return;
        }
        worldDepth.copyDepthFrom(main);
        Matrix4f world = new Matrix4f(projection).mul(view);
        inverseWorld.set(world).invert();
        cameraOffset = client.gameRenderer.getCamera().getPos().subtract(origin);
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        Vec3d relative = cube.getEntityPos().add(0.0, cube.getEffectScale(tickDelta) * 0.4, 0.0)
                .subtract(client.gameRenderer.getCamera().getPos());
        Vector4f focus = world.transform(new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1F));
        focusX = focus.w > 0.001F ? MathHelper.clamp(focus.x / focus.w * 0.5F + 0.5F, 0.1F, 0.9F) : 0.5F;
        focusY = focus.w > 0.001F ? MathHelper.clamp(focus.y / focus.w * 0.5F + 0.5F, 0.1F, 0.9F) : 0.5F;
        depthReady = true;
    }

    /**
     * 将最终场景复制后执行重影、失色、拉扯和地形由远及近黑化。
     * 无有效深度时使用烟雾边缘屏幕收缩；完全闭合始终输出纯黑。
     *
     * <p>例如 {@code render(1F, 0F, 4F, 0.5F, 5F)}，必须在 HUD 之前调用。
     *
     * @param directionX 重影的屏幕水平方向
     * @param directionY 重影的屏幕垂直方向
     * @param ghostProgress 零至四的重影层进度
     * @param obscuration 零至一的黑化进度
     * @param effectTime 运行时间，单位为秒
     */
    public static void render(float directionX, float directionY, float ghostProgress,
                              float obscuration, float effectTime) {
        RenderSystem.assertOnRenderThread();
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        ensureTargets(main);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(main.getColorAttachment(), scene.getColorAttachment(),
                0, 0, 0, 0, 0, main.textureWidth, main.textureHeight);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = stack.malloc(128);
            inverseWorld.get(0, data);
            data.position(64);
            data.putFloat(main.textureWidth).putFloat(main.textureHeight).putFloat(directionX).putFloat(directionY);
            data.putFloat(ghostProgress).putFloat(obscuration).putFloat(effectTime).putFloat(depthReady ? 1F : 0F);
            data.putFloat((float) cameraOffset.x).putFloat((float) cameraOffset.y).putFloat((float) cameraOffset.z).putFloat(focusX);
            data.putFloat(focusY).putFloat(0F).putFloat(0F).putFloat(0F);
            data.flip();
            encoder.writeToBuffer(uniforms.slice(), data);
        }
        try (RenderPass pass = encoder.createRenderPass(() -> "novamod memory transition",
                main.getColorAttachmentView(), OptionalInt.empty())) {
            pass.setPipeline(PIPELINE);
            pass.setUniform("MemoryData", uniforms);
            pass.bindSampler("Scene", scene.getColorAttachmentView());
            pass.bindSampler("SceneDepth", worldDepth.getDepthAttachmentView());
            pass.draw(0, 3);
        }
    }

    /** 在退出客户端或尺寸改变时释放附件及参数缓存；重复调用安全。 */
    public static void close() {
        if (scene != null) {
            scene.delete();
            scene = null;
        }
        if (worldDepth != null) {
            worldDepth.delete();
            worldDepth = null;
        }
        if (uniforms != null) {
            uniforms.close();
            uniforms = null;
        }
        depthReady = false;
    }
}
