package loss;
public class Setting {

	public double wd_ssim, sig_lap, w1_composed, w2_composed;
	public String title1, title2;
	public Setting(){
		
		this.wd_ssim=3.0;
		this.sig_lap=0.0;
		this.w1_composed=0.0;
		this.w2_composed=0.0;
		this.title1="";
		this.title2="";
	}
}
