package mocap.reader;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import mocap.figure.AnimData;
import mocap.figure.Bone;

class AnimSegment {
	public int startFrame;
	public int endFrame;
	public String animFile;
	
	public AnimSegment(String file, int start, int end)
	{
		animFile = file;
		startFrame = start;
		endFrame = end;
	}
	
	public AnimSegment(String[] args)
	{
		animFile = args[0];
		startFrame = Integer.parseInt(args[1]);
		endFrame = Integer.parseInt(args[2]);
	}
}

public class ConfigReader {
	public ArrayList<AnimData> animData = new ArrayList<AnimData>();
	public Bone skeleton;
	
	private HashSet<String> files = new HashSet<String>();
	private ArrayList<AnimSegment> segments = new ArrayList<AnimSegment>();
	private Map<String, Boolean> loadedFile = new HashMap<String, Boolean>();
	
	private Map<String, AnimData> fileData = new HashMap<String, AnimData>();
	
	public boolean readFile(File cfgFile, String workingDir, float targetHeight) 
			throws FileNotFoundException
	{
		Scanner in = new Scanner(cfgFile);
		
		while (in.hasNextLine())
		{
			String line = in.nextLine();
			
			// strip out any comments
			line = line.split("#")[0];

			// break the line up into its parts
			String[] parts = line.split("\\s+");
			
			// add file to list of things to load, if valid line
			if (parts.length >= 3)
			{
				loadedFile.put(parts[0], false);
				segments.add(new AnimSegment(parts));
			}
		}
		
		// quit if nothing to load
		if (segments.size() == 0)
			return false;
		
		// iterate through the segments
		for (AnimSegment segment : segments)
		{
			if (!loadedFile.get(segment.animFile))
			{
				BVHReader r = new BVHReader();
				File animFile = new File(workingDir, segment.animFile);
				if (r.readFile(animFile, targetHeight))
				{
					// if successful, mark file as 'read' and stash its AnimData
					fileData.put(segment.animFile, r.data);
					loadedFile.put(segment.animFile, true);
					
					// set a skeleton if we don't have one already 
					// (assuming same skeleton for all segments)
					if (skeleton == null)
						skeleton = r.skeleton;
				}
			}
			
			// pull out the relevant segments
			AnimData fileAD = fileData.get(segment.animFile);
			animData.add(fileAD.subCopy(segment.startFrame, segment.endFrame));			
		}
		
		return true;
	}
	
}
