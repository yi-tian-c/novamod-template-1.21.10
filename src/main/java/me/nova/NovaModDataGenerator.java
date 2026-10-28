package me.nova;

import me.nova.datagen.NovaLanguageProvider;
import me.nova.datagen.NovaModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class NovaModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack =
				fabricDataGenerator.createPack();
		pack.addProvider(NovaModelProvider::new);
		pack.addProvider(NovaLanguageProvider::new);
	}
}
