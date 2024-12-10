package me.mrnavastar.creorio.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class ChunkRenderer {

    private static final GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
    private static final WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;

    private static void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, ChunkPos pos) {
        RenderSystem.assertOnRenderThread();
        renderLayer.startDrawing();

        ShaderProgram shaderProgram = RenderSystem.getShader();
        for(int m = 0; m < 12; ++m) {
            int n = RenderSystem.getShaderTexture(m);
            shaderProgram.addSampler("Sampler" + m, n);
        }
        if (shaderProgram.modelViewMat != null) shaderProgram.modelViewMat.set(matrices.peek().getPositionMatrix());
        if (shaderProgram.projectionMat != null) shaderProgram.projectionMat.set(positionMatrix);
        if (shaderProgram.colorModulator != null) shaderProgram.colorModulator.set(RenderSystem.getShaderColor());
        if (shaderProgram.glintAlpha != null) shaderProgram.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
        if (shaderProgram.fogStart != null) shaderProgram.fogStart.set(RenderSystem.getShaderFogStart());
        if (shaderProgram.fogEnd != null) shaderProgram.fogEnd.set(RenderSystem.getShaderFogEnd());
        if (shaderProgram.fogColor != null) shaderProgram.fogColor.set(RenderSystem.getShaderFogColor());
        if (shaderProgram.fogShape != null) shaderProgram.fogShape.set(RenderSystem.getShaderFogShape().getId());
        if (shaderProgram.textureMat != null) shaderProgram.textureMat.set(RenderSystem.getTextureMatrix());
        if (shaderProgram.gameTime != null) shaderProgram.gameTime.set(RenderSystem.getShaderGameTime());

        RenderSystem.setupShaderLights(shaderProgram);
        shaderProgram.bind();
        GlUniform glUniform = shaderProgram.chunkOffset;

        worldRenderer.chunkInfos.stream().filter(info -> new ChunkPos(info.chunk.getOrigin()).equals(pos)).forEach(info -> {
            ChunkBuilder.BuiltChunk builtChunk = info.chunk;
            if (!builtChunk.getData().isEmpty(renderLayer)) {
                VertexBuffer vertexBuffer = builtChunk.getBuffer(renderLayer);
                BlockPos blockPos = builtChunk.getOrigin();
                if (glUniform != null) {
                    glUniform.set((float)((double)blockPos.getX() - cameraX), (float)((double)blockPos.getY() - cameraY), (float)((double)blockPos.getZ() - cameraZ));
                    glUniform.upload();
                }

                vertexBuffer.bind();
                vertexBuffer.draw();
            }

            if (glUniform != null) glUniform.set(0.0F, 0.0F, 0.0F);
        });

        shaderProgram.unbind();
        VertexBuffer.unbind();
        renderLayer.endDrawing();
    }

    public static void render(MatrixStack matrices, ChunkPos chunk) {
        Matrix4f projection = gameRenderer.getBasicProjectionMatrix(70.0F);

        Vector3f cam = MinecraftClient.getInstance().cameraEntity.getPos().toVector3f();

        //Vector3f pos = new Vector3f(chunk.getCenterX(), 140, chunk.getCenterZ());
        //Vector3f cam = pos.sub(-16, 0, 16);
        //projection.lookAt(cam, pos, new Vector3f(0, 1, 0));

        renderLayer(RenderLayer.getSolid(), matrices, cam.x, cam.y, cam.z, projection, chunk);
        renderLayer(RenderLayer.getCutoutMipped(), matrices, cam.x, cam.y, cam.z, projection, chunk);
        renderLayer(RenderLayer.getCutout(), matrices, cam.x, cam.y, cam.z, projection, chunk);
    }
}