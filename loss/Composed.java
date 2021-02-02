package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Composed extends AbstractLoss {

	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();
		
	}
	
	@Override
	public String getName() {
		return "Composed Function";
	}
	@Override
	public ArrayList<Double> compose(ArrayList<Double> loss1, double w_1,ArrayList<Double> loss2, double w_2) {
		ArrayList<Double> composed = new ArrayList<Double>();
		for (int i=0; i < loss1.size() ; i++ ) {
            composed.add(w_1*loss1.get(i)+w_2*loss2.get(i));
        }
		return composed;
	}

	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test, Setting setting) {
		return null;
	}
	
	@Override
	public Boolean getSegmented() {
		return false;
	}

	@Override
	public String check(ImagePlus reference, ImagePlus test, Setting setting) {
		return "Valid";
	}
}
