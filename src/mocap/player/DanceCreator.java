package mocap.player;

import java.util.ArrayList;

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
		//Again, I assume both animations use the same bones
		int numBones = a.getNumBones();
		
		AnimData blendedData = new AnimData(numBones);
		
		for (int i=0; i<numBones; i++) {
			float[] boneData1 = a.getBoneData(i);
			float[] boneData2 = b.getBoneData(i);
			System.out.println(boneData1 + ", " + boneData2);
			
			int numFrames = blendWeights.length;
			float[] boneBlend = 
				new float[boneData1.length + boneData2.length - numFrames];
			
			for (int j=0; j<boneData1.length-numFrames; j++) {
				boneBlend[j] = boneData1[j];
			}
			
			int k=0;
			for (int j=boneData1.length-numFrames; j<boneData1.length; j++) {
				boneBlend[j] = boneData1[j] * blendWeights[k] +
					boneData2[k] * blendWeights[numFrames - k - 1];
				k++;
			}
			
			for (int j=k; j<boneData2.length; j++) {
				boneBlend[boneData1.length-numFrames] = boneData2[j];
			}
			
			blendedData.putBoneData(i, boneBlend);
		}
		
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
