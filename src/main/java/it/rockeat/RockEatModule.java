package it.rockeat;

import it.rockeat.source.MusicSource;
import it.rockeat.source.soundcloud.SoundCloud;

import com.google.inject.AbstractModule;

public class RockEatModule extends AbstractModule {

	@Override
	protected void configure() {
		//bind(MusicSource.class).to(Rockit.class);
		bind(MusicSource.class).to(SoundCloud.class);

	}

}
