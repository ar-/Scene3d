package com.github.hoboris.scenes.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.IntBuffer;

/**
 * Adapter to display a Stage3D and its Actor3D within a Scene2D Actor
 */
public class Stage3DAdapterActor extends Actor implements Disposable {

    private Stage3D stage;

    private IntBuffer viewportBounds;

    public Stage3DAdapterActor() {
        viewportBounds = BufferUtils.newIntBuffer(4);
        stage = new Stage3D();
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        batch.end();

        Camera camera = stage.getCamera();

        camera.viewportWidth = (int)(getWidth() * getScaleX());
        camera.viewportHeight = (int)(getHeight() * getScaleY());
        if (!stage.getRoot().isVisible()) return;

        Vector2 position = localToStageCoordinates(new Vector2());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewportBounds);

        Gdx.gl.glViewport(
                (int)(position.x - getOriginX() * getScaleX() + getOriginX()),
                (int)(position.y - getOriginY() * getScaleY() + getOriginY()),
                (int)(getWidth() * getScaleX()),
                (int)(getHeight() * getScaleY()));

        stage.draw();

        Gdx.gl.glViewport(viewportBounds.get(0), viewportBounds.get(1), viewportBounds.get(2), viewportBounds.get(3));

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stage.act(delta);
    }

    public void addActor(Actor3D actor) {
        stage.addActor(actor);
    }

    public void addAction(Action3D action) {
        stage.addAction(action);
    }

    public Array<Actor3D> getActors() {
        return stage.getActors();
    }

    public boolean addListener (Event3DListener listener) {
        return stage.addListener(listener);
    }

    public boolean removeListener (Event3DListener listener) {
        return stage.removeListener(listener);
    }

    @Override
    public void clear () {
        super.clear();
        stage.clear();
    }

    public ModelBatch getModelBatch () {
        return stage.getModelBatch();
    }

    public Camera3D getCamera () {
        return stage.getCamera();
    }

    public void setCamera (Camera3D camera) {
        stage.setCamera(camera);
    }

    public Group3D getRoot () {
        return stage.getRoot();
    }

    public void setEnvironment(Environment environment){
        stage.setEnvironment(environment);
    }

    public Environment getEnvironment(){
        return stage.getEnvironment();
    }
    
    public Actor3D getObject(int screenX, int screenY) {
         return stage.getObject(screenX, screenY);
    }
    
    public Actor3D hit(int screenX, int screenY, Actor3D actor) {
        return stage.hit(screenX, screenY, actor);
    }
    
    public Actor3D hit(int screenX, int screenY, Group3D group) {
         return stage.hit(screenX, screenY, group);
    }

    @Override
    public void setDebug(boolean enabled) {
        setDebug(enabled, false);
    }

    public void setDebug(boolean enabled, boolean recursively, ModelBuilder modelBuilder) {
        super.setDebug(enabled);
        stage.setDebug(enabled, recursively, modelBuilder);
    }

    public void setDebug(boolean enabled, boolean recursively) {
        setDebug(enabled, recursively, new ModelBuilder());
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
