import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.glu.GLU;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class SolarSystem implements GLEventListener {
    private float angle = 0.0f;
    private final GLU glu = new GLU();
    private final ArrayList<Star> stars = new ArrayList<>();

    private long lastStarUpdateTime = System.currentTimeMillis();
    private final long starUpdateInterval = 5000; // 5 seconds
    private final Random rand = new Random();

    private static class Star {
        float x, y, brightness;
        Star(float x, float y, float brightness) {
            this.x = x;
            this.y = y;
            this.brightness = brightness;
        }
    }

    public static void main(String[] args) {
        java.awt.Toolkit.getDefaultToolkit();
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println("ERROR: Running in a headless environment!");
            System.exit(1);
        }

        GLProfile profile;
        try {
            profile = GLProfile.get(GLProfile.GL2);
        } catch (GLException e) {
            System.err.println("GL2 profile not available.");
            return;
        }

        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);

        SolarSystem solarSystem = new SolarSystem();
        canvas.addGLEventListener(solarSystem);
        canvas.setSize(800, 600);

        JFrame frame = new JFrame("Enhanced JOGL Solar System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        FPSAnimator animator = new FPSAnimator(canvas, 60, true);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0.05f, 1f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_POINT_SMOOTH);

        generateStars(); // Initialize stars
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        glu.gluLookAt(0, 0, 3.0, 0, 0, 0, 0, 1, 0);

        // Update stars every 5 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStarUpdateTime >= starUpdateInterval) {
            generateStars(); // Reposition and change brightness
            lastStarUpdateTime = currentTime;
        }

        drawStars(gl);

        // Sun with glow
        drawCircle(gl, 0f, 0f, 0.25f, new float[]{1f, 0.5f, 0.0f}); // Outer glow
        drawCircle(gl, 0f, 0f, 0.2f, new float[]{1f, 0.8f, 0.0f});  // Core

        // Mercury
        drawOrbit(gl, 0.4f);
        drawPlanet(gl, 0.4f, angle * 2.0f, 0.05f, new float[]{0.7f, 0.7f, 0.7f});

        // Venus
        drawOrbit(gl, 0.55f);
        drawPlanet(gl, 0.55f, angle * 1.5f, 0.06f, new float[]{1f, 0.8f, 0.6f});

        // Earth + Moon
        drawOrbit(gl, 0.7f);
        float earthX = (float) Math.cos(Math.toRadians(angle)) * 0.7f;
        float earthY = (float) Math.sin(Math.toRadians(angle)) * 0.7f;
        drawCircle(gl, earthX, earthY, 0.07f, new float[]{0.2f, 0.5f, 1f});

        float moonAngle = angle * 5f;
        float moonX = earthX + (float) Math.cos(Math.toRadians(moonAngle)) * 0.1f;
        float moonY = earthY + (float) Math.sin(Math.toRadians(moonAngle)) * 0.1f;
        drawCircle(gl, moonX, moonY, 0.02f, new float[]{0.9f, 0.9f, 0.9f});

        // Mars
        drawOrbit(gl, 1.0f);
        drawPlanet(gl, 1.0f, -angle * 0.7f, 0.06f, new float[]{1f, 0.3f, 0.2f});

        angle += 0.5f;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height == 0) height = 1;
        float aspect = (float) width / height;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, aspect, 1.0, 10.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void drawCircle(GL2 gl, float cx, float cy, float radius, float[] color) {
        int numSegments = 100;
        gl.glColor3fv(color, 0);
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;
            float x = cx + (float) Math.cos(angle) * radius;
            float y = cy + (float) Math.sin(angle) * radius;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawPlanet(GL2 gl, float orbitRadius, float angleDeg, float size, float[] color) {
        double rad = Math.toRadians(angleDeg);
        float x = (float) Math.cos(rad) * orbitRadius;
        float y = (float) Math.sin(rad) * orbitRadius;
        drawCircle(gl, x, y, size, color);
    }

    private void drawOrbit(GL2 gl, float radius) {
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        for (int i = 0; i < 100; i++) {
            double angle = 2 * Math.PI * i / 100;
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    private void drawStars(GL2 gl) {
        gl.glPointSize(1.5f);
        gl.glBegin(GL2.GL_POINTS);
        for (Star star : stars) {
            gl.glColor3f(star.brightness, star.brightness, star.brightness);
            gl.glVertex2f(star.x, star.y);
        }
        gl.glEnd();
    }

    private void generateStars() {
        stars.clear();
        for (int i = 0; i < 50; i++) {
            float x = rand.nextFloat() * 3.0f - 1.5f;
            float y = rand.nextFloat() * 3.0f - 1.5f;
            float brightness = rand.nextFloat() * 0.5f + 0.5f;
            stars.add(new Star(x, y, brightness));
        }
    }
}
