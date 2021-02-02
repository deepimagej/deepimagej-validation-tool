package loss;

import ij.process.ImageProcessor;

public class MinMax {

	public static double getminimum(ImageProcessor img1) {
		double min_im = Double.MAX_VALUE;
		double val;
		for (int i = 0 ; i < img1.getHeight(); i++)
			for( int j = 0 ; j < img1.getWidth(); j++) {
				val = img1.getValue(j, i);
				if(val<min_im)
					min_im = val;
			}
		return min_im;
	}
	
	public static double getmaximum(ImageProcessor img1) {
		double max_im = Double.MIN_VALUE;
		double val;
		for (int i = 0 ; i < img1.getHeight(); i++)
			for( int j = 0 ; j < img1.getWidth(); j++) {
				val = img1.getValue(j, i);
				if(val>max_im)
					max_im = val;
			}
		return max_im;
	}
}
