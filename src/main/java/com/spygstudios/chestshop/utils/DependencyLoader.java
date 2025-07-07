package com.spygstudios.chestshop.utils;

import java.io.InputStreamReader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

public class DependencyLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        try {
            YamlConfiguration pluginYaml = YamlConfiguration.loadConfiguration(new InputStreamReader(DependencyLoader.class.getResourceAsStream("/plugin.yml")));
            pluginYaml.getStringList("sona-libraries").forEach(lib -> {
                resolver.addDependency(new Dependency(new DefaultArtifact(lib), null));
            });
        } catch (Exception e) {
            resolver.addDependency(new Dependency(new DefaultArtifact("com.spygstudios:spyglib:1.2.3"), null));
            resolver.addDependency(new Dependency(new DefaultArtifact("org.xerial:sqlite-jdbc:3.50.2.0"), null));
            resolver.addDependency(new Dependency(new DefaultArtifact("com.mysql:mysql-connector-j:9.3.0"), null));
            e.printStackTrace();
        }

        resolver.addRepository(new RemoteRepository.Builder("oss-central", "default", "https://oss.sonatype.org/content/groups/public/").build());

        classpathBuilder.addLibrary(resolver);
    }
}
