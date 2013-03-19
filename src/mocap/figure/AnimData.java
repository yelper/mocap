package mocap.figure;

import mocap.j3d.Util;
import mocap.player.MocapPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Stores mocap animation data.
 * 
 * @author Michael Kipp
 */
public class AnimData
{
	private ArrayList<Quat4f[][]> rotations;
	private ArrayList<Vector3f[]> translations;	

    private ArrayList<float[][]> data; // first index: bones, second index: frames
    private float blendedData[][]; //the blended data, same indicies.
    	//note this implementation assumes the same number of bones
    private int _numFrames, _numBones;
    private float _fps = MocapPlayer.DEFAULT_FPS;

    public AnimData(int numBones)
    {
        blendedData = new float[numBones][];
        data = new ArrayList<float[][]>();
        
        rotations = new ArrayList<Quat4f[][]>();
        translations = new ArrayList<Vector3f[]>();        
        
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
    
    public void putBoneRotData(int animIndex, int index, float[] bonedata)
    {
    	// return if animIndex higher than curSize (why?)
    	if (animIndex > rotations.size())
    		return;
    	
    	// add this animation's data if it hasn't been made yet
    	if (animIndex == rotations.size())
    		rotations.add(new Quat4f[_numBones][]);
    	
    	Quat4f[][] animRot = rotations.get(animIndex);
    	animRot[index] = new Quat4f[_numFrames];
    	Quat4f[] thisRot = animRot[index];
    	
    	for (int i = 0; i < _numFrames; i++)
    	{
    		float[] angles = Arrays.copyOfRange(bonedata, 3*i, 3*i+3);
    		Quat4f rot = Util.getQuatFromEulerAngles(angles);
    		thisRot[i] = rot;
    	}
    	
    	rotations.set(animIndex, animRot);
    }
    
    public void putBoneTransData(int animIndex, float[] bonedata)
    {
    	// return if animIndex higher than the current size (why?)
    	if (animIndex > translations.size())
    		return;
    	
    	// add this animation's data if it hasn't been loaded yet
    	if (animIndex == translations.size())
    	{
    		Vector3f[] thistrans = new Vector3f[_numFrames]; 
    		for (int i = 0; i < _numFrames; i++)
	    	{
	    		float[] dir = Arrays.copyOfRange(bonedata, 3*i, 3*i+3);
	    		thistrans[i] = new Vector3f(dir);
	    	}
    		
    		translations.add(thistrans);
    	}
    }
    
    public Quat4f[] getBoneRotData(int animIndex, int boneindex)
    {
    	return rotations.get(animIndex)[boneindex];
    }
    
    public Vector3f[] getBoneTransData(int animIndex, int boneindex)
    {
    	return translations.get(animIndex);
    }
    
    public Transform3D getBoneXformData(int animIndex, int boneIndex, int frame)
    {
    	Vector3f thisTrans = translations.get(animIndex)[frame];
    	Quat4f   thisQuat  = rotations.get(animIndex)[boneIndex][frame];
    	return new Transform3D(thisQuat, thisTrans, 1f);
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
