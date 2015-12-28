package laser.ddg.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QuitCommand implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		System.exit (0);
	}

}
