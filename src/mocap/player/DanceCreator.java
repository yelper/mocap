package mocap.player;

import java.util.ArrayList;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import mocap.figure.AnimData;

public class DanceCreator {

	private ArrayList<AnimData> segments;
	
	public DanceCreator(ArrayList<AnimData> segments) {
		this.segments = segments;
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
		float[] blendWeights = {.9f, .75f, .6f, .5f, .4f, .25f, .1f};
		int numFrames = blendWeights.length;
		//Again, I assume both animations use the same bones
		int numBones = a.getNumBones();
		
		AnimData blendedData = new AnimData(numBones);
		
		// *** Start blending translational data *** //
		Vector3f[] boneTrans1 = a.getBoneTransData();
		Vector3f[] boneTrans2 = b.getBoneTransData();
		
		Vector3f[] blendedTrans = 
			new Vector3f[boneTrans1.length + boneTrans2.length - numFrames];
		
		for (int i=0; i<boneTrans1.length - numFrames; i++) {
			blendedTrans[i] = boneTrans1[i];
		}
		
		int k = 0;
		for (int i=boneTrans1.length-numFrames; i<boneTrans1.length; i++) {
			Vector3f vec1 = boneTrans1[i];
			Vector3f vec2 = boneTrans2[k];
			
			Vector3f blend = new Vector3f();
			blend.x = vec1.x * blendWeights[k] + vec2.x * 
				blendWeights[numFrames - k - 1];
			blend.y = vec1.y * blendWeights[k] + vec2.y * 
				blendWeights[numFrames - k - 1];
			blend.z = vec1.z * blendWeights[k] + vec2.z * 
				blendWeights[numFrames - k - 1];
			
			blendedTrans[i] = blend;
		}
		
		for (int i=k; i<boneTrans2.length; i++) {
			blendedTrans[i + boneTrans1.length - numFrames] = boneTrans2[i];
		}
		
		blendedData.putBoneTransData(blendedTrans);
		// *** End blending translational data *** //
		
		
		
		// *** Start blending rotational data *** //
		for (int i=0; i<numBones; i++) {
			Quat4f[] boneRot1 = a.getBoneRotData(i);
			Quat4f[] boneRot2 = b.getBoneRotData(i);
			Quat4f[] boneBlend = 
				new Quat4f[boneRot1.length + boneRot2.length - numFrames];
			
			for (int j=0; j<boneRot1.length-numFrames; j++) {
				boneBlend[j] = boneRot1[j];
			}
			
			k=0;
			for (int j=boneRot1.length-numFrames; j<boneRot1.length; j++) {
				Quat4f q1 = new Quat4f(boneRot1[j]);
				Quat4f q2 = new Quat4f(boneRot2[k]);
				
				q1.scale(blendWeights[k]);
				q2.scale(blendWeights[numFrames-k-1]);
				q1.mul(q2);
				boneBlend[j] = q1;
				k++;
			}
			
			for (int j=k; j<boneRot2.length; j++) {
				boneBlend[boneRot1.length-numFrames + j] = boneRot2[j];
			}
			
			blendedData.putBoneRotData(i, boneBlend);
		}
		// *** End blending rotational data *** //
		
		
		return blendedData;
	}
	
	private float confLevel(AnimData a, AnimData b) {
		//TODO
		
		
		return 0;
	}
	
	public AnimData getSequence(int numSegments) {
		AnimData sequence = null;
		
		int rand = (int)(Math.random() * segments.size());
		
		sequence = segments.get(rand);
		
		for(int i=1; i<numSegments; i++) {
			ArrayList<AnimData> highConf = new ArrayList<AnimData>();
			
			//TODO: good val for this threshold?
			float thresh = 0;
			
			for(int j=0; j<segments.size(); j++) {
				float conf = confLevel(sequence, segments.get(j));
				
				if (conf >= thresh) {
					highConf.add(segments.get(j));
				}
			}
			
			rand = (int)(Math.random() * highConf.size());
			
			sequence = blend(sequence, highConf.get(rand));
			
			//TODO: Add Perlin noise
		}
		
		return sequence;
	}
}
