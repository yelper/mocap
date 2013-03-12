package mocap.figure;

import mocap.player.MocapPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores mocap animation data.
 * 
 * @author Michael Kipp
 */
public class AnimData
{

    private ArrayList<float[][]> data; // first index: bones, second index: frames
    private float blendedData[][]; //the blended data, same indicies.
    	//note this implementation assumes the same number of bones
    private int _numFrames, _numBones;
    private float _fps = MocapPlayer.DEFAULT_FPS;

    public AnimData(int numBones)
    {
        blendedData = new float[numBones][];
        data = new ArrayList<float[][]>();
        _numBones = numBones;
    }

    public void putBoneData(int animIndex, int frameIndex, List<Float> animdata)
    {
    	if (animIndex > data.size()) {//not sure what to do here, if they specify
    								  //an animation that doesn't exist
    		return;
		}
    	else if (animIndex == data.size()) { //if they specify one more than is
    		//already there, then just make a new one
    		data.add(new float[_numBones][]);
    	} //otherwise, they're updating an old one
    	
    	float[][] currAnimData = data.get(animIndex);
        currAnimData[frameIndex] = new float[animdata.size()];
        int j = 0;
        for (Float x : animdata) {
        	currAnimData[frameIndex][j++] = x;
        }
        
        if(data.size()==1) //if they've just added the first animation
        	blendedData[frameIndex] = new float[animdata.size()];
        
        for (int i = 0; i < animdata.size(); i++) {
        	//sum up the data for this bone at this frame, across all the animations
        	float sum = 0;
        	for (float[][] anim : data) {
        		sum += anim[frameIndex][i];
        	}
        	
        	//the blended data is the sum divided by how many animations there are
        	blendedData[frameIndex][i] = sum / data.size();
        }
    }

    public void putBoneData(int animIndex, int frameIndex, float[] animdata)
    {
    	if (animIndex > data.size()) {//not sure what to do here, if they specify
			  							//an animation that doesn't exist
    		return;
		}
		else if (animIndex == data.size()) { //if they specify one more than is
						//already there, then just make a new one
			data.add(new float[_numBones][]);
		} //otherwise, they're updating an old one
    	
    	/*if (animIndex > 0 && frameIndex > data.get(0)[0].length) {
    		//if they are trying to add more frames... not sure, do we just extend it?
    		//right now it automatically stops at the number of frames of the first
    		//animation that was added
    		return;
    	}*/
    	
        data.get(animIndex)[frameIndex] = animdata;
        
        if(data.size()==1) //if they've just added the first animation
        	blendedData[frameIndex] = new float[animdata.length];
        
        for (int i = 0; i < blendedData[frameIndex].length; i++) {
        	//sum up the data for this bone at this frame, across all the animations
        	float sum = 0;
        	int divisor = 0;
        	for (float[][] anim : data) {
        		if (anim[frameIndex].length > i) {
        			sum += anim[frameIndex][i];
        			divisor++;
        		}
        	}
        	
        	//the blended data is the sum divided by how many animations there are
        	blendedData[frameIndex][i] = sum / divisor;
        }
    }

    public float[] getBoneData(int animIndex, int frameIndex)
    {
        return data.get(animIndex)[frameIndex];
    }
    
    public float[] getBlendedBoneData(int frameIndex) {
    	return blendedData[frameIndex];
    }

    public void setNumFrames(int n)
    {
        _numFrames = n;
    }

    public int getNumFrames()
    {
        return _numFrames;
    }

    public void setFps(float fps)
    {
        _fps = fps;
    }

    public float getFps()
    {
        return _fps;
    }
    
    public int getNumAnims() {
    	return data.size();
    }

    @Override
    public String toString()
    {
        return "<AnimData frames:" + _numFrames + ">";
    }
}
