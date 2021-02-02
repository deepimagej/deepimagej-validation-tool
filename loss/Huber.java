package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Huber extends AbstractLoss {


	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();
		
	}
	
	@Override
	public String getName() {
		return "Huber";
	}
	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test,Setting setting) {
		
		int nxr = reference.getWidth();
		int nyr = reference.getHeight();
			
		ArrayList<Double> res = new ArrayList<Double>(); 	
		
	
		int nzr = reference.getStack().getSize();
		int nzt = test.getStack().getSize();
		double delta = 1.0;
		
		for (int z=1; z<=Math.max(nzr, nzt); z++) {
			int ir = Math.min(z, nzr);
			int it = Math.min(z, nzt);
			ImageProcessor ipt = test.getStack().getProcessor(it);
			ImageProcessor ipr = reference.getStack().getProcessor(ir);
			int n=0;
			double s, g, error=0.0, loss= 0.0;
			for (int x = 0; x < nxr; x++) {
				for (int y = 0; y < nyr; y++) {
					
					s =  ipr.getPixelValue(x, y);
					g = ipt.getPixelValue(x, y);
					if (!Double.isNaN(g))
						if (!Double.isNaN(s)) {
							error = s-g;
							if( Math.abs(error) > delta) {
								loss+=0.5*Math.pow(delta, 2) + delta*(Math.abs(error) - delta);
							}
							else
								loss+=0.5*Math.pow(error, 2);
							n++;
						}
				}
			}
			res.add(loss);
					
		}
		
		
		return res ;
	}


	@Override
	public ArrayList<Double> compose(ArrayList<Double> loss1, double w_1, ArrayList<Double> loss2, double w_2) {
		
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

