import java.awt.Color;


import java.awt.Font;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import loss.AbstractLoss;
import loss.Bce;
import loss.DiceLoss;
import loss.Jaccard;
import loss.LAP;
import loss.MAE;
import loss.NormL1;
import loss.NormL2;
import loss.PSNR;
import loss.RMSE;
import loss.RegressSNR;
import loss.SNR;
import loss.SSIM;
import loss.Setting;

public class Settings implements PlugInFilter {
	
	private GenericDialog gd ;
	
	public Setting run(String arg) {
		
		//import function that can be used for composed function
		ArrayList<AbstractLoss> functions = new ArrayList<AbstractLoss>();
		functions.add(new NormL1());
		functions.add(new NormL2());
		functions.add(new Bce());
		functions.add(new RMSE());
		functions.add(new MAE());
		functions.add(new SNR());
		functions.add(new PSNR());
		functions.add(new DiceLoss());
		functions.add(new Jaccard());
		functions.add(new SSIM());
		functions.add(new RegressSNR());
		functions.add(new LAP());
		
		String name[] = new String[functions.size()];
		
		int j=0;
		for(AbstractLoss function : functions) {
			name[j]=function.getName();
			j++;
		}
		
		// create the box of dialog
		gd = new GenericDialog("Settings");
		//IJ.log("Hello"); 
		Font Title = new Font("TimesRoman", Font.PLAIN, 20);
		//value that the users can change for the different settings for the functions
		gd.addMessage("LAP", Title, Color.black);
		gd.addNumericField("Starting Sigma:", 1, 0);
		gd.addMessage("SSIM", Title, Color.black);
		gd.addNumericField("Window Size:", 1, 0);
		gd.addMessage("Composed Function", Title, Color.black);
		gd.addChoice("First loss of Composed Function:", name, name[0]);
		gd.addNumericField("Coefficient:", 0, 2);
		gd.addChoice("Second loss of Composed Function:", name, name[0]);
		gd.addNumericField("Coefficient:", 0, 2);
		
		gd.showDialog();
		
		//get the values written by the user
		Setting setting = new Setting();
		setting.sig_lap=gd.getNextNumber();
		setting.wd_ssim=gd.getNextNumber();
		setting.w1_composed=gd.getNextNumber();
		setting.w2_composed=gd.getNextNumber();
		int index1 = gd.getNextChoiceIndex();
		setting.title1 = name[index1];
		int index2 = gd.getNextChoiceIndex();
		setting.title2 = name[index2];
		
		return setting;
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		IJ.log("setup");
		return 0;
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		IJ.log("run");
	}

}
