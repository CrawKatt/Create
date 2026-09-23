// versions
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2024.11.17"
// https://fabricmc.net/develop/
val minecraftVersion = "1.21.1"
val loaderVersion = "0.16.10"
val fapiVersion = "0.116.1+1.21.1"

// in-house dependencies
val flywheelVersion = "1.0.6-44"
val ponderVersion = "1.0.82"
val registrateVersion = "1.3.77-MC1.21.1"
// last 3.x version that still publishes all modules this project uses
// (accessors, lazy_registration, extensions and asm were removed in later betas)
val portLibVersion = "3.1.0-beta.54+1.21.1"
val portLibModules = listOf(
	"accessors", "asm", "attributes", "base", "blocks", "brewing", "client_events", "common",
	"conditions", "config", "core", "data", "entity", "extensions", "fluids",
	"gui_utils", "item_abilities", "lazy_registration", "level_events",
	"mixin_extensions", "model_loader", "models", "obj_loader", "render_types", "tags", "transfer"
)

// external dependencies
val configApiVersion = "21.1.3"
val nightConfigVersion =  "3.6.3"
val jsr305Version = "3.0.2"

// compat
// https://modrinth.com/mod/cc-tweaked/versions
val ccVersion = "1.115.1"
// for CC - https://modrinth.com/mod/cloth-config/versions
val clothVersion = "15.0.140+fabric"
// https://modrinth.com/mod/jei/versions
val jeiVersion = "19.21.0.247"
// https://modrinth.com/mod/rei/versions
val reiVersion = "16.0.799"
// https://modrinth.com/mod/emi/versions
val emiVersion = "1.1.20+1.21.1"
// https://modrinth.com/mod/botania
val botaniaVersion = "1.19.2-436-FABRIC"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "11.0.3"
// https://modrinth.com/mod/sandwichable/versions
val sandwichableVersion = "1.3.1+1.20.1"
// https://modrinth.com/mod/sodium
val sodiumVersion = "mc1.21.1-0.6.9-fabric"
// https://github.com/emilyploszaj/trinkets/releases/
val trinketsVersion = "3.10.0"
// for Trinkets - https://modrinth.com/mod/cardinal-components-api/versions
val ccaVersion = "6.1.2"
// https://modrinth.com/mod/journeymap
val jmVersion = "1.21.1-6.0.0-beta.54+fabric"
// matches the journeymap-api JiJed inside the journeymap jar
val jmApiVersion = "2.0.0-1.21.1-SNAPSHOT"

// dev stuff
val ccRuntime = false
val recipeViewer = "emi" // jei, rei, or emi

plugins {
    id("fabric-loom") version "1.10.+"
    id("maven-publish")
}

val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "-build.$it" }
    .orElse("-local")
    .getOrElse("")
val modVersion = providers.gradleProperty("mod_version").get()

version = "$modVersion+mc$minecraftVersion$buildNum"

group = "com.simibubi.create"
base.archivesName = "create-fabric"

repositories {
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://maven.fabricmc.net") // FAPI, Loader
    maven("https://maven.createmod.net") // Ponder, Flywheel
    maven("https://mvn.devos.one/snapshots") // Registrate, Forge Tags, Milk Lib
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
    maven("https://maven.shedaniel.me") // REI and deps
    maven("https://api.modrinth.com/maven") { // LazyDFU, Sodium, Sandwichable
        content { includeGroupAndSubgroups("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com") // Mod Menu, Trinkets
    maven("https://maven.squiddev.cc") // CC:T
    maven("https://modmaven.dev") // Botania
    maven("https://maven.jamieswhiteshirt.com/libs-release") { // Reach Entity Attributes
        content { includeGroup("com.jamieswhiteshirt") }
    }
    maven("https://maven.ladysnake.org/releases") // CCA, for Trinkets
    maven("https://maven.ftb.dev/releases") // FTB
    maven("https://maven.ftb.dev/snapshots") { content { includeGroup("dev.ftb.mods") } }
    maven("https://jitpack.io") { content { includeModule("com.github.Nova-Committee", "ModernKeyBinding") } }
    maven("https://maven.architectury.dev") // Architectury API
    maven("https://jm.gserv.me/repository/maven-public/") // Journey map
}

val ponder = file("Ponder")
val journeyMapArchive = configurations.detachedConfiguration(
    dependencies.create("maven.modrinth:journeymap:$jmVersion")
).apply { isTransitive = false }
val journeyMapApiJar = zipTree(journeyMapArchive.singleFile)
    .matching { include("META-INF/jars/journeymap-api-fabric-$jmApiVersion.jar") }
    .singleFile

dependencies {
    // setup
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
    modImplementation(include("dev.engine-room.flywheel:flywheel-fabric-$minecraftVersion:$flywheelVersion")!!)

    // deprecated module still provides RenderAttachedBlockView used by our block models
    modCompileOnly("net.fabricmc.fabric-api:fabric-rendering-data-attachment-v1:0.3.48+73761d2e19")

    for (module in portLibModules) {
        modImplementation(include("io.github.fabricators_of_create.Porting-Lib:$module:$portLibVersion")!!)
    }

    modApi(include("com.tterrag.registrate_fabric:Registrate:$registrateVersion")!!)

    modApi(include("com.electronwill.night-config:core:$nightConfigVersion")!!)
    modApi(include("com.electronwill.night-config:toml:$nightConfigVersion")!!)
    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$configApiVersion")!!)
    modApi(include("dev.engine-room.flywheel:flywheel-fabric-api-$minecraftVersion:$flywheelVersion")!!)
    api(include("com.google.code.findbugs:jsr305:$jsr305Version")!!)

    if (ponder.exists()) {
        implementation("net.createmod.ponder:ponder-fabric:$ponderVersion+mc$minecraftVersion") { isTransitive = false }
		implementation("net.createmod.ponder:ponder-common:$ponderVersion+mc$minecraftVersion")
    } else {
        modRuntimeOnly("net.createmod.ponder:ponder-fabric:$ponderVersion+mc$minecraftVersion") { isTransitive = false }
        include("net.createmod.ponder:ponder-fabric:$ponderVersion+mc$minecraftVersion")
        modCompileOnly("net.createmod.ponder:ponder-fabric:$ponderVersion+mc$minecraftVersion") { isTransitive = false }
    }

    // compat
    modCompileOnly("cc.tweaked:cc-tweaked-$minecraftVersion-fabric-api:$ccVersion")

    modCompileOnly("com.terraformersmc:modmenu:$modmenuVersion")
    modCompileOnly("maven.modrinth:sodium:$sodiumVersion")

    modCompileOnly("dev.emi:trinkets:$trinketsVersion")
    // for Trinkets
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")

    // FIXME - Use gradle.properties for these versions, make change to concealed for this
    modCompileOnly("dev.architectury:architectury-fabric:13.0.6")
    modCompileOnly("dev.ftb.mods:ftb-chunks-fabric:2001.3.1")
    modCompileOnly("dev.ftb.mods:ftb-teams-fabric:2001.3.0")
    modCompileOnly("dev.ftb.mods:ftb-library-fabric:2001.2.4")
    modCompileOnly("com.github.Nova-Committee:ModernKeyBinding:17bf4f794ae3ce31aee90e0df67e2757c3533d10") { isTransitive = false }

    modCompileOnly("maven.modrinth:journeymap:$jmVersion")
    modCompileOnly(files(journeyMapApiJar))

    // EMI
    modCompileOnly("dev.emi:emi-fabric:$emiVersion:api") { isTransitive = false }
    // JEI
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") { isTransitive = false }
    // REI
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")

    when (recipeViewer) {
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "emi" -> modLocalRuntime("dev.emi:emi-fabric:$emiVersion")
    }

    // dev env
    modLocalRuntime("com.terraformersmc:modmenu:$modmenuVersion")
    modLocalRuntime("dev.emi:trinkets:$trinketsVersion") { isTransitive = false }
    // for Trinkets
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")
    if (ccRuntime) {
        modLocalRuntime("cc.tweaked:cc-tweaked-$minecraftVersion-fabric:$ccVersion")
        modLocalRuntime("maven.modrinth:cloth-config:$clothVersion")
    }
    // have deprecated modules present at runtime only
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api-deprecated:$fapiVersion")
}

sourceSets.named("main") {
    resources {
        srcDir("src/generated/resources")
        exclude(".cache/")
    }
}

loom {
    accessWidenerPath = file("src/main/resources/create.accesswidener")

    runs {
		register("datagen") {
			client()
			name("Data Generation")
			vmArg("-Dfabric-api.datagen")
			vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
			vmArg("-Dfabric-api.datagen.modid=create")
			property("porting_lib.datagen.existing_resources", file("src/main/resources").absolutePath)
		}

        register("gametestServer") {
            server()
            name("Headlesss GameTests")
            ideConfigGenerated(false) // this run is for CI
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory.file("junit.xml").get().asFile.absolutePath}")
            runDir("run/gametest")
        }

        named("server") {
            runDir("run/server")
        }

        configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            property("mixin.debug.export", "true")
        }
    }
}

configurations {
    configureEach {
        exclude(group = "io.github.fabricators_of_create.Porting-Lib", module = "gametest")
    }

    // this avoids remapping ponder when it's local
    named("runtimeClasspath") {
        attributes {
            attribute(Attribute.of("create.marker", String::class.java), "h")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("**/*.bbmodel", "**/*.lnk")

    val properties: MutableMap<String, Any> = mutableMapOf(
        "version" to version,
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion,
        "fabric_version" to fapiVersion,
        "forge_config_version" to configApiVersion,
        "port_lib_version" to portLibVersion,
    )

    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

java {
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}

tasks.register<JavaExec>("verifyKeybindingCompat") {
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.simibubi.create.foundation.mixin.compat.ModernKeyBindingCompatTest")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "create-fabric-$minecraftVersion"
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/releases") {
            name = "devOsReleases"
            credentials(PasswordCredentials::class)
        }

        maven("https://mvn.devos.one/snapshots") {
            name = "devOsSnapshots"
            credentials(PasswordCredentials::class)
        }
    }
}
