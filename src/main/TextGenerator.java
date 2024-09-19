package main;

import java.util.Scanner;

public class TextGenerator {

	public static void main(String[] args) {
		String src = "=(VLOOKUP('10-10-10'!F9, まとめ!J8:K27, 2, FALSE) + VLOOKUP('10-10-10'!G9, まとめ!J8:K27, 2, FALSE) + VLOOKUP('10-10-10'!H9, まとめ!J8:K27, 2, FALSE))";
		Scanner sc = new Scanner(System.in);
		String str = sc.next()+"-"+sc.next()+"-"+sc.next();
		sc.close();
		System.out.println(src.replace("10-10-10", str));
	}

}
