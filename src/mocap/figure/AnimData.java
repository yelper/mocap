package mocap.figure;

import mocap.j3d.Util;
import mocap.player.MocapPlayer;

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
	private Quat4f[][] rotations;
	private Vector3f[] translations;
    private int _numFrames, _numBones;
    private float _fps = MocapPlayer.DEFAULT_FPS;

    public AnimData(int numBones)
    {
        rotations = new Quat4f[numBones][]; 
        
        _numBones = numBones;
    }
    
    public void putBoneRotData(int index, float[] bonedata)
    {
    	rotations[index] = new Quat4f[_numFrames];
    	Quat4f[] thisRot = rotations[index];
    	
    	for (int i = 0; i < _numFrames; i++)
    	{
    		float[] angles = Arrays.copyOfRange(bonedata, 3*i, 3*i+3);
    		Quat4f rot = Util.getQuatFromEulerAngles(angles);
    		thisRot[i] = rot;
    	}
    }
    
    public void putBoneRotData(int index, Quat4f[] bonedata)
    {
    	rotations[index] = bonedata;
    }
    
    public void putBoneTransData(float[] bonedata)
    {
    	translations = new Vector3f[_numFrames]; 
		for (int i = 0; i < _numFrames; i++)
    	{
    		float[] dir = Arrays.copyOfRange(bonedata, 3*i, 3*i+3);
    		translations[i] = new Vector3f(dir);
    	}
    }
    
    public void putBoneTransData(Vector3f[] bonedata) {
    	translations = bonedata;
    }
    
    public Quat4f[] getBoneRotData(int boneindex)
    {
    	return rotations[boneindex];
    }
    
    public Vector3f[] getBoneTransData()
    {
    	return translations;
    }
    
    public Transform3D getBoneXformData(int boneIndex, int frame)
    {
    	Vector3f thisTrans = translations[frame];
    	Quat4f   thisQuat  = rotations[boneIndex][frame];
    	return new Transform3D(thisQuat, thisTrans, 1f);
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
    	rotations = rots;
    }
    
    public void setTranslations(Vector3f[] trans)
    {
    	translations = trans;
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
		Quat4f[][] thisRot = rotations;
		Quat4f[][] retRot = new Quat4f[_numBones][];
		for (int i = 0; i < thisRot.length; i++)
		{
			retRot[i] = Arrays.copyOfRange(thisRot[i], startFrame, endFrame);
		}
		ret.setRotations(retRot);
		
		Vector3f[] subtrans = Arrays.copyOfRange(translations, startFrame, endFrame);
		ret.setTranslations(subtrans);		
		
		return ret;
	}
}
