/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.engine;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.cinematic.MotionPath;
import com.jme3.environment.util.BoundingSphereDebug;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.debug.WireSphere;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.ShadowUtil;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;

/**
 * @author capdevon
 */
public class JMonkey3 {

	protected static boolean initialized;
	protected static AppSettings settings;
	protected static AppStateManager stateManager;
	protected static AssetManager assetManager;
	protected static InputManager inputManager;
	protected static Node rootNode;
	protected static Node guiNode;
	protected static Camera camera;

    private JMonkey3() {
        // singleton constructor
    }

    public static void initEngine(SimpleApplication app) {
        if (!initialized) {
            initialized = true;
            JMonkey3.settings     = app.getContext().getSettings();
            JMonkey3.stateManager = app.getStateManager();
            JMonkey3.assetManager = app.getAssetManager();
            JMonkey3.inputManager = app.getInputManager();
            JMonkey3.rootNode     = app.getRootNode();
            JMonkey3.guiNode      = app.getGuiNode();
            JMonkey3.camera       = app.getCamera();
        }
    }

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.UIEditor
     * -------------------------------------------------------------------------
     */
    public static class UIEditor {

        public static BitmapFont guiFont;
        public static ColorRGBA color = ColorRGBA.Red;

        /**
         * A centered plus sign to help the player aim.
         */
        public static BitmapText getCrossHair() {
            return getCrossHair("+");
        }

        public static BitmapText getCrossHair(String text) {
            BitmapFont font = getGuiFont();
            BitmapText ch = new BitmapText(font, false);
            ch.setSize(font.getCharSet().getRenderedSize() * 2);
            ch.setText(text);
            float width = settings.getWidth() / 2 - ch.getLineWidth() / 2;
            float height = settings.getHeight() / 2 + ch.getLineHeight() / 2;
            ch.setLocalTranslation(width, height, 0);
            return ch;
        }

        public static BitmapText getText(float xPos, float yPos) {
            return getText(xPos, yPos, true);
        }

        public static BitmapText getText(float xPos, float yPos, boolean show) {
            BitmapFont font = getGuiFont();
            BitmapText hud = new BitmapText(font, false);
            hud.setSize(font.getCharSet().getRenderedSize());
            hud.setLocalTranslation(xPos, yPos, 0);
            hud.setColor(color);
            if (show) {
                guiNode.attachChild(hud);
            }
            return hud;
        }

        public static BitmapFont getGuiFont() {
            if (guiFont == null) {
                guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            }
            return guiFont;
        }

        public static Node getGuiNode() {
            return guiNode;
        }
    }

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.Gizmos
     * -------------------------------------------------------------------------
     */
    public static class Gizmos {

        public static ColorRGBA color = ColorRGBA.Red;

        /**
         * Draws a wireframe cube with center and size.
         *
         * @param center
         * @param size
         */
        public static void drawWireBox(Vector3f center, Vector3f size) {
            Geometry g = new Geometry("Box.WireMesh", new WireBox(size.x, size.y, size.z));
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            g.setMaterial(mat);
            g.setLocalTranslation(center);
            rootNode.attachChild(g);
        }

        /**
         * Draws a wireframe sphere with center and radius.
         *
         * @param center
         * @param radius
         */
        public static void drawWireSphere(Vector3f center, float radius) {
            Geometry g = new Geometry("Sphere.WireMesh", new WireSphere(radius));
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            g.setMaterial(mat);
            g.setLocalTranslation(center);
            rootNode.attachChild(g);
        }

        /**
         * Draws a wireframe grid with offset and size.
         *
         * @param offset
         * @param size
         */
        public static void drawWireGrid(Vector3f offset, int size) {
            Geometry g = new Geometry("Grid.WireMesh", new Grid(size, size, 1f));
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            g.setMaterial(mat);
            g.center().move(offset);
            rootNode.attachChild(g);
        }

        /**
         * Draws world bound.
         *
         * @param sp
         */
        public static void drawWorldBound(Spatial sp) {
            BoundingVolume bv = sp.getWorldBound();
            Mesh mesh = null;

            if (bv.getType() == BoundingVolume.Type.AABB) {
                BoundingBox bb = (BoundingBox) bv;
                mesh = new WireBox(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
            }
            if (bv.getType() == BoundingVolume.Type.Sphere) {
                BoundingSphere bs = (BoundingSphere) bv;
                mesh = new WireSphere(bs.getRadius());
            }

            Geometry geom = new Geometry("WorldBound.WireMesh", mesh);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            geom.setMaterial(mat);
            geom.setLocalTranslation(sp.getWorldTranslation());
            rootNode.attachChild(geom);
        }

        /**
         *
         * @param points
         * @param node
         */
        public static void drawPoints(List < Vector3f > points, Node node) {
            MotionPath path = new MotionPath();
            for (Vector3f p: points) {
                path.addWayPoint(p);
            }
            path.enableDebugShape(assetManager, node);
        }
    }
    
    /**
     * -------------------------------------------------------------------------
     * JMonkey3.Debug
     * -------------------------------------------------------------------------
     */
	public static class Debug {

		public static Geometry createCameraFrustum(Camera cam) {

			Vector3f[] points = new Vector3f[8];
			for (int i = 0; i < 8; i++) {
				points[i] = new Vector3f();
			}

			Camera frustumCam = cam.clone();
			frustumCam.setLocation(new Vector3f(0, 0, 0));
			frustumCam.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
			ShadowUtil.updateFrustumPoints2(frustumCam, points);
			Mesh mesh = WireFrustum.makeFrustum(points);

			Geometry frustumGeo = new Geometry("Viewing.Frustum", mesh);
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", ColorRGBA.White);
			frustumGeo.setMaterial(mat);
			frustumGeo.setCullHint(Spatial.CullHint.Never);
			frustumGeo.setShadowMode(RenderQueue.ShadowMode.Off);

			return frustumGeo;
		}

		public static Geometry createDebugSphere(float scale) {
			Geometry geo = BoundingSphereDebug.createDebugSphere(assetManager);
			geo.setShadowMode(RenderQueue.ShadowMode.Off);
			geo.setLocalScale(scale);
			return geo;
		}
	}

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.Primitive
     * -------------------------------------------------------------------------
     */
    public static class Primitive {

        /**
         * Get default axes.
         *
         * @param id
         * @return 
         */
        public static Node createAxes(String id) {
            Node node = new Node(id);
            node.attachChild(createArrow("X", Vector3f.UNIT_X, ColorRGBA.Red));
            node.attachChild(createArrow("Y", Vector3f.UNIT_Y, ColorRGBA.Green));
            node.attachChild(createArrow("Z", Vector3f.UNIT_Z, ColorRGBA.Blue));
            return node;
        }

        /**
         * Get an arrow with name, dir and color.
         *
         * @param name
         * @param dir
         * @param color
         * @return
         */
        public static Geometry createArrow(String name, Vector3f dir, ColorRGBA color) {
            Arrow arrow = new Arrow(dir);
            Geometry g = new Geometry(name, arrow);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            g.setMaterial(mat);
            return g;
        }

        /**
         * Get a solid cube with color and size.
         *
         * @param color
         * @param size
         * @return
         */
        public static Geometry createCube(ColorRGBA color, Vector3f size) {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            return createCube(mat, size);
        }

        public static Geometry createCube(Material mat, Vector3f size) {
            Box box = new Box(size.x, size.y, size.z);
            Geometry geo = new Geometry("Box.GeoMesh", box);
            geo.setMaterial(mat);
            return geo;
        }

        /**
         * Get a solid sphere with color and radius.
         *
         * @param color
         * @param radius
         * @return
         */
        public static Geometry createSphere(ColorRGBA color, float radius) {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            return createSphere(mat, radius);
        }

        public static Geometry createSphere(Material mat, float radius) {
            Sphere sphere = new Sphere(6, 6, radius);
            Geometry geo = new Geometry("Sphere.GeoMesh", sphere);
            geo.setMaterial(mat);
            return geo;
        }

        public static Node createCapsule(ColorRGBA color, float radius, float height) {
            Node capsule = new Node("Capsule");
            Geometry cylinder = new Geometry("Cylinder", new Cylinder(16, 16, radius, height));
            Geometry top = new Geometry("Top.Sphere", new Sphere(16, 16, radius));
            Geometry bottom = new Geometry("Bottom.Sphere", new Sphere(16, 16, radius));
            cylinder.rotate(FastMath.HALF_PI, 0, 0);
            bottom.setLocalTranslation(0, -height / 2, 0);
            top.setLocalTranslation(0, height / 2, 0);
            capsule.attachChild(cylinder);
            capsule.attachChild(bottom);
            capsule.attachChild(top);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            capsule.setMaterial(mat);
            return capsule;
        }

    }

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.MatDefs
     * -------------------------------------------------------------------------
     */
    public static class MatDefs {

        /**
         * Create a lighting material from the specified texture.
         *
         * @param texture
         * @return
         */
        public static Material getLighting(String texture) {
            Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            m.setTexture("DiffuseMap", assetManager.loadTexture(texture));
            return m;
        }

        /**
         * Create a lighting material with diffuse color.
         * @param color
         * @return
         */
        public static Material getLighting(ColorRGBA color) {
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", color);
            return mat;
        }

        /**
         * Create an unshaded material from the specified texture.
         *
         * @param texture
         * @return
         */
        public static Material getUnshaded(String texture) {
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setTexture("ColorMap", assetManager.loadTexture(texture));
            return m;
        }

        /**
         * Create an unshaded material with color.
         *
         * @param color
         * @return
         */
        public static Material getUnshaded(ColorRGBA color) {
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setColor("Color", color);
            return m;
        }

        /**
         * Create an unshaded material with color and alpha.
         *
         * @param color
         * @param alpha
         * @return
         */
        public static Material getUnshaded(ColorRGBA color, float alpha) {
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setColor("Color", new ColorRGBA(color.r, color.g, color.b, alpha));
            m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            return m;
        }
    }

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.Assets
     * -------------------------------------------------------------------------
     */
    public static class Assets {

        public static Spatial getSkyCubeMap(String sky) {
            return SkyFactory.createSky(assetManager, sky, EnvMapType.CubeMap);
        }

        public static Spatial getSkySphereMap(String sky) {
            return SkyFactory.createSky(assetManager, sky, EnvMapType.SphereMap);
        }

        public static AudioNode getAudioStream(String audio) {
            return new AudioNode(assetManager, audio, DataType.Stream);
        }

        public static AudioNode getAudioBuffer(String audio) {
            return new AudioNode(assetManager, audio, DataType.Buffer);
        }

        public static Spatial getModel(String model) {
            return assetManager.loadModel(model);
        }

        public static Texture getTexture(String name) {
            return assetManager.loadTexture(name);
        }

    }
    
    /**
     * -------------------------------------------------------------------------
     * JMonkey3.GameObject
     * -------------------------------------------------------------------------
     */
    public static class GameObject {
    	
        public static void setUserDataRecursive(Spatial sp, final String key, final Object data) {
            sp.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit(Geometry sp) {
                    sp.setUserData(key, data);
                }
            });
        }

        /**
         * Finds a GameObject by name and returns it. 
         * If no GameObject with name can be found, null is returned
         * 
         * @param name
         * @return 
         */
        public static Node find(final String name) {
            final List<Node> lst = new ArrayList<>();
            rootNode.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit(Node node) {
                    if (name.equals(node.getName())) {
                        lst.add(node);
                    }
                }
            });
            return lst.isEmpty() ? null : lst.get(0);
        }
        
        /**
         * Returns one active GameObject tagged tag. 
         * Returns null if no GameObject was found.
         * 
         * @param tag
         * @return
         */
        public static Node findWithTag(final String tag) {
            List<Node> lst = findGameObjectsWithTag(tag);
            return lst.isEmpty() ? null : lst.get(0);
        }

        /**
         * Returns an array of active GameObjects tagged tag. 
         * Returns empty array if no GameObject was found.
         * 
         * @param tag
         * @return
         */
        public static List<Node> findGameObjectsWithTag(final String tag) {
            final List<Node> lst = new ArrayList<>();
            rootNode.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit(Node node) {
                    if (tag.equals(node.getUserData("TagName"))) {
                        lst.add(node);
                    }
                }
            });
            return lst;
        }

        /**
         * Returns the first active loaded object of Type type.
         * 
         * @param clazz
         * @return 
         */
        public static Node findObjectOfType(Class<? extends Control> clazz) {
            List<Node> lst = findObjectsOfType(clazz);
            return lst.isEmpty() ? null : lst.get(0);
        }

        /**
         * Returns a list of all active loaded objects of Type type.
         * 
         * @param clazz
         * @return 
         */
        public static List<Node> findObjectsOfType(Class<? extends Control> clazz) {
            final List<Node> lst = new ArrayList<>();
            rootNode.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
                @Override
                public void visit(Node node) {
                    if (node.getControl(clazz) != null) {
                        lst.add(node);
                    }
                }
            });
            return lst;
        }

        /**
         * Returns the component of Type type in the GameObject or any of its children using depth first search.
         * @param <T>
         * @param spatial
         * @param clazz
         * @return
         */
        public static <T extends Control> T getComponentInChild(Spatial spatial, Class<T> clazz) {
            T control = spatial.getControl(clazz);
            if (control != null) {
                return control;
            }

            if (spatial instanceof Node) {
                for (Spatial child : ((Node) spatial).getChildren()) {
                    control = getComponentInChild(child, clazz);
                    if (control != null) {
                        return control;
                    }
                }
            }

            return null;
        }
        
        /**
         * Retrieves the component of Type type in the GameObject or any of its parents.
         * @param <T>
         * @param spatial
         * @param clazz
         * @return 
         */
        public static <T extends Control> T getComponentInParent(Spatial spatial, Class<T> clazz) {
            Node parent = spatial.getParent();
            while (parent != null) {
                T control = parent.getControl(clazz);
                if (control != null) {
                    return control;
                }
                parent = parent.getParent();
            }
            return null;
        }
    	
    }

    /**
     * -------------------------------------------------------------------------
     * JMonkey3.Engine
     * -------------------------------------------------------------------------
     */
    public static class Engine {

        public static <T extends AppState> T getState(Class <T> clazz) {
            return stateManager.getState(clazz);
        }

        public static Node getRootNode() {
            return rootNode;
        }

        public static Camera getMainCamera() {
            return camera;
        }

        public static int getScreenWidth() {
            return settings.getWidth();
        }

        public static int getScreenHeight() {
            return settings.getHeight();
        }

        public static boolean useJoysticks() {
            return settings.useJoysticks();
        }

    }

}