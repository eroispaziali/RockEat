package it.rockeat;

import it.rockeat.ui.RockEatCli;
import it.rockeat.ui.RockEatGui;

public class RockEat {
	
	public static void main(String[] args) {
		if (args.length > 0) {
			RockEatCli.main(args);
		} else {
			RockEatGui.main(args);	
		}
	}

}
