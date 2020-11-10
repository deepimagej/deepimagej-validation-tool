package loss;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Jaccard extends AbstractLoss {

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
		return "Jaccard";
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
			int difval = (int)ipt.getMax();
			int n=0;
			double s, g, sum = 0.0, jaccard = 0.0, intersection=0.0, smooth=1.0,union=0.0,meanjacc=0.0;
			
			for(int v=0; v < difval ; v++) {
				for (int x = 0; x < nxr; x++) {
					for (int y = 0; y < nyr; y++) {
						
						s =  ipr.getPixelValue(x, y);
						g = ipt.getPixelValue(x, y);
						if( s== (v+1) && g== (v+1)) {
							if (!Double.isNaN(g))
								if (!Double.isNaN(s)) {
									intersection += 1;
									sum += 2;
									
								}
						}
						else if(s== (v+1) || g== (v+1)){
							if (!Double.isNaN(g))
								if (!Double.isNaN(s)) {
									sum += 1;
								}
						}
						
					}
				}
				union=sum-intersection;
				jaccard=(intersection + smooth)/(union + smooth);
				res.add(1-jaccard);
				meanjacc+=1-jaccard;
			}
			meanjacc/=difval;
			res.add(meanjacc);
					
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

