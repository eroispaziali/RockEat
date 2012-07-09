package it.rockeat;

import it.rockeat.ui.RockEatCli;
import it.rockeat.ui.RockEatUI;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class RockEat {
	
	public static void main(String[] args) {
		if (args.length > 0) {
			RockEatCli.main(args);
		} else {
            try {           
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                /* silently ignore */
            }
            JFrame frame = new RockEatUI();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);	
		}
	}

}
