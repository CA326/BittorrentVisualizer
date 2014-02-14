import javax.xml.bind.DatatypeConverter;
import java.util.Scanner;
class Test {

	public static boolean bitSet(int i, char [] c) {
		return c[i] == '1';
	}

	public static void main(String [] args) {
		String s = "FFFFF7EEFFFFFFFFFDFF79FE7EE0";
		String binary = "";
		char [] bitfield;

		int j = 0;
		for(int i = 0; i < s.length(); i += 2) {
			int n = Integer.parseInt(s.substring(i, i + 2), 16);
			String theByte = Integer.toBinaryString(n);
			System.out.println(i + ": " + theByte);
			binary += theByte;
		}

		bitfield = binary.toCharArray();

		Scanner in = new Scanner(System.in);

		while(true) {
			System.out.println("Index of bit to check: ");
			int i = in.nextInt();
			System.out.println(bitSet(i, bitfield));
		}
	}
}