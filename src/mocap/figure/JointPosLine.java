package mocap.figure;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

public class JointPosLine {
	private String boneToFollow;
	private Bone skeleton;
	private AnimData data;
	
	private List<Point3d> pts = new ArrayList<Point3d>();
	
	public JointPosLine(Bone skeleton, String boneToFollow, AnimData data) {
		this.skeleton = skeleton;
		this.boneToFollow = boneToFollow;
		this.data = data;
		
		initializeLinePoints();
	}
	
	public List<Point3d> initializeLinePoints()
	{
		pts.clear();
		
		List<Bone> bones = new ArrayList<Bone>();
		skeleton.collectBones(bones); 
		
		Point3d offset = new Point3d(); 
		
		int numFrames = data.getNumFrames();
		for (int f = 0; f < numFrames; f++)
		{
			for (int b = 0; b < bones.size(); b++)
			{
				Bone bone = bones.get(b);
				int bi = bone.getIndex();
				if (bi == 0)
					bone.setPose(data.getBoneTransData()[f], data.getBoneRotData(bi)[f], offset);
				else
					bone.setPose(data.getBoneRotData(bi)[f], offset);
				
				// if this the bone we requested, add the position and move onto the next frame
				if (bone.getName().compareTo(boneToFollow) == 0)
				{
					Point3d thisPos = new Point3d();
					bone.getWorldPosition(thisPos);
					pts.add(thisPos);
					
					// double the points up 
					if (f != 0 && f != numFrames - 1)
						pts.add(thisPos);
					
					break;
				}
			}				
		}
		
		return pts;
	}
	
	public BranchGroup getLineObject()
	{
		BranchGroup lineRoot = new BranchGroup();
		lineRoot.setCapability(BranchGroup.ALLOW_DETACH);
		
		LineArray line = new LineArray(pts.size(), LineArray.COORDINATES);
		Point3d[] p = new Point3d[pts.size()];
		pts.toArray(p);
				
		line.setCoordinates(0, p);
		
		Appearance a = new Appearance();
		ColoringAttributes ca = new ColoringAttributes(new Color3f(0.7f, 0.7f, 0.7f), 
													   ColoringAttributes.SHADE_FLAT);
		LineAttributes la = new LineAttributes();
		la.setLineWidth(2f);
		la.setLineAntialiasingEnable(true);
		
		a.setColoringAttributes(ca);
		a.setLineAttributes(la);
		
		lineRoot.addChild(new Shape3D(line, a));
		lineRoot.compile();
		
		return lineRoot;
	}
}
