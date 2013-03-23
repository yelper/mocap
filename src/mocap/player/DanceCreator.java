package mocap.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import mocap.figure.AnimData;
import mocap.figure.Bone;

public class DanceCreator {

	private ArrayList<AnimData> segments;
	private Bone skeleton;
	
	public DanceCreator(ArrayList<AnimData> segments, Bone skel) {
		this.segments = segments;
		skeleton = skel;
	}
	
	/**
	 * Creates a new AnimData that has first a, then b, with the end of a 
	 * blended with the beginning of b.
	 * @param a
	 * @param b
	 * @return
	 */
	private AnimData blend(AnimData a, AnimData b) {
		//TODO: Change this to Gaussian
		int overlap = 50;
		float[] blendWeights = new float[overlap];
		for (int i=overlap; i>0; i--) {
			blendWeights[overlap - i] = ((float)i) / overlap; 
		}
		
		//Again, I assume both animations use the same bones
		int numBones = a.getNumBones();
		
		AnimData blendedData = new AnimData(numBones);
		
		// *** Start blending translational data *** //
		Vector3f[] boneTrans1 = a.getBoneTransData();
		//System.out.println(b);
		Vector3f[] boneTrans2 = b.getBoneTransData();
		
		int numFrames = boneTrans1.length + boneTrans2.length - overlap;
		
		blendedData.setNumFrames(numFrames);
		
		Vector3f[] blendedTrans = new Vector3f[numFrames];
		
		Vector3f end = new Vector3f(boneTrans1[boneTrans1.length-1]);
		Vector3f start = boneTrans2[0];
		
		end.sub(start);
		Vector3f shift = new Vector3f(end);
		
		for (int i=0; i<boneTrans1.length; i++) {
			blendedTrans[i] = boneTrans1[i];
		}
		
		int k = 0;
		for (int i=boneTrans1.length-overlap; i<boneTrans1.length; i++) {
			Vector3f vec1 = boneTrans1[i];
			Vector3f vec2 = new Vector3f(boneTrans2[k]);
			
			vec2.add(shift);
			
			Vector3f blend = new Vector3f();
			blend.x = vec1.x * blendWeights[k] + vec2.x * 
				blendWeights[overlap - k - 1];
			blend.y = vec1.y * blendWeights[k] + vec2.y * 
				blendWeights[overlap - k - 1];
			blend.z = vec1.z * blendWeights[k] + vec2.z * 
				blendWeights[overlap - k - 1];
			
			blendedTrans[i] = blend;
			k++;
		}
		
		for (int i=k; i<boneTrans2.length; i++) {
			Vector3f curr = new Vector3f(boneTrans2[i]);
			curr.add(shift);
			blendedTrans[i + boneTrans1.length - overlap] = curr;
		}
		
		blendedData.putBoneTransData(blendedTrans);
		// *** End blending translational data *** //
		
		
		
		// *** Start blending rotational data *** //
		for (int i=0; i<numBones; i++) {
			Quat4f[] boneRot1 = a.getBoneRotData(i);
			Quat4f[] boneRot2 = b.getBoneRotData(i);
			Quat4f[] boneBlend = 
				new Quat4f[numFrames];
			
			for (int j=0; j<boneRot1.length-overlap; j++) {
				boneBlend[j] = boneRot1[j];
			}
			
			k=0;
			for (int j=boneRot1.length-overlap; j<boneRot1.length; j++) {
				Quat4f q1 = new Quat4f(boneRot1[j]);
				Quat4f q2 = new Quat4f(boneRot2[k]);
				
				q1.interpolate(q2, 1-blendWeights[k]);
				boneBlend[j] = q1;
				k++;
			}
			
			for (int j=k; j<boneRot2.length; j++) {
				boneBlend[boneRot1.length-overlap + j] = boneRot2[j];
			}
			
			blendedData.putBoneRotData(i, boneBlend);
		}
		// *** End blending rotational data *** //
		
		
		return blendedData;
	}
	
	private float confLevel(AnimData a, AnimData b) {
		float conf = 0f;
		
		List<Bone> bones = new ArrayList<Bone>();
		skeleton.collectBones(bones); 
		Point3d offset = new Point3d(); 
		
		for (int i = 0; i < bones.size(); i++)
		{
			Bone bone = bones.get(i);
			int bi = bone.getIndex();
			if (bi == 0)
				bone.setPose(
						a.getBoneTransData()[a.getNumFrames() - 1], 
						a.getBoneRotData(bi)[a.getNumFrames() - 1], offset);
			else
				bone.setPose(a.getBoneRotData(bi)[a.getNumFrames() - 1], 
						offset);
			
			Point3d pos1 = new Point3d();
			bone.getWorldPosition(pos1);
			
			
			if (bi == 0)
				//Use the last frame as the position for the root, because
				//we're going to shift it over before blending anyway
				bone.setPose(a.getBoneTransData()[a.getNumFrames()-1], 
						b.getBoneRotData(bi)[0], offset);
			else
				bone.setPose(b.getBoneRotData(bi)[0], offset);
			
			Point3d pos2 = new Point3d();
			bone.getWorldPosition(pos2);
			
			System.out.println("\n\nBone Data");
			System.out.println("First rot: " + a.getBoneRotData(bi)[a.getNumFrames() - 1]);
			System.out.println("Second rot: " + b.getBoneRotData(bi)[0]);
			System.out.println("P1: " + pos1);
			System.out.println("P2: " + pos2);
			
			double dist = pos2.distance(pos1);
			//System.out.println(dist);
			
			conf += dist;
		}
		
		//Now add in the difference between the base rotation of the root
		Quat4f rot1 = new Quat4f(a.getBoneRotData(0)[a.getNumFrames() - 1]);
		Quat4f rot2 = b.getBoneRotData(0)[0];
		
		float innerProd = (rot1.x * rot2.x + rot1.y * rot2.y + 
				rot2.z * rot2.z + rot1.w * rot2.w);
		
		//The angle between the two quaternions
		double theta = Math.acos((2 * innerProd * innerProd) - 1);
		//System.out.print(conf + ", " + theta + ", ");
		if (!Double.isNaN(theta))conf += theta;
		else conf += 1000;
		// an arbitrarily high value because if theta is nan, the rotations are
		//drastically different
		
		//System.out.println(1/conf);
		return 1/conf;
	}
	
	public AnimData getSequence(int numSegments) {
		AnimData sequence = null;
		
		int rand = (int)(Math.random() * segments.size());
		
		sequence = segments.get(rand);
		
		for(int i=1; i<numSegments; i++) {
			float thresh = 2f;
			Map<Float, AnimData> highConf = getSegsOverThresh(sequence, thresh);
			
			while (highConf.size() == 0) {
				thresh -= .2f;
				highConf = getSegsOverThresh(sequence, thresh);
			}
			
			ArrayList<AnimData> chosen = new ArrayList<AnimData>();
			
			if (highConf.keySet().size() < 3) {
				highConf = getTopThree(sequence);
			}
			
			for (Float f : highConf.keySet()) {
				chosen.add(highConf.get(f));
			}
			
			rand = (int)(Math.random() * chosen.size());
			
			sequence = blend(sequence, chosen.get(rand));
			
			//TODO: Add Perlin noise
		}
		
		return sequence;
	}

	private Map<Float, AnimData> getTopThree(AnimData sequence) {
		Map<Float, AnimData> allConf = getSegsOverThresh(sequence, 0);
		
		float max = 0;
		float max2 = 0;
		float max3 = 0;
		
		//There has to be a better way to do this.
		for(Float f : allConf.keySet()) {
			if (f > max3) {
				if (f > max2) {
					if (f > max) {
						max3 = max2;
						max2 = max;
						max = f;
					}
					else {
						max3 = max2;
						max2 = f;
					}
				}
				else {
					max3 = f;
				}
			}
		}
		
		Map<Float, AnimData> top3 = new HashMap<Float, AnimData>();
		top3.put(max, allConf.get(max));
		top3.put(max2, allConf.get(max2));
		top3.put(max3, allConf.get(max3));
		return top3;
	}

	private Map<Float, AnimData> getSegsOverThresh(
			AnimData sequence, float thresh) {
		Map<Float, AnimData> highConf = new HashMap<Float, AnimData>();
		
		for(int j=0; j<segments.size(); j++) {
			float conf = confLevel(sequence, segments.get(j));

			if (conf >= thresh) {
				highConf.put(conf, segments.get(j));
			}
		}
		
		return highConf;
	}
}
