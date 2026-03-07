package org.minecraftclone.core;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;
import org.minecraftclone.gfx.*;
import org.minecraftclone.world.*;
import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;
import org.minecraftclone.world.chunk.ChunkMesher;
import org.minecraftclone.world.chunk.ChunkPos;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private ShaderProgram shader;
    private Camera camera;
    private Matrix4f projection;
    private Texture texture;
    private ChunkManager chunkManager;

    private Map<Long, ChunkRenderData> chunkMeshes = new HashMap<>();
    private static final int VIEW_DISTANCE = 12;   // chunks
    private int frameCounter = 0;

    private int fps;
    private int frames;
    private double fpsTimer;

    private static final int REMESH_BUDGET_PER_FRAME = 4; // start with 2-4

    private static final String VERT = """
        #version 330 core
        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec2 aUV;

        uniform mat4 uMVP;
        out vec2 vUV;

        void main() {
            vUV = aUV;
            gl_Position = uMVP * vec4(aPos, 1.0);
        }
        """;

    private static final String FRAG = """
        #version 330 core
        in vec2 vUV;
        out vec4 FragColor;

        uniform sampler2D uTex;

        void main() {
            vec4 c = texture(uTex, vUV);
            if (c.a < 0.5) discard;
            FragColor = c;
        }
        """;

    private static class ChunkRenderData {
        Mesh solid;
        Mesh water;
    }

    public void init() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.545f, 0.545f, 1.0f, 1.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);

        shader = new ShaderProgram(VERT, FRAG);
        texture = new Texture("/textures/atlas.png");

        camera = new Camera();

        // depth precision fix: raise near plane a bit (helps your huge terrain)
        projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0),
                1280f / 720f,
                0.5f,
                1000f
        );

        chunkManager = new ChunkManager();
    }

    public void beginFrame(Window window) {

        double now = glfwGetTime();
        frames++;

        if (now - fpsTimer >= 1.0) {
            fps = frames;
            frames = 0;
            fpsTimer = now;
        }

        updateChunkStreaming();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();

        Matrix4f view = camera.getViewMatrix();
        Matrix4f vp = new Matrix4f(projection).mul(view);

        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        // rebuild meshes if needed
        int remeshed = 0;

        for (Chunk c : chunkManager.loadedChunksSnapshot()) {
            if (remeshed >= REMESH_BUDGET_PER_FRAME) break;
            long key = new ChunkPos(c.cx(), c.cz()).key();
            if (c.isDirty() || !chunkMeshes.containsKey(key)) {

                ChunkMesher.ChunkMeshData data = ChunkMesher.buildMesh(chunkManager, c);

                ChunkRenderData old = chunkMeshes.get(key);
                if (old != null) {
                    if (old.solid != null) old.solid.cleanup();
                    if (old.water != null) old.water.cleanup();
                }

                ChunkRenderData rd = new ChunkRenderData();
                if (data.solid().length > 0) rd.solid = new Mesh(data.solid());
                if (data.water().length > 0) rd.water = new Mesh(data.water());

                chunkMeshes.put(key, rd);
                c.clearDirty();
                remeshed++;
            }
        }

        // ---- PASS 1: SOLIDS ----
        glDepthMask(true);

        for (Chunk c : chunkManager.loadedChunksSnapshot()) {
            long key = new ChunkPos(c.cx(), c.cz()).key();
            ChunkRenderData rd = chunkMeshes.get(key);
            if (rd == null || rd.solid == null) continue;

            Matrix4f model = new Matrix4f().translate(c.cx() * Chunk.SIZE, 0, c.cz() * Chunk.SIZE);
            Matrix4f mvp = new Matrix4f(vp).mul(model);

            shader.setMat4("uMVP", mvp);
            rd.solid.draw();
        }

        // ---- PASS 2: WATER (transparent) ----
        glDepthMask(false);

        for (Chunk c : chunkManager.loadedChunksSnapshot()) {
            long key = new ChunkPos(c.cx(), c.cz()).key();
            ChunkRenderData rd = chunkMeshes.get(key);
            if (rd == null || rd.water == null) continue;

            Matrix4f model = new Matrix4f().translate(c.cx() * Chunk.SIZE, -0.125f, c.cz() * Chunk.SIZE);
            Matrix4f mvp = new Matrix4f(vp).mul(model);

            shader.setMat4("uMVP", mvp);
            rd.water.draw();
        }

        glDepthMask(true);

        shader.unbind();
        renderFPS(window);
    }

    public void endFrame(Window window) {
        glfwSwapBuffers(window.handle());
        glfwPollEvents();
    }

    public void cleanup() {
        if (shader != null) shader.cleanup();
        if (texture != null) texture.cleanup();

        for (ChunkRenderData rd : chunkMeshes.values()) {
            if (rd.solid != null) rd.solid.cleanup();
            if (rd.water != null) rd.water.cleanup();
        }
        chunkMeshes.clear();
    }

    public Camera getCamera() {
        return camera;
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    private void updateChunkStreaming() {
        frameCounter++;
        if (frameCounter % 10 != 0) return;

        int playerX = (int) Math.floor(camera.position.x);
        int playerZ = (int) Math.floor(camera.position.z);

        int centerCx = floorDiv(playerX, Chunk.SIZE);
        int centerCz = floorDiv(playerZ, Chunk.SIZE);

        chunkManager.ensureLoadedAround(centerCx, centerCz, VIEW_DISTANCE);

        var unloaded = chunkManager.unloadOutsideRadius(centerCx, centerCz, VIEW_DISTANCE);
        for (long key : unloaded) {
            ChunkRenderData rd = chunkMeshes.remove(key);
            if (rd != null) {
                if (rd.solid != null) rd.solid.cleanup();
                if (rd.water != null) rd.water.cleanup();
            }
        }
    }

    public void onBreakBlock() {
        var hit = VoxelRaycast.raycast(chunkManager, camera.getPosition(), camera.getForward(), 6f);
        if (hit == null) return;
        chunkManager.setBlock(hit.x(), hit.y(), hit.z(), Blocks.AIR);
    }

    public void onPlaceBlock() {
        var hit = VoxelRaycast.raycast(chunkManager, camera.getPosition(), camera.getForward(), 6f);
        if (hit == null) return;

        int px = hit.x() + hit.nx();
        int py = hit.y() + hit.ny();
        int pz = hit.z() + hit.nz();

        chunkManager.setBlock(px, py, pz, Blocks.FLOWER);
    }

    private void renderFPS(Window window) {
        String text = "FPS: " + fps;

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vid = glfwGetVideoMode(monitor);
        assert vid != null;
        int width = vid.width();
        int height = vid.height();

        // STB buffer (each char ~270 bytes worst case)
        var buffer = MemoryUtil.memAlloc(text.length() * 270);

        int quads = STBEasyFont.stb_easy_font_print(
                0, 0, text, null, buffer
        );

        // Switch to 2D orthographic projection
        glUseProgram(0); // no shader

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);

        glColor3f(1f, 1f, 1f);

        // Position top-right
        glTranslatef(width - 120, 20, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 16, buffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glDisableClientState(GL_VERTEX_ARRAY);

        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        MemoryUtil.memFree(buffer);
    }
}