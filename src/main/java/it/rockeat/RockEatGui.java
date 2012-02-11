package it.rockeat;

import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

public class RockEatGui extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 7764484387055760031L;
	protected JTextField textField;
	protected JTextArea textArea;
	protected JProgressBar progressBar;
	protected JButton startButton;
	protected GridBagConstraints c;
	protected RockEater eater;

	public class DownloadTask extends SwingWorker<Void, Void> {
		
		private Album album;
		private String label;
		
		public DownloadTask(Album album) {
			this.album = album;
		}

		@Override
	    public Void doInBackground() {
			Integer count = 0;
			for (Track track : album.getTracks()) {
				label = track.toString();
				try {
					setProgress(++count);
					eater.download(album, track);
				} catch (Exception e) {
					System.out.println("non riesce a salvare il file. Disco pieno?");
				}
			}
			label = "RockEat ha scaricato " + Long.toString(eater.getDownloadedTracks()) + " tracce (" + FileUtils.byteCountToDisplaySize(eater.getBytesDownloaded()) + ") e spera che ti piacciano";
			setProgress(++count);
	        return null;
	    }

	    @Override
	    public void done() {
	        Toolkit.getDefaultToolkit().beep();
	        uiReset();
	    }
	    
	    public String getLabel() {
	    	return label;
	    }
	    
   }
	
	protected void uiReset() {
        startButton.setEnabled(true);
        textField.setText("");
        textField.setEnabled(true);
        setCursor(null);
	}
	
    public RockEatGui() {
        super(new GridBagLayout());
 
        textField = new JTextField(40);
        textField.setText("");
        
        startButton = new JButton("Vai");
        startButton.addActionListener(this);
 
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        add(textField, c);
        add(startButton, c);
        c.fill = GridBagConstraints.BOTH;
        
        c.weightx = 1.0;
        c.weighty = 1.0;
        progressBar = new JProgressBar(0,100);
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
        add(progressBar, c);
        
        uiReset();
    }
	 
    public void actionPerformed(ActionEvent evt) {
    	textField.setEnabled(false);
    	startButton.setEnabled(false);
        String url = textField.getText();
        
        eater = new RockEater();
        eater.setDownloadEnabled(Boolean.TRUE);
        eater.setId3TaggingEnabled(Boolean.TRUE);
        
        startButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        
        try {
			Album album = eater.parse(url);
			
			progressBar.setMinimum(0);
			progressBar.setMaximum(album.getTracksCount()+1);
			progressBar.setStringPainted(true);
			
			if (CollectionUtils.isNotEmpty(album.getTracks())) {
				DownloadTask task = new DownloadTask(album);
		        task.addPropertyChangeListener(this);
		        task.execute();
			}
			
		} catch (MalformedURLException e) {
			 JOptionPane.showMessageDialog(this, "RockEat ha bisogno di un indirizzo vero", "RockEat", 0);
			 uiReset();
		} catch (ConnectionException e) {
			 JOptionPane.showMessageDialog(this, "RockEat non è riuscito a connettersi", "RockEat", 0);
			 uiReset();
		} catch (ParsingException e) {
			JOptionPane.showMessageDialog(this, "RockEat non ha trovato nulla da mangiare","RockEat", 1);
			uiReset();
		}
    }
 
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
        	DownloadTask task = (DownloadTask) evt.getSource();
            progressBar.setValue((Integer)evt.getNewValue());
            progressBar.setString(task.getLabel());
            progressBar.setStringPainted(true);
        } 
    }
    
    public static void main(String[] args) {
    	
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
    	        JFrame frame = new JFrame("RockEat");
    	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	        frame.add(new RockEatGui());
    	        frame.pack();
    	        frame.setVisible(true);
            }
        });
    }

}
