package it.rockeat;

import it.rockeat.ui.RockEatCli;
import it.rockeat.ui.RockEatUI;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RockEat {

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new RockEatModule());

		if (args.length > 0) {
			RockEatCli.main(args);
		} else {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				/* silently ignore */
			}
			JFrame frame = injector.getInstance(RockEatUI.class);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		}
	}

}
