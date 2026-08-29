package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.archEx.ArchExCompat;
import com.simibubi.create.compat.curios.CuriosDataGenerator;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.CreateDatamapProvider;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.TagLangGen;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistrySetBuilder;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;

public class CreateDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
		FabricDataGenerator.Pack pack = generator.createPack();
		// all addDataGenerator calls must happen before setupDatagen constructs the root provider
		gatherData(pack, helper);
		Create.registrate().setupDatagen(pack, helper);
	}

	public static void gatherData(FabricDataGenerator.Pack pack, ExistingFileHelper existingFileHelper) {
		addExtraRegistrateData();

		// fabric: tag lang
		TagLangGen.datagen();
		// fabric: archex compat
		ArchExCompat.init(pack);

		// fabric: pretty much redone, make sure all providers make it through merges

		pack.addProvider(AllSoundEvents::provider);
		pack.addProvider(GeneratedEntriesProvider::new);
		pack.addProvider(CreateRecipeSerializerTagsProvider::new);
		pack.addProvider(CreateContraptionTypeTagsProvider::new);
		pack.addProvider(CreateMountedItemStorageTypeTagsProvider::new);
		pack.addProvider(DamageTypeTagGen::new);
		pack.addProvider(AllAdvancements::new);
		pack.addProvider(StandardRecipeGen::new);
		pack.addProvider(MechanicalCraftingRecipeGen::new);
		pack.addProvider(SequencedAssemblyRecipeGen::new);
		pack.addProvider((output, registries) -> ProcessingRecipeGen.registerAll(output, registries));
		pack.addProvider(CreateDatamapProvider::new);
		pack.addProvider(VanillaHatOffsetGenerator::new);
		pack.addProvider((output, registries) -> new CuriosDataGenerator(output, registries, null));
		pack.addProvider(CreateEnchantmentTagsProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		GeneratedEntriesProvider.addBootstraps(registryBuilder);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(langConsumer);
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			consumer.accept(key, value);
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		// Ponder 1.0.44 needs a client world to compile scenes; headless datagen has none,
		// so keep the previously generated ponder translations instead
		if (net.minecraft.client.Minecraft.getInstance().level == null) {
			Create.LOGGER.warn("No client world during datagen; preserving existing ponder lang");
			preserveExistingPonderLang(consumer);
			return;
		}

		// Register this since FMLClientSetupEvent does not run during datagen
		PonderIndex.addPlugin(new CreatePonderPlugin());

		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}

	private static void preserveExistingPonderLang(BiConsumer<String, String> consumer) {
		for (String base : new String[] {"src/generated/resources", "../src/generated/resources"}) {
			java.nio.file.Path path = java.nio.file.Path.of(base, "assets", "create", "lang", "en_us.json");
			if (!java.nio.file.Files.isRegularFile(path))
				continue;
			try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(path)) {
				com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
				for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : obj.entrySet()) {
					if (entry.getKey().startsWith("create.ponder.") || entry.getKey().startsWith("create.subtitle.")
							|| entry.getKey().startsWith("create.gui.goggles.") || entry.getKey().startsWith("create.generic."))
						consumer.accept(entry.getKey(), entry.getValue().getAsString());
				}
				return;
			} catch (Exception e) {
				Create.LOGGER.warn("Failed to read existing lang at {}", path, e);
			}
		}
	}
}
