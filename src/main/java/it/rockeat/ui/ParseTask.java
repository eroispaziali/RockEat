package it.rockeat.ui;

import it.rockeat.bean.Album;
import it.rockeat.eater.Eater;
import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.exception.UnknownPlayerException;

import java.awt.image.ImageObserver;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

public class ParseTask extends SwingWorker<Void, Void> {
	
	private Album album;
	private String url;
	private RockEatGui ui;
	
	public ParseTask(RockEatGui ui, String url) {
		this.ui = ui;
		this.url = url;
	}
	
	@Override
    public Void doInBackground() {
		ui.progressBar.setIndeterminate(true);
    	ui.progressBar.setString(Messages.PARSE_IN_PROGRESS);
    	ui.progressBar.setStringPainted(true);
		try {
			Eater eater = ui.controller.findEater(url);
			if (BooleanUtils.isFalse(eater.runTest())) {
				throw new UnknownPlayerException();
			}
			album = ui.controller.parse(url);

		} catch (ConnectionException e) {
			ui.uiReset();
			JOptionPane.showMessageDialog(ui, Messages.ERROR_CONNECTION, Messages.TITLE, 0);
			setProgress(ImageObserver.ERROR);
		} catch (ParsingException e) {
			ui.uiReset();
			JOptionPane.showMessageDialog(ui, Messages.NOTHING_FOUND, Messages.TITLE, 1);
			setProgress(ImageObserver.ERROR);
		} catch (MalformedURLException e) {
			ui.uiReset(); 
			JOptionPane.showMessageDialog(ui, Messages.ERROR_URL, Messages.TITLE, 1);
			setProgress(ImageObserver.ERROR);
		} catch (UnknownPlayerException e) {
			ui.uiReset();
			JOptionPane.showMessageDialog(ui, Messages.ERROR_PLAYER, Messages.TITLE, 0);
			setProgress(ImageObserver.ERROR);
		} catch (BackendException e) {
			ui.uiReset();
			JOptionPane.showMessageDialog(ui, Messages.ERROR_BACKEND, Messages.TITLE, 0);
			setProgress(ImageObserver.ERROR);
		}
        return null;
    }

    @Override
    public void done() {
		if (album!=null && CollectionUtils.isNotEmpty(album.getTracks())) {
			DownloadTask downloadTask = new DownloadTask(ui, album);
	        downloadTask.addPropertyChangeListener(ui);
	        downloadTask.execute();
		} else {
	        ui.progressBar.setString("");
	        ui.progressBar.setStringPainted(true);
			ui.uiReset();
		}
    }
}
