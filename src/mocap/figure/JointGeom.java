package mocap.figure;

import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;

import mocap.j3d.Util;
import mocap.scene.CoordCross;

/**
 * Joint geometry consists of a geometric representation of the joint itself (a sphere, a cube).
 * 
 * @author Michael Kipp
 */
public class JointGeom
{

	public static final int NONE = -1, CROSS = 0, SPHERE_SMALL = 1,
						    SPHERE_BIG = 2;
	private static final float SIZE_CROSS = .2f;
	private static final float SIZE_SPHERE_SMALL = .01f;
	private static final float SIZE_SPHERE_BIG = .03f;
	private Switch _switch;

	public JointGeom(TransformGroup parent, float radius)
	{
		_switch = new Switch();
		_switch.setCapability(Switch.ALLOW_SWITCH_WRITE);

		// option 1: cross
		_switch.addChild(new CoordCross(SIZE_CROSS).getRoot());

		// option 2: small sphere
		Sphere s = new Sphere(SIZE_SPHERE_SMALL);
		_switch.addChild(s);

		// option 3: big sphere
		s = new Sphere(SIZE_SPHERE_BIG);
		_switch.addChild(s);

		select(SPHERE_SMALL); // initial selection
		parent.addChild(_switch);
	}

	public void select(int style)
	{
		_switch.setWhichChild(style == NONE ? Switch.CHILD_NONE : style);
	}
}
