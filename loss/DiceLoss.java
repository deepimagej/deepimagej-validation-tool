package loss;

import java.text.DecimalFormat;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class DiceLoss extends AbstractLoss {

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
		return "DiceLoss";
	}
	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test) {
		
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
			double s, g, sumr = 0.0, sumt = 0.0, dice = 0.0, intersection=0.0, smooth=1.0;
			for (int x = 0; x < nxr; x++) {
				for (int y = 0; y < nyr; y++) {
					
					s =  ipr.getPixelValue(x, y);
					g = ipt.getPixelValue(x, y);
					if (!Double.isNaN(g))
						if (!Double.isNaN(s)) {
							g=1/(1+Math.exp(-g/255.0));
							s=s/255;
							intersection += g*s;
							sumr +=s*s;
							sumt +=g*g;
						}
				}
			}
			dice=(2*intersection + smooth)/(sumt + sumr + smooth);
			res.add(1-dice);
					
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
