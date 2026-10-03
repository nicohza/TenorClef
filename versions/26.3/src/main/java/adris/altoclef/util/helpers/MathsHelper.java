package adris.altoclef.util.helpers;

import net.minecraft.util.math.Vec3d;

/**
 * Math utilities
 * <p>
 * I'm not British. I swear. I just don't want it to conflict with minecraft's `MathHelper`.
 */
public interface MathsHelper {

    /** Uniform scale; Vec3d.multiply(double) was renamed to scale() on 26.x so go through the 3-arg overload. */
    static Vec3d scale(Vec3d vec, double k) {
        return vec.multiply(k, k, k);
    }

    static Vec3d project(Vec3d vec, Vec3d onto, boolean assumeOntoNormalized) {
        if (!assumeOntoNormalized) {
            onto = onto.normalize();
        }
        return MathsHelper.scale(onto, vec.dotProduct(onto));
    }

    static Vec3d project(Vec3d vec, Vec3d onto) {
        return project(vec, onto, false);
    }

    static Vec3d projectOntoPlane(Vec3d vec, Vec3d normal, boolean assumeNormalNormalized) {
        Vec3d p = project(vec, normal, assumeNormalNormalized);
        return vec.subtract(p);
    }

    static Vec3d projectOntoPlane(Vec3d vec, Vec3d normal) {
        return projectOntoPlane(vec, normal, false);
    }
}
