package mocap.player;

import java.util.List;
import java.util.ArrayList;

import mocap.figure.AnimData;
import mocap.figure.Bone;
import mocap.j3d.Util;

public class PerlinNoise {

	private AnimData data;
	private List<Bone> bones = new ArrayList<Bone>();
	private float[][][] extremePts;
	
	public PerlinNoise(AnimData data, Bone root)
	{
		this.data = data;
		root.collectBones(bones);
		findExtremePoints();
	}
	
	public void findExtremePoints()
	{
		int numFrames = data.getNumFrames();
		extremePts = new float[bones.size()][][]; // bone -> {min,max} -> z,y,x
		
		// for each bone, find the limits of rotation
		for (int i = 0; i < bones.size(); i++)
		{
			Bone bone = bones.get(i);
			float[][] rots = Util.getEulerFromQuat(data.getBoneRotData(bone.getIndex()));
			
			float maxz = rots[0][0]; float maxy = rots[0][1]; float maxx = rots[0][2];
			float minz = rots[0][0]; float miny = rots[0][1]; float minx = rots[0][2];
			
			for (int f = 1; f < numFrames; f++)
			{
				float[] rot = rots[f];
				minz = Math.min(minz, rot[0]); maxz = Math.max(maxz, rot[0]);
				miny = Math.min(miny, rot[1]); maxy = Math.max(maxy, rot[1]);
				minx = Math.min(minx, rot[2]); maxx = Math.max(maxx, rot[2]);
			}
			
			// set the extreme points, see initialization of extremePts above
			extremePts[i] = new float[2][3];
			float[][] p = extremePts[i];
			p[0][0] = minz; p[1][0] = maxz;
			p[0][1] = miny; p[1][1] = maxy;
			p[0][2] = minx; p[1][2] = maxx;
		}
	}

    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	for (int b = 0; b < bones.size(); b++)
    	{
    		Bone bone = bones.get(b);
    		float[][] xp = extremePts[b];
    		String thisLine = String.format("%-15s {% 7.2f % 7.2f % 7.2f} {% 7.2f % 7.2f % 7.2f}",
    				bone.getName(),
    				Math.toDegrees((double)xp[0][2]), Math.toDegrees((double)xp[0][1]), 
    				Math.toDegrees((double)xp[0][0]),
    				Math.toDegrees((double)xp[1][2]), Math.toDegrees((double)xp[1][1]), 
    				Math.toDegrees((double)xp[1][0]));
    		
    		sb.append(thisLine);
    		sb.append("\n");
    	}
    	
    	return sb.toString();
    }
}
