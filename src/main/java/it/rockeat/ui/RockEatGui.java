package it.rockeat.ui;

import it.rockeat.Controller;
import it.rockeat.bean.Album;
import it.rockeat.bean.Track;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.exception.ParsingException;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class RockEatGui extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID 	= 7764484387055760031L;
	
	protected JLabel label;
	protected JTextField textField;
	protected JTextArea textArea;
	protected JProgressBar progressBar;
	protected JButton startButton;
	protected GridBagConstraints c;
	protected Controller controller = new Controller();

	public class DownloadTask extends SwingWorker<Void, Void> {
		
		private Album album;
		private String label;
		private Component component;
		
		public DownloadTask(Album album, Component component) {
			this.album = album;
			this.component = component;
		}

		@Override
	    public Void doInBackground() {
			Integer count = 0;
			for (Track track : album.getTracks()) {
				label = track.toString();
				try {
					setProgress(++count);
					controller.download(album, track);
				} catch (FileSaveException e) {
					JOptionPane.showMessageDialog(component, Messages.ERROR_FILEWRITE, Messages.TITLE, 0);
					setProgress(ABORT);
				} catch (Exception e) {
					setProgress(ERROR);
				}
			}
			if (controller.getDownloadedTracks()>0) {
				label = Messages.DOWNLOAD_COMPLETE;
				label = StringUtils.replace(label,"{0}", Long.toString(controller.getDownloadedTracks()));
				label = StringUtils.replace(label,"{1}", FileUtils.byteCountToDisplaySize(controller.getBytesDownloaded()));
			} else {
				label = Messages.ERROR_DOWNLOAD;
			}
			setProgress(++count);
	        return null;
	    }

	    @Override
	    public void done() {
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
        startButton = new JButton("Mangia");
        startButton.addActionListener(this);
        progressBar = new JProgressBar(0,100);
 
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
			Album album = controller.parse(url);
			
			progressBar.setMinimum(0);
			progressBar.setMaximum(album.getTracksCount()+1);
			progressBar.setStringPainted(true);
			
			if (CollectionUtils.isNotEmpty(album.getTracks())) {
				DownloadTask downloadTask = new DownloadTask(album, this);
		        downloadTask.addPropertyChangeListener(this);
		        downloadTask.execute();
			}
			
		} catch (ConnectionException e) {
			JOptionPane.showMessageDialog(this, Messages.ERROR_CONNECTION, Messages.TITLE, 0);
			uiReset();
		} catch (ParsingException e) {
			JOptionPane.showMessageDialog(this, Messages.NOTHING_FOUND, Messages.TITLE, 1);
			uiReset();
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(this, Messages.ERROR_URL, Messages.TITLE, 1);
			uiReset();
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
        	DownloadTask task = (DownloadTask) evt.getSource();
            progressBar.setValue((Integer)evt.getNewValue());
            progressBar.setString(task.getLabel());
            progressBar.setStringPainted(true);
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
