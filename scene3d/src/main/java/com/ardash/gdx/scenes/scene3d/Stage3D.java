package com.ardash.gdx.scenes.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;

public class Stage3D extends InputAdapter implements Disposable {
    private final ModelBatch modelBatch;
    private Environment environment;

    private Camera3D camera;

    private final Group3D root;

    /** Creates a stage with a viewport equal to the device screen resolution. The stage
     * will use its own {@link SpriteBatch}. */
    public Stage3D() {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Creates a stage with the specified viewport that doesn't keep the aspect ratio.
     * The stage will use its own {@link SpriteBatch}, which will be disposed when the stage is disposed. */
    public Stage3D(float width, float height) {
        this(width, height, new Environment());

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    public Stage3D(float width, float height, Environment environment) {
        root = new Group3D();
        root.setStage(this);

        modelBatch = new ModelBatch();

        camera =  new Camera3D(width, height);
        this.environment = environment;
    }

    public void draw(){
        camera.update();
        if (!root.isVisible()) return;
        modelBatch.begin(camera);
        root.draw(modelBatch, environment);
        modelBatch.end();
    }

    /** Calls {@link #act(float)} with {@link Graphics#getDeltaTime()}. */
    public void act () {
        act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
    }

    /** Calls the {@link Actor#act(float)} method on each actor in the stage. Typically called each frame. This method also fires
     * enter and exit events.
     * @param delta Time in seconds since the last frame. */
    public void act(float delta) {
        root.act(delta);
    }

    /** Adds an actor to the root of the stage.
     * @see Group3D#addActor(Actor3D)
     * @see Actor#remove() */
    public void addActor(Actor3D actor) {
        root.addActor(actor);
    }

    /** Adds an action to the root of the stage.
     * @see Group3D#addAction(Action3D) */
    public void addAction(Action3D action) {
        root.addAction(action);
    }

    /** Returns the root's child actors.
     * @see Group#getChildren() */
    public Array<Actor3D> getActors() {
        return root.getChildren();
    }

    /** Adds a listener to the root.
     * @see Actor#addListener(EventListener) */
    public boolean addListener (Event3DListener listener) {
        return root.addListener(listener);
    }

    /** Removes a listener from the root.
     * @see Actor#removeListener(EventListener) */
    public boolean removeListener (Event3DListener listener) {
        return root.removeListener(listener);
    }

    /** Removes the root's children, actions, and listeners. */
    public void clear () {
        root.clear();
    }

    public ModelBatch getModelBatch () {
        return modelBatch;
    }

    public Camera3D getCamera () {
        return camera;
    }

    /** Sets the stage's camera. The camera must be configured properly. {@link Stage#draw()} will call {@link Camera#update()} and use the {@link Camera#combined} matrix
     * for the SpriteBatch {@link SpriteBatch#setProjectionMatrix(com.badlogic.gdx.math.Matrix4) projection matrix}. */
    public void setCamera (Camera3D camera) {
        this.camera = camera;
    }

    /** Returns the root group which holds all actors in the stage. */
    public Group3D getRoot () {
        return root;
    }

    public void setEnvironment(Environment environment){
        this.environment = environment;
    }

    public Environment getEnvironment(){
        return environment;
    }
    
    public Actor3D getObject(int screenX, int screenY) {
         Actor3D temp = null;
         SnapshotArray<Actor3D> children = root.getChildren();
         Actor3D[] actors = children.begin();
         for (int i = 0, n = children.size; i < n; i++){
             temp = hit(screenX, screenY, actors[i]);
             if (actors[i] instanceof Group3D)
                 temp = hit(screenX, screenY, (Group3D)actors[i]);
         }
         children.end();
         return temp;
    }
    
    public Actor3D hit(int screenX, int screenY, Actor3D actor3D) {
        Ray ray = camera.getPickRay(screenX, screenY);
        final float dist2 = actor3D.intersects(ray);
        if (dist2 >= 0) {
            return actor3D;
        }
        return null;
    }
    
    public Actor3D hit(int screenX, int screenY, Group3D group3d) {
         Actor3D temp = null;
         SnapshotArray<Actor3D> children = group3d.getChildren();
         Actor3D[] actors = children.begin();
         for (int i = 0, n = children.size; i < n; i++){
             temp = hit(screenX, screenY, actors[i]);
             if (actors[i] instanceof Group3D)
                 temp = hit(screenX, screenY, (Group3D)actors[i]);
         }
         children.end();
         return temp;
    }

    /** If true, {@link Actor3D#drawDebug(ModelBatch, Environment)} will be called for this group and, optionally, all children recursively. */
    public void setDebug (boolean enabled, boolean recursively, ModelBuilder modelBuilder) {
        root.setDebug(enabled, recursively, modelBuilder);
    }

    public void setDebug (boolean enabled, boolean recursively) {
        root.setDebug(enabled, recursively);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        root.dispose();
        clear();
    }
}
