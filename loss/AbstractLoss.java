package loss;

import java.util.ArrayList;

import ij.ImagePlus;

public abstract class AbstractLoss {
	
	private boolean selected;
	
	public ArrayList<Double> run(ImagePlus reference, ImagePlus test, Setting setting) {
		
		ArrayList<Double> loss = compute(reference, test,setting);
		
		return loss;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean getSelected() {
		return selected;
	}
	
	
	public abstract String getName();
	public abstract Boolean getSegmented();
	public abstract ArrayList<Double> compute(ImagePlus reference, ImagePlus test,Setting setting);
	public abstract String check(ImagePlus reference, ImagePlus test,Setting setting);
	public abstract ArrayList<Double> compose(ArrayList<Double> loss1, double w_1,ArrayList<Double> loss2, double w_2);
}
