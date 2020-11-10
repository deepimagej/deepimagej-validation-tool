import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Vector;

/*import deepimagej.ImagePlus2Tensor;
import org.tensorflow.Tensor;*/

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.macro.Variable;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import loss.AbstractLoss;
import loss.Bce;
import loss.DiceLoss;
import loss.Jaccard;
import loss.LAP;
import loss.MAE;
import loss.RMSE;
import loss.RegressSNR;
import loss.NormL1;
import loss.NormL2;
import loss.PSNR;
import loss.SNR;
import loss.SSIM;

public class DeepImageJ_ImageValidation implements ActionListener,DialogListener,ExtendedPlugInFilter,PlugIn, ItemListener{

	private static String title1 = "";
	private static String title2 = "";

	
	private TextArea info;
	private GenericDialog gd ;
	Hello_World hello = new Hello_World();
	Panel settings = new Panel();
	Button button = new Button("Advanced");
	private int dec=3;
	
	
	
	public static void main(String arg[]) {
		new DeepImageJ_ImageValidation().run("");
	}

	public void run(String arg) {
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
		
		
		
		info = new TextArea("Information ", 5, 48, TextArea.SCROLLBARS_VERTICAL_ONLY);
		info.setEditable(false);
		info.setText("Infos aboout images");
		Panel panel = new Panel(new BorderLayout());
		panel.add(info, BorderLayout.CENTER);
		
		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.error("lo");
			return;
		}
		IJ.register(ImageCalculator.class);
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}
		//this.pfr;
		//ImagePlus imp = WindowManager.getImage(wList[0]);

		
		gd = new GenericDialog("Loss Functions");
		/*PlugInFilterRunner pfr = new PlugInFilterRunner(hello, "run", "1");
		gd.addPreviewCheckbox(pfr);*/
		gd.addDialogListener(this);
		String defaultItem;
		if (title1.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title1;
		gd.addChoice("Reference image:", titles, defaultItem);
		if (title2.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title2;
		gd.addChoice("Test image:", titles, defaultItem);
		gd.addPanel(panel);
		int col=3,row=5;
		String name[] = new String[functions.size()];
		boolean content[] = new boolean[functions.size()];
		int j=0;
		for(AbstractLoss function : functions) {
			name[j]=function.getName();
			content[j]=function.getName().contentEquals("NormL2");
			j++;
		}
		button.addActionListener(this);
		settings.add(button);
		gd.addCheckboxGroup(row , col , name, content);
		gd.addPanel(settings);
		gd.addHelp("https://deepimagej.github.io/deepimagej/download.html");

		for (Component c : gd.getComponents()) {
			if (c instanceof Choice) {
				Choice choice = (Choice) c;
				choice.addItemListener(this);
			}
		}
		gd.addNumericField("Decimal places (0-9):", dec, 0);
		
		gd.showDialog();
		
		IJ.error(Integer.toString(dec));
		
		int decimals=(int)gd.getNextNumber();
		
		for(AbstractLoss function : functions)
			function.setSelected(gd.getNextBoolean());
		
		if (gd.wasCanceled())
			return;
		int index1 = gd.getNextChoiceIndex();
		title1 = titles[index1];
		int index2 = gd.getNextChoiceIndex();
		title2 = titles[index2];
		info.setText(title2);
		ImagePlus img1 = WindowManager.getImage(wList[index1]);
		ImagePlus img2 = WindowManager.getImage(wList[index2]);
		
		//Tensor<Float> tensor = ImagePlus2Tensor.implus2TensorFloat(img1, "");
		//int numten = tensor.numElements();
		//IJ.error(Integer.toString(numten));
		
		ResultsTable table = new ResultsTable();
		
		int stack1=1;
		int stack2=1;
		
		Boolean increment_both=false;
		Boolean increment_first=false;
		Boolean increment_sec=false;
		Boolean first_func=true;
		Boolean jaccard=false;
		int nzr = img1.getStack().getSize();
		int nzt = img2.getStack().getSize();
		int nbf_sel=0;
		int nbf_sel_jacc=0;
		int nbjacc = 0;
		
		double max_im1 = img1.getStack().getProcessor(1).getMax();
		double max_im2 = img2.getStack().getProcessor(1).getMax();
		
		double min_im1 = img1.getStack().getProcessor(1).getMax();
		double min_im2 = img2.getStack().getProcessor(1).getMax();
		
		ImageProcessor ipt = img1.getStack().getProcessor(1);
		ImageProcessor ipr = img2.getStack().getProcessor(1);
		
		int nxr = img1.getWidth();
		int nyr = img2.getHeight();
		for(AbstractLoss function : functions)
			if (function.getSelected()) {
				if(function.getName()=="Jaccard") {
					nbjacc =(int)img1.getStack().getProcessor(1).getMax()+1;
					jaccard=true;
					if((min_im1<0)||(min_im2<0)) {
						IJ.error("For Jaccard, values must be positive");
						return;
					}
					double s,g;
					for (int x = 0; x < nxr; x++) {
						for (int y = 0; y < nyr; y++) {
							
							s =  ipr.getPixelValue(x, y);
							g = ipt.getPixelValue(x, y);
							if ((s%1)!=0.0||(g%1)!=0.0) {
								IJ.error("For Jaccard, values must be integer");
								return;
							}
						}
					}
					
				}
				else if(function.getName()=="Bce") {
					if((max_im1>1)||(max_im2>1)||(min_im1<0)||(min_im2<0)) {
						IJ.error("For Bce, values must be between 0 and 1");
						return;
					}
				}
				else {
					nbf_sel++;
				}
			}
		
		String results[][]= new String[nbf_sel][Math.max(nzr, nzt)];
		String result_jacc[]= new String[Math.max(nzr, nzt)];
		ArrayList<String> funcname = new ArrayList<String>();
		
		if(nzr==nzt) {
			increment_both=true;
		}
		else if(nzr>nzt) {
			if(nzt==1) {
				increment_first=true;
			}
			else {
				IJ.error("Wrong number of stacks");
				return;
			}
		}
		else if(nzr<nzt) {
			if(nzr==1) {
				increment_sec=true;
			}
			else {
				IJ.error("Wrong number of stacks");
				return;
			}
		}
		int nfunc=0;
		int nloss=0;
		for(AbstractLoss function : functions)
			if (function.getSelected()) { 
				funcname.add(function.getName());
				
				ArrayList<Double> losses= function.run(img1, img2);
				if(function.getName()=="Jaccard") {
					nloss=0;
					
					for(Double loss : losses ) {
						
						result_jacc[nloss]=String.format("%.0"+Integer.toString(decimals)+"f",loss);
						nloss++;
					}
					
				}
				else {
					nloss=0;
					
					for(Double loss : losses ) {
						
						results[nfunc][nloss]=String.format("%.0"+Integer.toString(decimals)+"f",loss);
						nloss++;
					}
					
					nfunc++;
				}
				
				
			}
		int no_im_jacc=0;
		for (int l = 0; l < Math.max(nzr, nzt) ; l++) {
			if(jaccard) {
				for(int m = 0; m < nbjacc; m++) {
					
					table.incrementCounter();
					table.addValue("ref", img1.getTitle());
					table.addValue("test", img2.getTitle());
					if(m==nbjacc-1) {
						table.addValue("N° Ref", Integer.toString(stack1)+"(average)");
						table.addValue("N° Test", Integer.toString(stack2)+"(average)");
					}
					else {
						table.addValue("N° Ref", Integer.toString(stack1)+"("+Integer.toString(m+1)+")");
						table.addValue("N° Test", Integer.toString(stack2)+"("+Integer.toString(m+1)+")");
					}
					table.addValue("Jaccard", result_jacc[no_im_jacc+m]);
					for (int i = 0; i < funcname.size(); i++) {
						table.addValue(funcname.get(i), results[i][l]);
					}
					
				}
				no_im_jacc+=nbjacc;
				
				if (increment_both) {
					stack1++;
					stack2++;
				}
				else if(increment_first) {
					stack1++;
				}
				else if(increment_sec) {
					stack2++;
				}
			}
			else {
				
				table.incrementCounter();
				table.addValue("ref", img1.getTitle());
				table.addValue("test", img2.getTitle());
				table.addValue("N° Ref", stack1);
				table.addValue("N° Test", stack2);
				for (int i = 0; i < funcname.size(); i++) {
					table.addValue(funcname.get(i), results[i][l]);
				}
				if (increment_both) {
					stack1++;
					stack2++;
				}
				else if(increment_first) {
					stack1++;
				}
				else if(increment_sec) {
					stack2++;
				}
				
			}
			
			
		}
		table.show("Loss");
		
		
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		Vector choices = gd.getChoices();
		Choice choice_one = (Choice)choices.elementAt(0);
		Choice choice_two = (Choice)choices.elementAt(1);
		String item1 = choice_one.getSelectedItem();
		String item2 = choice_two.getSelectedItem();
		
		int[] wList = WindowManager.getIDList();
		ImagePlus img1 = WindowManager.getImage(item1);
		ImagePlus img2 = WindowManager.getImage(item2);
		int nx1= img1.getWidth();
		int ny1= img1.getHeight();
		int BitDepth1 = img1.getBitDepth();
		int typ1= img1.getType();
		int NSlices1 = img1.getNSlices();
		int nx2= img2.getWidth();
		int ny2= img2.getHeight();
		int BitDepth2 = img2.getBitDepth();
		int typ2= img2.getType();
		int NSlices2 = img2.getNSlices();

		
		String[] cat ={"GRAY8","GRAY16","GRAY32","COLOR_256","COLOR_RGB"};
		
		info.setText(item1 + ":" + "Size: " + " [" + nx1 + " " + ny1 + "]" + "," + "BitDepth: " + BitDepth1 + "," + "NSlices: " + NSlices1 + "Cat :"+ cat[typ1] + "\n");
		info.append(item2 + ":" + "Size: " + " [" + nx2 + " " + ny2 + "]" + "," + "BitDepth: " + BitDepth2 + "," + "NSlices: " + NSlices2 + "Cat :"+ cat[typ2] + "\n");
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		// TODO Auto-generated method stub
		
		return 0;
	}

	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		// TODO Auto-generated method stub
		
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//IJ.log("advanced");
		dec=hello.run("");
		return;
	}
	
}

