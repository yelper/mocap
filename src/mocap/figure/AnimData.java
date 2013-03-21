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

    private float[][] data; // first index: bones, second index: frames
    private int _numFrames, _numBones;
    private float _fps = MocapPlayer.DEFAULT_FPS;

    public AnimData(int numBones)
    {
        data = new float[numBones][];
        
        rotations = new ArrayList<Quat4f[][]>();
        translations = new ArrayList<Vector3f[]>();        
        
        _numBones = numBones;
    }

    public void putBoneData(int boneIndex, List<Float> animdata)
    {
    	float[][] currAnimData = data;
        currAnimData[boneIndex] = new float[animdata.size()];
        int j = 0;
        for (Float x : animdata) {
        	currAnimData[boneIndex][j++] = x;
        }
    }

    public void putBoneData(int boneIndex, float[] animdata)
    {
        data[boneIndex] = animdata;
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

    public float[] getBoneData(int boneIndex)
    {
        return data[boneIndex];
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
    
    public int getNumBones() {
    	return _numBones;
    }
    
    public void setRotations(Quat4f[][] rots)
    {
    	rotations.clear();
    	rotations.add(rots);
    }
    
    public void setTranslations(Vector3f[] trans)
    {
    	translations.clear();
    	translations.add(trans);
    }

    @Override
    public String toString()
    {
        return "<AnimData frames:" + _numFrames + ">";
    }

	public AnimData subCopy(int startFrame, int endFrame) {
		AnimData ret = new AnimData(_numBones);
		ret.setFps(_fps);
		ret.setNumFrames(endFrame - startFrame);
		
		// chop up each bone's animdata to the specified window
		Quat4f[][] thisRot = rotations.get(0);
		Quat4f[][] retRot = new Quat4f[_numBones][];
		for (int i = 0; i < thisRot.length; i++)
		{
			retRot[i] = Arrays.copyOfRange(thisRot[i], startFrame, endFrame);
		}
		ret.setRotations(retRot);
		
		Vector3f[] subtrans = Arrays.copyOfRange(translations.get(0), startFrame, endFrame);
		ret.setTranslations(subtrans);		
		
		return ret;
	}
}
