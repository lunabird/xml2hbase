import java.awt.List;
import java.util.ArrayList;

public class InnerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> a=new ArrayList<String>();
		ArrayList<String> b=new ArrayList<String>();
		if(a==b){
			System.out.println("���");
		}
		b=a;
		if(a==b){
			System.out.println("���okkkkk");
		}
		a.add("Hello");
		a.add("World");
		for(int i=0;i<b.size();i++){
			System.out.println("b "+i+" "+b.get(i));
		}
	}

}
