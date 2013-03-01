package mocap.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BVHReader {

	private double[][] motValues;
	
	/**
	 * Reads a BVH file. Currently only reads the motion values part of 
	 * the file, we need to implement Bone and others for the rest. Stores
	 * the motion vales in the motValues 2-D array.
	 * @param bvhFile the file to read from
	 * @return True if the file was successfully read from. False if the file
	 *   could not be read from, was missing information, or was incorrectly
	 *   formatted in any way.
	 * @throws FileNotFoundException if the file could not be found.
	 */
	public boolean readFile(File bvhFile) throws FileNotFoundException {
		Scanner in = new Scanner(bvhFile);
		
		while (in.hasNextLine()) {
			String line = in.nextLine();
			//TODO: Stuff for the bones, the first part of the file
			
			if (line.equals("MOTION")) {
				//"Frames: XX"
				if (!in.hasNextLine()) //there should be more lines
					return false;
				line = in.nextLine();
				if (line.indexOf("Frames") < 0) //the next line should be "Frames: XX"
					return false;
				int frames = 0;
				try {
					frames = Integer.parseInt(line.substring(line.indexOf(" ")));
				} catch (NumberFormatException e) {
					return false;
				}
				
				//"Frame Time: XX"
				if (!in.hasNextLine())
					return false;
				line = in.nextLine();
				if (line.indexOf("Frame Time") < 0)
					return false;
				String[] tokens = line.split(" ");
				double frameTime = 0; //TODO: not sure what this is for but
									  //I assume we need it!
				try {
					frameTime = Double.parseDouble(tokens[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				
				//Now read in all the numbers
				if (!in.hasNextLine())
					return false;
				line = in.nextLine();
				String[] nums = line.split(" ");
				motValues = new double[frames][nums.length];
				try {
					for (int i=0; i<nums.length; i++) {
						motValues[0][i] = Double.parseDouble(nums[i]);
					}
					
					int frameCount = 1;
					while (in.hasNextLine()) {
						line = in.nextLine();
						nums = line.split(" ");
						
						for (int i=0; i<nums.length; i++) {
							motValues[frameCount][i] = Double.parseDouble(nums[i]);
						}
						
						frameCount++;
					}
					
					if (frameCount < frames) {
						//I.e., if the actual number of frames in the file
						//is less than the number it says there are
						return false;
					}
					
				} catch (NumberFormatException e) {
					return false;
				} catch (IndexOutOfBoundsException e) {
					//If there were more lines in the file than it said there were
					return false;
				}
				
			}
		}
		return true;
	}
	
	public double[][] getMotVals() {
		return motValues;
	}
}
