package ardash.gdx.scenes.scene3d.shape;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import ardash.gdx.scenes.scene3d.Actor3D;
import ardash.gdx.scenes.scene3d.utils.AdvModelBuilder;

/**
 * Created by Andreas Redmer on 11/04/2020.
 */

public class Image3D extends Actor3D {

    public Image3D(float width, float height) {
        this(width, height, Color.WHITE);
    }

    public Image3D(float width, float height, Color color) {
        this(width, height, color, new ModelBuilder());
    }

    public Image3D(float width, float height, ModelBuilder modelBuilder) {
        this(width, height, Color.WHITE, modelBuilder);
    }

    public Image3D(float width, float height, Color color, ModelBuilder modelBuilder) {
        super(createModel(width, height, color, (Texture)null, modelBuilder));
    }

    public Image3D(float width, float height, Texture texture, ModelBuilder modelBuilder) {
        super(createModel(width, height, null, texture, modelBuilder));
    }

    public Image3D(float width, float height, Color color, Texture texture, ModelBuilder modelBuilder) {
        super(createModel(width, height, color, texture, modelBuilder));
    }

    /**
     * Used by normal actors with 2d sprites from texture atlas
     */
    public Image3D(float width, float height, TextureRegion textureRegion, ModelBuilder modelBuilder) {
        super(createModel(width, height, null, textureRegion, modelBuilder, 0f));
    }

    public Image3D(float width, float height, TextureRegion textureRegion, ModelBuilder modelBuilder, float shear) {
        super(createModel(width, height, null, textureRegion, modelBuilder, shear));
    }
    
    /**
     * x -> w
     * y -> h
     * 00 - 10
     * |     |
     * 01 - 11
     */
    private static Model createModel(float width, float height, Color color, Texture texture, ModelBuilder modelBuilder) {
        Material material = new Material();
        if (color != null) material.set( ColorAttribute.createDiffuse(color) );
        if (texture != null) material.set( TextureAttribute.createDiffuse(texture) );

        int usageCode = VertexAttributes.Usage.Position + VertexAttributes.Usage.ColorPacked + VertexAttributes.Usage.Normal + VertexAttributes.Usage.TextureCoordinates;

        return modelBuilder.createRect(0, 0, 0, width, 0, 0, width, height, 0, 0, height, 0, 0, 0, 1, material, usageCode) ;
    }

    private static Model createModel(float width, float height, Color color, TextureRegion textureRegion, ModelBuilder modelBuilder, float shear) {
        Material material = new Material();
        if (color != null) material.set( ColorAttribute.createDiffuse(color) );
        if (textureRegion != null) material.set( TextureAttribute.createDiffuse(textureRegion) );
        
//        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f)); // TODO do at render time not creation time

        int usageCode = VertexAttributes.Usage.Position + VertexAttributes.Usage.ColorPacked + VertexAttributes.Usage.Normal + VertexAttributes.Usage.TextureCoordinates;

        return modelBuilder.createRect(0, 0, 0, width, 0+shear, 0, width, height+shear, 0, 0, height, 0, 0, 0, 1, material, usageCode) ;
    
    }
}
