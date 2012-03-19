package it.rockeat;

import it.rockeat.ui.RockEatCli;
import it.rockeat.ui.RockEatGui;

import java.awt.GraphicsEnvironment;

public class RockEat {
	
	public static void main(String[] args) {
		if (GraphicsEnvironment.isHeadless()) {
			RockEatGui.main(args);
		} else {
			RockEatCli.main(args);
		}
	}

}
