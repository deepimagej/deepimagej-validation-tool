package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class NormL2 extends AbstractLoss {

	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();

	}
	
	@Override
	public String getName() {
		return "NormL2";
	}
	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test,Setting setting) {
		
		int nxr = reference.getWidth();
		int nyr = reference.getHeight();
			
		ArrayList<Double> res = new ArrayList<Double>(); 	
		
	
		int nzr = reference.getStack().getSize();
		int nzt = test.getStack().getSize();
		
		for (int z=1; z<=Math.max(nzr, nzt); z++) {
			int ir = Math.min(z, nzr);
			int it = Math.min(z, nzt);
			ImageProcessor ipt = test.getStack().getProcessor(it);
			ImageProcessor ipr = reference.getStack().getProcessor(ir);
			int n=0;
			double s, g, sum=0.0;
			for (int x = 0; x < nxr; x++) {
				for (int y = 0; y < nyr; y++) {
					
					s =  ipr.getPixelValue(x, y);
					g = ipt.getPixelValue(x, y);
					if (!Double.isNaN(g))
						if (!Double.isNaN(s)) {
							sum+=(g-s)*(g-s);
						}
				}
			}
			sum=Math.sqrt(sum);
			res.add(sum);
					
		}
		
		
		return res ;
	}
	
	public double Norml2_Stack(ImageProcessor im1, ImageProcessor im2) {
		double s,g, sum=0.0;
		int nxr = im1.getWidth();
		int nyr = im1.getHeight();
		for (int x = 0; x < nxr; x++) {
			for (int y = 0; y < nyr; y++) {
				
				s =  im1.getPixelValue(x, y);
				g = im2.getPixelValue(x, y);
				if (!Double.isNaN(g))
					if (!Double.isNaN(s)) {
						sum+=Math.pow(g-s,2);
					}
			}
		}
		sum=Math.sqrt(sum);
		return sum;
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

