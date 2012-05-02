package it.rockeat;

import it.rockeat.ui.RockEatCli;
import it.rockeat.ui.RockEatGui;

import java.awt.HeadlessException;

public class RockEat {
	
	public static void main(String[] args) {
		try {
			RockEatGui.main(args);
		} catch (HeadlessException e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getClass().toString());
			RockEatCli.main(args);
		}
	}

}
