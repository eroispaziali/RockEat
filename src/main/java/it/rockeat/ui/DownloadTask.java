package it.rockeat.ui;

import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.model.Album;
import it.rockeat.model.RockitTrack;

import java.awt.image.ImageObserver;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class DownloadTask extends SwingWorker<Void, Void> {

	private Album album;
	private RockEatUI rockEatUI;
	private String label;

	public DownloadTask(RockEatUI jRockEatUI, Album album) {
		this.album = album;
		this.rockEatUI = jRockEatUI;
	}

	@Override
	public Void doInBackground() {
		rockEatUI.getProgressBar().setIndeterminate(false);
		rockEatUI.getProgressBar().setMinimum(0);
		rockEatUI.getProgressBar().setMaximum(album.getTracksCount() + 1);
		rockEatUI.getProgressBar().setStringPainted(true);

		Integer count = 0;
		for (RockitTrack track : album.getTracks()) {
			label = track.toString();
			try {
				setProgress(++count);
				rockEatUI.getController().download(album, track);
			} catch (FileSaveException e) {
				JOptionPane.showMessageDialog(rockEatUI,
						Messages.ERROR_FILEWRITE, Messages.TITLE, 0);
				setProgress(ImageObserver.ABORT);
				rockEatUI.reset();
			} catch (DownloadException e) {
				JOptionPane.showMessageDialog(rockEatUI, Messages.ERROR_PLAYER,
						Messages.TITLE, 0);
				setProgress(ImageObserver.ABORT);
				rockEatUI.reset();
			} catch (Exception e) {
				setProgress(ImageObserver.ERROR);
				rockEatUI.reset();
			}
		}
		if (rockEatUI.getController().getDownloadedTracks() > 0) {
			rockEatUI.getController().downloadFinished(album);
			label = Messages.DOWNLOAD_COMPLETE;
			label = StringUtils.replace(label, "{0}", Long.toString(rockEatUI
					.getController().getDownloadedTracks()));
			label = StringUtils.replace(label, "{1}", FileUtils
					.byteCountToDisplaySize(rockEatUI.getController()
							.getBytesDownloaded()));
		} else {
			label = Messages.ERROR_DOWNLOAD;
		}
		setProgress(++count);
		return null;
	}

	@Override
	public void done() {
		/* do nothing */
	}

	public String getLabel() {
		return label;
	}
}
