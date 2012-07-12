package it.rockeat.ui;

import it.rockeat.exception.BackendException;
import it.rockeat.exception.ConnectionException;
import it.rockeat.exception.ParsingException;
import it.rockeat.exception.UnknownPlayerException;
import it.rockeat.model.RockitAlbum;
import it.rockeat.source.MusicSource;

import java.awt.image.ImageObserver;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.BooleanUtils;

public class ParseTask extends SwingWorker<Void, Void> {

    private RockitAlbum album;
    private String url;
    private RockEatUI rockEatUI;

    public ParseTask(RockEatUI ui, String url) {
        this.rockEatUI = ui;
        this.url = url;
    }

    @Override
    public Void doInBackground() {
        rockEatUI.getProgressBar().setIndeterminate(true);
        rockEatUI.getProgressBar().setString(Messages.PARSE_IN_PROGRESS);
        rockEatUI.getProgressBar().setStringPainted(true);
        try {
            MusicSource eater = rockEatUI.getSourceManager().findSource(url);
            if (BooleanUtils.isFalse(eater.runTest())) {
                throw new UnknownPlayerException();
            }
            album = rockEatUI.getSourceManager().parse(url);            
//            album = new Album();
//            album.setTitle("Album di prova");
//            album.setArtist("Artista");
//            List<Track> tracks =new ArrayList<Track>();
//            Track track = new Track();
//            track.setAuthor("Artista");
//            track.setTitle("Lorem Ipsum");
//            tracks.add(track);
//            album.setTracks(tracks);

        } catch (ConnectionException e) {
        	rockEatUI.reset();
            JOptionPane.showMessageDialog(rockEatUI, Messages.ERROR_CONNECTION, Messages.TITLE, 0);
            setProgress(ImageObserver.ERROR);
        } catch (ParsingException e) {
        	rockEatUI.reset();
            JOptionPane.showMessageDialog(rockEatUI, Messages.NOTHING_FOUND, Messages.TITLE, 1);
            setProgress(ImageObserver.ERROR);
        } catch (MalformedURLException e) {
        	rockEatUI.reset();
            JOptionPane.showMessageDialog(rockEatUI, Messages.ERROR_URL, Messages.TITLE, 1);
            setProgress(ImageObserver.ERROR);
        } catch (UnknownPlayerException e) {
        	rockEatUI.reset();
            JOptionPane.showMessageDialog(rockEatUI, Messages.ERROR_PLAYER, Messages.TITLE, 0);
            setProgress(ImageObserver.ERROR);
        } catch (BackendException e) {
        	rockEatUI.reset();
            JOptionPane.showMessageDialog(rockEatUI, Messages.ERROR_BACKEND, Messages.TITLE, 0);
            setProgress(ImageObserver.ERROR);
        }
        return null;
    }

    @Override
    public void done() {
        if (album != null) {
            rockEatUI.startDownload(album);
        }


//		if (album!=null && CollectionUtils.isNotEmpty(album.getTracks())) {
//			DownloadTask downloadTask = new DownloadTask(ui, album);
//	        downloadTask.addPropertyChangeListener(ui);
//	        downloadTask.execute();
//		} else {
//	        ui.progressBar.setString("");
//	        ui.progressBar.setStringPainted(true);
//			ui.uiReset();
//		}
    }
}
