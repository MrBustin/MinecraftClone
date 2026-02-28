package org.minecraftclone.core;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.minecraftclone.gfx.*;
import org.minecraftclone.world.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private ShaderProgram shader;
    private Mesh triangle;
    private Camera camera;
    private Matrix4f projection;
    private World world;
    private Texture texture;
    private ChunkManager chunkManager;
    private java.util.Map<Long, Mesh> chunkMeshes = new java.util.HashMap<>();
    private static final int VIEW_DISTANCE = 6;   // chunks
    private int frameCounter = 0;

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
            FragColor = texture(uTex, vUV);
        }
        """;
    public void init() {
        GL.createCapabilities();
        world = new World(16, 8, 16);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.545f, 0.545f, 1.0f, 1.0f);

        shader = new ShaderProgram(VERT, FRAG);
        texture = new Texture("/textures/atlas.png");

        camera = new Camera();
        projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0),
                1280f / 720f,
                0.1f,
                1000f
        );

        chunkManager = new ChunkManager();

// preload a small area for testing
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                chunkManager.getOrCreate(cx, cz);
            }
        }

        triangle = new Mesh(Primitives.cubePUV());
    }

    public void beginFrame() {
        updateChunkStreaming();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shader.bind();

        Matrix4f view = camera.getViewMatrix();
        Matrix4f vp = new Matrix4f(projection).mul(view);



        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        for (Chunk c : chunkManager.loadedChunksSnapshot()) {
            if (c.isDirty() || !chunkMeshes.containsKey(new ChunkPos(c.cx(), c.cz()).key())) {
                float[] data = ChunkMesher.buildMesh(chunkManager, c);

                // rebuild mesh
                long key = new ChunkPos(c.cx(), c.cz()).key();
                Mesh old = chunkMeshes.get(key);
                if (old != null) old.cleanup();

                chunkMeshes.put(key, new Mesh(data));
                c.clearDirty();
            }

            // Draw the mesh with chunk offset
            long key = new ChunkPos(c.cx(), c.cz()).key();
            Mesh mesh = chunkMeshes.get(key);
            if (mesh == null) continue;

            Matrix4f model = new Matrix4f().translate(c.cx() * Chunk.SIZE, 0, c.cz() * Chunk.SIZE);
            Matrix4f mvp = new Matrix4f(vp).mul(model);

            shader.setMat4("uMVP", mvp);
            mesh.draw();
        }

        shader.unbind();
    }

    public void endFrame(Window window) {
        glfwSwapBuffers(window.handle());
        glfwPollEvents();
    }

    public void cleanup() {
        if (triangle != null) triangle.cleanup();
        if (shader != null) shader.cleanup();
        if (texture != null) texture.cleanup();
        for (Mesh m : chunkMeshes.values()) m.cleanup();
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
        // Only update every ~10 frames (tune later)
        frameCounter++;
        if (frameCounter % 10 != 0) return;

        int playerX = (int) Math.floor(camera.position.x);
        int playerZ = (int) Math.floor(camera.position.z);

        int centerCx = floorDiv(playerX, Chunk.SIZE);
        int centerCz = floorDiv(playerZ, Chunk.SIZE);

        // Load around player
        chunkManager.ensureLoadedAround(centerCx, centerCz, VIEW_DISTANCE);

        // Unload far chunks, and free their meshes
        java.util.List<Long> unloaded = chunkManager.unloadOutsideRadius(centerCx, centerCz, VIEW_DISTANCE);
        for (long key : unloaded) {
            Mesh m = chunkMeshes.remove(key);
            if (m != null) m.cleanup();
        }
    }
    public void onBreakBlock() {
        var hit = VoxelRaycast.raycast(chunkManager, camera.getPosition(), camera.getForward(), 6f);
        if (hit == null) return;

        chunkManager.setBlock(hit.x(), hit.y(), hit.z(), BlockType.AIR);
    }

    public void onPlaceBlock() {
        var hit = VoxelRaycast.raycast(chunkManager, camera.getPosition(), camera.getForward(), 6f);
        if (hit == null) return;

        int px = hit.x() + hit.nx();
        int py = hit.y() + hit.ny();
        int pz = hit.z() + hit.nz();

        // choose what block to place for now
        chunkManager.setBlock(px, py, pz, BlockType.DIRT);
    }
}
