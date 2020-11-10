package loss;

import java.util.ArrayList;

import ij.ImagePlus;

public abstract class AbstractLoss {
	
	private boolean selected;
	
	public ArrayList<Double> run(ImagePlus reference, ImagePlus test) {
		
		ArrayList<Double> loss = compute(reference, test);
		
		return loss;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean getSelected() {
		return selected;
	}
	public abstract String getName();
	public abstract ArrayList<Double> compute(ImagePlus reference, ImagePlus test);
	public abstract String check(ImagePlus reference, ImagePlus test);
}
