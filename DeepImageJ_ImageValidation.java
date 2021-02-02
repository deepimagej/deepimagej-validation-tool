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
import loss.Categorical_Hinge;
import loss.Composed;
import loss.DiceLoss;
import loss.Hinge;
import loss.Jaccard;
import loss.KLD;
import loss.LAP;
import loss.Log_Cosh;
import loss.MAE;
import loss.MAPE;
import loss.MSLE;
import loss.RMSE;
import loss.RegressSNR;
import loss.NormL1;
import loss.NormL2;
import loss.PSNR;
import loss.Poisson;
import loss.SNR;
import loss.SSIM;
import loss.Setting;
import loss.Square_Hinge;



public class DeepImageJ_ImageValidation implements ActionListener,PlugIn, ItemListener{

	private static String title1 = "";
	private static String title2 = "";

	
	private TextArea info;
	
	private GenericDialog gd ;
	Settings set = new Settings();
	Panel settings = new Panel();
	Button button = new Button("Advanced");
	private Setting setting = new Setting();
	public int columns_function = 4 , rows_function = 6;
	public int columns_text = 40 , rows_text = 3;
	
	
	
	public static void main(String arg[]) {
		new DeepImageJ_ImageValidation().run("");
	}

	public void run(String arg) {
		
		// import loss functions an array list
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
		functions.add(new Composed());
		functions.add(new KLD());
		functions.add(new MAPE());
		functions.add(new MSLE());
		functions.add(new Categorical_Hinge());
		functions.add(new Hinge());
		functions.add(new Log_Cosh());
		functions.add(new Poisson());
		functions.add(new Square_Hinge());
		
		
		
		
		// create table of text with infos about images loaded
		info = new TextArea("Information ", rows_text, columns_text, TextArea.SCROLLBARS_VERTICAL_ONLY);
		info.setEditable(false);
		
		
		//get list of images open in ImageJ
		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.noImage();
			return;
		}
		
		//get infos about the image put initially in the scroll bar 
		ImagePlus img_initial = WindowManager.getImage(wList[0]);
		int nx1= img_initial.getWidth();
		int ny1= img_initial.getHeight();
		int BitDepth1 = img_initial.getBitDepth();
		int typ1= img_initial.getType();
		int NSlices1 = img_initial.getNSlices();
		
		//corresponding text to the categories of images
		String[] cat ={"GRAY8","GRAY16","GRAY32","COLOR_256","COLOR_RGB"};
		
		//set text in the edit text
		info.setText( img_initial.getTitle() + " "  + "Size: " + " [" + nx1 + " " + ny1 + "]" + " , " + "BitDepth: " + BitDepth1 + " , " + "NSlices: " + NSlices1 + " , " + "Cat : "+ cat[typ1] + "\n");
		info.append( img_initial.getTitle() + " "  + "Size: " + " [" + nx1 + " " + ny1 + "]" + " , " + "BitDepth: " + BitDepth1 + " , " + "NSlices: " + NSlices1 + " , " + "Cat : "+ cat[typ1] + "\n");
		
		Panel panel = new Panel(new BorderLayout());
		panel.add(info, BorderLayout.CENTER);

		
		//Create a list with titles of all images
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}

		//create the box of dialog
		gd = new GenericDialog("Loss Functions");
		
		//Create scroll bar to choose images to be used
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
		
		//add the box of text to the dialog
		gd.addPanel(panel);
		
		
		//get the names of the different loss functions and create a matrix of checkboxes with those one
		String name[] = new String[functions.size()];
		boolean content[] = new boolean[functions.size()];
		
		int j=0;
		for(AbstractLoss function : functions) {
			name[j] = function.getName();
			content[j] = function.getName().contentEquals("NormL2");
			j++;
		}
		
		gd.addCheckboxGroup(rows_function , columns_function , name, content);
		
		//create a button "settings" to set the parameters of different loss functions
		button.addActionListener(this);
		settings.add(button);
		gd.addPanel(settings);
		//help button
		gd.addHelp("https://deepimagej.github.io/deepimagej/download.html");

		for (Component c : gd.getComponents()) {
			if (c instanceof Choice) {
				Choice choice = (Choice) c;
				choice.addItemListener(this);
			}
		}
		
		//put a numeric fiel to know the number of decimals the user wants
		gd.addNumericField("Decimal places (0-9):", 3, 0);
		
		//display the dialog box 
		gd.showDialog();
		
		// Now we will get the infos of the dialog box 
		
		
		int decimals = (int)gd.getNextNumber();
		
		//set true to function selected and false to others
		for(AbstractLoss function : functions)
			function.setSelected(gd.getNextBoolean());
		
		//cancel the dialog box
		if (gd.wasCanceled())
			return;
		
		//get the images selected in the scrollbar
		int index1 = gd.getNextChoiceIndex();
		title1 = titles[index1];
		int index2 = gd.getNextChoiceIndex();
		title2 = titles[index2];
		info.setText(title2);
		ImagePlus img1 = WindowManager.getImage(wList[index1]);
		ImagePlus img2 = WindowManager.getImage(wList[index2]);
		

		
		ResultsTable table = new ResultsTable();
		
		int stack1=1;
		int stack2=1;
		
		Boolean increment_both=false;
		Boolean increment_first=false;
		Boolean increment_sec=false;
		Boolean first_func=true;
		Boolean segmented=false;
		int nzr = img1.getStack().getSize();
		int nzt = img2.getStack().getSize();
		int nb_function_selected=0;
		int nb_function_segmented=0;
		
		int place_jacc = 0; //find the place of the segmented
		
		//get the max of the first stack of the images
		double max_im1 = img1.getStack().getProcessor(1).getMax();
		double max_im2 = img2.getStack().getProcessor(1).getMax();
		
		//get the min of the first stack of the images
		double min_im1 = img1.getStack().getProcessor(1).getMax();
		double min_im2 = img2.getStack().getProcessor(1).getMax();
		
		//get the first stack of the images
		ImageProcessor ipt = img1.getStack().getProcessor(1);
		ImageProcessor ipr = img2.getStack().getProcessor(1);
		
		int nxr1 = img1.getWidth();
		int nyr1 = img1.getHeight();
		int nxr2 = img1.getWidth();
		int nyr2 = img1.getHeight();
		
		if(nxr1 != nxr2 || nyr1 != nyr2 )
			IJ.error("Images do not have the same dimensions");
		
		
		
		
		place_jacc =(int)img1.getStack().getProcessor(1).getMax()+1;
		
		//Verify some constraints of the loss functions and display an error if there is one
		for(AbstractLoss function : functions)
			if (function.getSelected()) {
				String valid = function.check(img1, img2, setting);
				if(!valid.equals("Valid")) {
					IJ.error(valid);
					function.setSelected(false);
					//return;
				}
			}
		
		for(AbstractLoss function : functions)
			if (function.getSelected()) {
				if(function.getSegmented() == true) {
					segmented=true;
					nb_function_segmented++;
				}
				else {
					nb_function_selected++;
				}
			}
		
		//create a table which will store results
		String results[][]= new String[nb_function_selected][Math.max(nzr, nzt)];
		String results_segmented[][]= new String[nb_function_segmented][place_jacc*Math.max(nzr, nzt)];
		ArrayList<String> funcname = new ArrayList<String>();
		ArrayList<String> funcname_segmented = new ArrayList<String>();
		
		// check for the number of stacks if the reference and test havve multiple stacks 
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
		
		int nfunc=0; //function counter 
		int nfunc_segmented=0;
		int nloss=0;// image comparison counter
		
		
		
		ArrayList<Double> loss1_comp = null, loss2_comp = null;
		//call the classes of loss function to get the results
		for(AbstractLoss function : functions) {
			if (function.getSelected()) { 
				ArrayList<Double> losses= null;
				if(function.getName()=="Composed Function") {
					losses= function.compose(loss1_comp, setting.w1_composed, loss2_comp, setting.w2_composed);
				}
				else {
					losses= function.run(img1, img2,setting);
					if(function.getName()== setting.title1) {
						loss1_comp=losses;
					}
					else if(function.getName()== setting.title2) {
						loss2_comp=losses;
					}
				}
				
				//store in a special list for segmented
				if(function.getSegmented() == true) {
					funcname_segmented.add(function.getName());
					nloss=0;
					
					for(Double loss : losses ) {
						
						results_segmented[nfunc_segmented][nloss]=String.format("%.0"+Integer.toString(decimals)+"f",loss);
						nloss++;
					}
					nfunc_segmented++;
					
				}
				else {
					funcname.add(function.getName());
					nloss=0;
					
					for(Double loss : losses ) {
						
						results[nfunc][nloss]=String.format("%.0"+Integer.toString(decimals)+"f",loss);
						nloss++;
					}
					
					nfunc++;
				}
				
				
			}
			else {
				
				if(function.getName()== setting.title1) {
					loss1_comp = function.run(img1, img2,setting);
				}
				else if(function.getName()== setting.title2) {
					loss2_comp = function.run(img1, img2,setting);
				}
			}
		}
		
		int no_im_jacc=0;
		int fc_segmented = 0;
		//Create the table of results
		for (int l = 0; l < Math.max(nzr, nzt) ; l++) {
			if(segmented) {
				for(int m = 0; m < place_jacc; m++) {
					
					table.incrementCounter();
					table.addValue("ref", img1.getTitle());
					table.addValue("test", img2.getTitle());
					if(m==place_jacc-1) {
						table.addValue("N Ref", Integer.toString(stack1)+"(average)");
						table.addValue("N Test", Integer.toString(stack2)+"(average)");
					}
					else {
						table.addValue("N Ref", Integer.toString(stack1)+"("+Integer.toString(m+1)+")");
						table.addValue("N Test", Integer.toString(stack2)+"("+Integer.toString(m+1)+")");
					}
					for (int i = 0; i < funcname_segmented.size(); i++) {
						table.addValue(funcname_segmented.get(i), results_segmented[i][no_im_jacc+m]);
					}
					for (int i = 0; i < funcname.size(); i++) {
						table.addValue(funcname.get(i), results[i][l]);
					}
					
				}
				no_im_jacc+=place_jacc;
				
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
				table.addValue("N Ref", stack1);
				table.addValue("N Test", stack2);
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
		// display table of results
		table.show("Loss");
		
		
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		//get the change of choice in the scroll bar 
		Vector choices = gd.getChoices();
		Choice choice_one = (Choice)choices.elementAt(0);
		Choice choice_two = (Choice)choices.elementAt(1);
		String item1 = choice_one.getSelectedItem();
		String item2 = choice_two.getSelectedItem();
		
		//get infos about the 2 images selected
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
		
		//set text
		info.setText( item1 + " "  + "Size: " + " [" + nx1 + " " + ny1 + "]" + " , " + "BitDepth: " + BitDepth1 + " , " + "NSlices: " + NSlices1 + " , " + "Cat : "+ cat[typ1] + "\n");
		info.append( item2 + " " + "Size: " + " [" + nx2 + " " + ny2 + "]" + " , " + "BitDepth: " + BitDepth2 + " , " + "NSlices: " + NSlices2 + " , " + "Cat : "+ cat[typ2] + "\n");
		
		/* v�rifier les entr�es pour d�cocher les loss fonctions non utilisables */
	}

	//Go to Setting dialog box 
	public void actionPerformed(ActionEvent e) {
		setting=set.run("");
		return;
	}
	
}

