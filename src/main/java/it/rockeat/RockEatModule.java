package it.rockeat;

import it.rockeat.source.MusicSource;
import it.rockeat.source.rockit.Rockit;

import com.google.inject.AbstractModule;

public class RockEatModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MusicSource.class).to(Rockit.class);
	}

}
