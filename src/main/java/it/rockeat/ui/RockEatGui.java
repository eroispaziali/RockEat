package it.rockeat.ui;

import it.rockeat.SourceManager;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RockEatGui extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID 	= 7764484387055760031L;
	
	protected JLabel label;
	protected JTextField textField;
	protected JTextArea textArea;
	protected JProgressBar progressBar;
	protected JButton startButton;
	protected GridBagConstraints c;
	protected SourceManager controller = new SourceManager();

	protected void uiReset() {
        startButton.setEnabled(true);
        textField.setText("");
        textField.setEnabled(true);
        setCursor(null);
        progressBar.setIndeterminate(false);
	}
	
    public RockEatGui() {
    	super(new GridBagLayout());
        textField = new JTextField(40);
        startButton = new JButton("Mangia");
        startButton.addActionListener(this);
        progressBar = new JProgressBar(0,100);
        
//        JLabel picLabel = new JLabel(new ImageIcon("resources/rockeat.png"));
//        c = new GridBagConstraints();
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.insets = new Insets(15,5,5,5);
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weightx = 1;
//        add(picLabel, c);
        
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(15,5,5,5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(textField, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(15,5,0,5);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        add(startButton, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        add(progressBar, c);
        
  
		
        uiReset();
    }
	 
    @Override
	public void actionPerformed(ActionEvent evt) {
    	textField.setEnabled(false);
    	startButton.setEnabled(false);
        String url = textField.getText();
        
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        
        try {
//			ParseTask parseTask = new ParseTask(this, url);
//			parseTask.addPropertyChangeListener(this);
//			parseTask.execute();
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(this, Messages.ERROR_URL, Messages.TITLE, 1);
			uiReset();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, Messages.ERROR_UNEXPECTED, Messages.TITLE, 0);
			uiReset();
		}
    }
 
    @Override
	public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
        	if (evt.getSource() instanceof DownloadTask) {
	        	DownloadTask task = (DownloadTask) evt.getSource();
	            progressBar.setValue((Integer)evt.getNewValue());
	            progressBar.setString(task.getLabel());
	            progressBar.setStringPainted(true);
        	}
        } 
    }
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
    	        JFrame frame = new JFrame(Messages.TITLE);
    	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	        frame.add(new RockEatGui());
    	        frame.pack();
    	        frame.setSize(new Dimension(500,140));
    	        frame.setResizable(true);
    	        frame.setVisible(true);
            }
        });
    }

}
