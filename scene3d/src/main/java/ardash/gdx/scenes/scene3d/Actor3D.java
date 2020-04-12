package ardash.gdx.scenes.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.Disposable;

public class Actor3D extends ModelInstance implements Disposable {
    private Stage3D stage3D;
    private Group3D parent;

    private final DelayedRemovalArray<Event3DListener> listeners = new DelayedRemovalArray<>(0);
    private final Array<Action3D> actions = new Array<>(0);

    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    private BoundingBox boundBox = new BoundingBox();
    public final float radius;

    private String name;
    private boolean visible = true;

    // Debug tools
    private boolean debug;
    private ModelInstance axis;
    private ModelBuilder modelBuilder;

    float x, y, z;
    float scaleX = 1, scaleY = 1, scaleZ = 1;
    float yaw = 0f,pitch =0f, roll=0f;
    Matrix4 rotationMatrix = new Matrix4();
    private AnimationController animation;

    public Actor3D(){
        this(new Model());
        setScale(0,0,0);
    }

    public Actor3D(Model model){
        this(model, 0f, 0f ,0f);
    }

    public Actor3D(Model model, float x, float y, float z){
        super(model);
        setPosition(x,y,z);
        calculateBoundingBox(boundBox);
        center.set(boundBox.getCenter(new Vector3()));
        dimensions.set(boundBox.getDimensions(new Vector3()));
        radius = dimensions.len() / 2f;
        animation = new AnimationController(this);
    }

    /** Updates the actor based on time. Typically this is called each frame by {@link Stage3D#act(float)}.
     * <p>
     * The default implementation calls {@link Action3D#act(float)} on each action and removes actions that are complete.
     * @param delta Time in seconds since the last frame. */
    public void act (float delta) {
        for (int i = 0; i < actions.size; i++) {
            Action3D action3D = actions.get(i);
            if (action3D.act(delta) && i < actions.size) {
                actions.removeIndex(i);
                action3D.setActor(null);
                i--;
            }
        }
        if (animation.inAction)
            animation.update(delta);
    }

    public void draw(ModelBatch modelBatch, Environment environment){
    	Gdx.gl20.glEnable(GL20.GL_BLEND);
    	Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        modelBatch.render(this, environment);
        drawDebug(modelBatch, environment);
    }

    public Actor3D hit (float x, float y) {
        return null;
    }

    /** Removes this actor from its parent, if it has a parent.
     * @see Group3D#removeActor(Actor3D) */
    public boolean remove () {
        return parent != null && parent.removeActor(this);
    }

    /** Add a listener to receive events that hit this actor.
     *
     * @see InputListener
     * @see ClickListener */
    public boolean addListener(Event3DListener listener) {
        if (!listeners.contains(listener, true)) {
            listeners.add(listener);
            return true;
        }
        return false;
    }

    public boolean removeListener (Event3DListener listener) {
        return listeners.removeValue(listener, true);
    }

    public Array<Event3DListener> getListeners () {
        return listeners;
    }

    public void addAction(Action3D action3D) {
        action3D.setActor(this);
        actions.add(action3D);
    }

    public void removeAction(Action3D action) {
        if (actions.removeValue(action, true)) action.setActor(null);
    }

    public Array<Action3D> getActions() {
        return actions;
    }

    /** Removes all actions on this actor. */
    public void clearActions() {
        for (int i = actions.size - 1; i >= 0; i--)
            actions.get(i).setActor(null);
        actions.clear();
    }

    /** Removes all listeners on this actor. */
    public void clearListeners () {
        listeners.clear();
    }

    /** Removes all actions and listeners on this actor. */
    public void clear () {
        clearActions();
        clearListeners();
    }

    /** Called by the framework when this actor or any parent is added to a group that is in the stage3D.
     * @param stage May be null if the actor or any parent is no longer in a stage. */
    protected void setStage(Stage3D stage) {
        this.stage3D = stage;
    }

    /** Returns the stage3D that this actor is currently in, or null if not in a stage. */
    public Stage3D getStage() {
        return stage3D;
    }

    /** Returns true if this actor is the same as or is the descendant of the specified actor. */
    public boolean isDescendantOf (Actor3D actor) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        Actor3D parent = this;
        while (true) {
            if (parent == null) return false;
            if (parent == actor) return true;
            parent = parent.parent;
        }
    }

    /** Returns true if this actor is the same as or is the ascendant of the specified actor. */
    public boolean isAscendantOf (Actor3D actor) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        while (true) {
            if (actor == null) return false;
            if (actor == this) return true;
            actor = actor.parent;
        }
    }

    /** Returns true if the actor's parent is not null. */
    public boolean hasParent () {
        return parent != null;
    }

    /** Returns the parent actor, or null if not in a stage. */
    public Group3D getParent () {
        return parent;

    }

    /** Called by the framework when an actor is added to or removed from a group.
     * @param parent May be null if the actor has been removed from the parent. */
    protected void setParent (Group3D parent) {
        this.parent = parent;
    }

    private static final Vector3 position = new Vector3();
    public boolean isCullable(final Camera cam) {
        return cam.frustum.sphereInFrustum(getTransform().getTranslation(position).add(center), radius);
    }

    public boolean isVisible () {
        return visible;

    }
    /** If false, the actor will not be drawn and will not receive touch events. Default is true. */
    public void setVisible (boolean visible) {
        this.visible = visible;
    }

    /** @return -1 on no intersection, or when there is an intersection: the squared distance between the center of this
     * object and the point on the ray closest to this object when there is intersection. */
    public float intersects(Ray ray) {
        transform.getTranslation(position).add(center);
        final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
        if (len < 0f)
            return -1f;
        float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
        return (dist2 <= radius * radius) ? dist2 : -1f;
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        transform.setToTranslationAndScaling(this.x, this.y, this.z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    public void translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        transform.setToTranslationAndScaling(this.x, this.y, this.z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }
    
    /*
     *  Set the actor's rotation values to new yaw, pitch and roll
     *  @param newYaw, newPitch, newRoll these values must be within 360 degrees
     */
    public void setRotation(float newYaw, float newPitch, float newRoll){
        yaw = newYaw;
        pitch = newPitch;
        roll = newRoll;
        /*
         * The libgdx library attaches the Euler angles to the wrong axis
         * Setting the yaw rotates the actor around the y axis instead of the z axis
         * Setting the pitch rotates the actor around the x axis instead of the y axis
         * Setting the roll rotates the actor around the z axis instead of the x axis
         * So we have to correct it
         */
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    /*
     *  Set the actor's yaw
     *  @param newYaw value must be within 360 degrees
     */
    public void setYaw(float newYaw){
        yaw = newYaw;
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    /*
     *  Set the actor's pitch
     *  @param newPitch value must be within 360 degrees
     */
    public void setPitch(float newPitch){
        pitch = newPitch;
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    /*
     *  Set the actor's roll
     *  @param newRoll value must be within 360 degrees
     */
    public void setRoll(float newRoll){
        roll = newRoll;
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }


    public static float normalizeDegrees(float degrees){
        float newAngle = degrees;
        while (newAngle < -360) newAngle += 360;
        while (newAngle > 360) newAngle -= 360;
        return newAngle;
    }

    /*
     *  Rotates the actor by the amount of yaw, pitch and roll
     *  @param amountYaw,amountPitch,amountRoll These values must be within 360 degrees
     */
    public void rotate(float amountYaw, float amountPitch, float amountRoll){
        yaw = normalizeDegrees(yaw + amountYaw);
        pitch = normalizeDegrees(pitch + amountPitch);
        roll = normalizeDegrees(roll + amountRoll);
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    public void rotateYaw(float amountYaw){
        yaw = normalizeDegrees(yaw + amountYaw);
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    public void rotatePitch(float amountPitch){
        pitch = normalizeDegrees(pitch + amountPitch);
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    public void rotateRoll(float amountRoll){
        roll = normalizeDegrees(roll + amountRoll);
        rotationMatrix = transform.setFromEulerAngles(pitch, roll, yaw).cpy();
        transform.setToTranslationAndScaling(x, y, z, scaleX, scaleY, scaleZ);
        transform.mul(rotationMatrix);
    }

    public float getYaw(){
        return yaw;
    }

    public float getPitch(){
        return pitch;
    }

    public float getRoll(){
        return roll;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        transform.setToScaling(scaleX, scaleY, scaleZ);
    }

    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        this.scaleZ = scale;
        transform.setToScaling(scaleX, scaleY, scaleZ);
    }

    /** Adds the specified scale to the current scale. */
    public void scale(float scale) {
        scaleX += scale;
        scaleY += scale;
        scaleZ += scale;
        transform.scl(scale); // re-implement this
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX += scaleX;
        this.scaleY += scaleY;
        this.scaleZ += scaleZ;
        transform.scl(scaleX, scaleY, scaleZ); // re-implement this
    }


    public void setX (float x) {
        this.x = x;
        transform.setToTranslation(x, y, z);
    }

    public float getX () {
        return x;
    }

    public void setY (float y) {
        this.y = y;
        transform.setToTranslation(x, y, z);
    }

    public float getY () {
        return y;
    }

    public void setZ (float z) {
        this.z = z;
        transform.setToTranslation(x, y, z);
    }

    public float getZ (){
        return z;
    }

    public void setScaleX (float scaleX) {
        this.scaleX = scaleX;
        transform.scale(scaleX, scaleY, scaleZ);
    }

    public float getScaleX () {
        return scaleX;
    }

    public void setScaleY (float scaleY) {
        this.scaleY = scaleY;
        transform.scale(scaleX, scaleY, scaleZ);
    }

    public float getScaleY () {
        return scaleY;
    }

    public void setScaleZ (float scaleZ) {
        this.scaleY = scaleZ;
        transform.scale(scaleX, scaleY, scaleZ);
    }

    public float getScaleZ () {
        return scaleZ;
    }

    /** Sets a name for easier identification of the actor in application code.
     * @see Group#findActor(String) */
    public void setName (String name) {
        this.name = name;

    }
    public String getName () {
        return name;
    }

    /** Draws this actor's debug lines if {@link #getDebug()} is true.
     * @param modelBatch
     * @param environment*/
    public void drawDebug(ModelBatch modelBatch, Environment environment) {
        if (!debug) return;

        axis.transform.setToTranslationAndScaling(this.x, this.y, this.z, scaleX, scaleY, scaleZ);
        axis.transform.mul(rotationMatrix);

        modelBatch.render(axis, environment);
    }

    /** If true, {@link #drawDebug(ModelBatch, Environment)} will be called for this actor. */
    public void setDebug (boolean enabled, ModelBuilder modelBuilder) {
        if (debug == enabled) return;
        debug = enabled;
        if (enabled) {
            if (modelBuilder != null) {
                this.modelBuilder = modelBuilder;
            }

            Material boxMaterial = new Material();
            boxMaterial.set( ColorAttribute.createDiffuse(Color.WHITE) );
            int usageCode = VertexAttributes.Usage.Position + VertexAttributes.Usage.ColorPacked + VertexAttributes.Usage.Normal;
            Model model = this.modelBuilder.createXYZCoordinates(Math.max(1, radius), boxMaterial, usageCode );
            axis = new ModelInstance(model);
        } else {
            axis.model.dispose();
            axis = null;
        }
    }

    public void setDebug (boolean enabled) {
        setDebug(enabled, new ModelBuilder());
    }

    public boolean getDebug () {
        return debug;
    }

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public String toString () {
        String name = this.name;
        if (name == null) {
            name = getClass().getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex != -1) name = name.substring(dotIndex + 1);
        }
        return name;
    }

    public Color getColor(){
        return ((ColorAttribute)materials.get(0).get(ColorAttribute.Diffuse)).color;
    }

    public void setColor(Color color){
        materials.get(0).set(ColorAttribute.createDiffuse(color));
    }

    public Matrix4 getTransform(){
        return transform;
    }

    public void setTransform(Matrix4 transform){
        this.transform = transform;
    }

    public BoundingBox getBoundingBox(){
        return boundBox;
    }

    public void setBoundingBox(BoundingBox box){
        boundBox = box;
    }

    public AnimationController getAnimation(){
        return animation;
    }

    @Override
    public void dispose() {
        if (axis != null) axis.model.dispose();
    }
    
    public void moveBy (float x, float y)
    {
    	translate(x, y, 0f);
    }


}