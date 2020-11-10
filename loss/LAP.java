package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class LAP extends AbstractLoss {

	/*
	 * This is a series unit test for the Bce function
	 */
	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();
		
		ArrayList<Double> result = new Bce().run(ref, test);
		System.out.println("Series of unit test");
		System.out.println("" + result);
	}
	
	@Override
	public String getName() {
		return "LAP";
	}
	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test) {
		
		int nxr = reference.getWidth();
		int nyr = reference.getHeight();
		double sigma = 0.5;
		
			
		ArrayList<Double> res = new ArrayList<Double>(); 	
		
	
		int nzr = reference.getStack().getSize();
		int nzt = test.getStack().getSize();
		double s, g,s_l,g_l,sum=0.0;
		
		for (int z=1; z<=Math.max(nzr, nzt); z++) {
			int ir = Math.min(z, nzr);
			int it = Math.min(z, nzt);
			ImageProcessor ipt = test.getStack().getProcessor(it);
			ImageProcessor ipr = reference.getStack().getProcessor(ir);
			
			sum=0.0;
			//int n=0;
			for(int l=1; l<4 ;l++){
				ImageProcessor ipt_l = test.getStack().getProcessor(it);
				ipt_l.blurGaussian(sigma*l);
				ImageProcessor ipr_l = reference.getStack().getProcessor(it);
				ipr_l.blurGaussian(sigma*l);
				for (int x = 0; x < nxr; x++) {
					for (int y = 0; y < nyr; y++) {
						
						s =  ipr.getPixelValue(x, y);
						g = ipt.getPixelValue(x, y);
						s_l =  ipr_l.getPixelValue(x, y);
						g_l = ipt_l.getPixelValue(x, y);
						if (!Double.isNaN(g))
							if (!Double.isNaN(s)) {
								sum+=Math.abs((g-g_l)-(s-s_l));
							}
					}
				}
			}
			res.add(sum);	
		}
		
		
		return res ;
	}

	@Override
	public String check(ImagePlus reference, ImagePlus test) {
		
		if (reference == null)
			return "null image";
		if (test == null)
			return "null image";
		
		return "";
	}
}


