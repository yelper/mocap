package mocap.j3d;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;

/**
 *
 * @author Michael Kipp
 */
public class Util
{

    private Util()
    {
    }

    /**
     * Computes the full transform from root to the given transform group,
     * including the transform *in* the TG.
     *
     * The resulting transform can be used to find the origin of the TG by
     * applying the transform to (0,0,0).
     *
     * @param tg The TG in question
     * @param tf Resulting transform is returned here (old transform is overwritten)
     */
    public static void getFullTransform(TransformGroup tg, Transform3D tf)
    {
        Transform3D tf2 = new Transform3D();
        tf.setIdentity();
        tg.getTransform(tf2);
        tg.getLocalToVworld(tf);
        tf.mul(tf2);
    }

    /**
     * Computes the transform that takes a point that is *local* in locationTG
     * to the frame of reference of frameTG.
     *
     * If one applies the resulting transform to (0,0,0) one obtains the location
     * of the origin of locationTG in the frame of reference of frameTG.
     *
     * @param locationTG The TG in question, must be (grand)child of frameTG
     * @param frameTG The new frame of reference, must be parent of locationTG
     * @param tf Resulting transform is returned here (old transform is overwritten)
     */
    public static void getRelativeTransform(TransformGroup locationTG, TransformGroup frameTG, Transform3D tf)
    {
        Transform3D tf2 = new Transform3D();
        getFullTransform(locationTG, tf);
        getFullTransform(frameTG, tf2);
        tf2.invert();
        tf.mul(tf2, tf);
    }
    
    
    /**
     * Do some quaternion computation from Euler angles (from the animation data)
     * 
     * Expects the Euler angles in z,y,x order
     */
    
    public static Quat4f getQuatFromEulerAngles(float[] eulerAngles)
    {
    	// create the axis-angle representations
    	AxisAngle4f z = new AxisAngle4f(0, 0, 1, eulerAngles[0]);
    	AxisAngle4f y = new AxisAngle4f(0, 1, 0, eulerAngles[1]);
    	AxisAngle4f x = new AxisAngle4f(1, 0, 0, eulerAngles[2]);
    	
    	// set each quaternion with their one axis-rotation 
    	Quat4f qz = new Quat4f();
    	qz.set(z);
    	Quat4f qy = new Quat4f();
    	qy.set(y);
    	Quat4f qx = new Quat4f();
    	qx.set(x);
    	
    	// compose them together into qz to make an overall quaternion
    	qz.mul(qy);
    	qz.mul(qx);
    	
    	return qz;
    }
}
