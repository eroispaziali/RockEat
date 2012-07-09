package it.rockeat.ui;

import it.rockeat.exception.DownloadException;
import it.rockeat.exception.FileSaveException;
import it.rockeat.model.RockitAlbum;
import it.rockeat.model.RockitTrack;

import java.awt.image.ImageObserver;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class DownloadTask extends SwingWorker<Void, Void> {

    private RockitAlbum album;
    private String label;
    private RockEatGui ui;

    public DownloadTask(RockEatGui ui, RockitAlbum album) {
        this.album = album;
        this.ui = ui;
    }

    @Override
    public Void doInBackground() {
        ui.progressBar.setIndeterminate(false);
        ui.progressBar.setMinimum(0);
        ui.progressBar.setMaximum(album.getTracksCount() + 1);
        ui.progressBar.setStringPainted(true);

        Integer count = 0;
        ui.progressBar.setIndeterminate(false);
        for (RockitTrack track : album.getTracks()) {
            label = track.toString();
            try {
                setProgress(++count);
                ui.controller.download(album, track);
            } catch (FileSaveException e) {
                JOptionPane.showMessageDialog(ui, Messages.ERROR_FILEWRITE, Messages.TITLE, 0);
                setProgress(ImageObserver.ABORT);
            } catch (DownloadException e) {
                JOptionPane.showMessageDialog(ui, Messages.ERROR_PLAYER, Messages.TITLE, 0);
                setProgress(ImageObserver.ABORT);
            } catch (Exception e) {
                setProgress(ImageObserver.ERROR);
            }
        }
        if (ui.controller.getDownloadedTracks() > 0) {
            label = Messages.DOWNLOAD_COMPLETE;
            label = StringUtils.replace(label, "{0}", Long.toString(ui.controller.getDownloadedTracks()));
            label = StringUtils.replace(label, "{1}", FileUtils.byteCountToDisplaySize(ui.controller.getBytesDownloaded()));
        } else {
            label = Messages.ERROR_DOWNLOAD;
        }
        setProgress(++count);
        return null;
    }

    @Override
    public void done() {
        ui.uiReset();
    }

    public String getLabel() {
        return label;
    }
}
