package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Categorical_Hinge extends AbstractLoss {

	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();
		
	}
	
	@Override
	public String getName() {
		return "Categorical_Hinge";
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
			double s, g, loss, pos=0.0,neg = 0.0, rmse, nmax;
			for (int x = 0; x < nxr; x++) {
				for (int y = 0; y < nyr; y++) {
					
					s =  ipr.getPixelValue(x, y);
					g = ipt.getPixelValue(x, y);
					if (!Double.isNaN(g))
						if (!Double.isNaN(s)) {
							nmax= (1-s)*g;
							if (nmax > neg)
								neg=nmax;
							pos += g*s;
							n++;
						}
				}
			}
			pos=pos/n;
			
			loss= Math.max(neg-pos+1.0, 0.0);
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
		
		//get the max of the first stack of the images
		double max_im1 = MinMax.getmaximum(reference.getStack().getProcessor(1));
		double max_im2 = MinMax.getmaximum(test.getStack().getProcessor(1));
		
		//double la = test.getStack().getProcessor(1).getPixelValue(0, 0);
		//get the min of the first stack of the images
		double min_im1 = MinMax.getminimum(reference.getStack().getProcessor(1));    //.getMin();
		double min_im2 = MinMax.getminimum(test.getStack().getProcessor(1));// .getMin();
	
		if((max_im1 > 1.0 )||(max_im2 > 1.0)||(min_im1<0.0)||(min_im2<0.0)) {
			return "For Categorical Hinge, values must be between 0.0 and 1.0" ;
		}
		return "Valid";
	}
}
