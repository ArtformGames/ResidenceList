package com.artformgames.plugin.residencelist.utils;

import cc.carm.lib.githubreleases4j.GithubReleases4J;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GHUpdateChecker {

    protected static final @NotNull Pattern GH_URL_PATTERN = Pattern.compile("^https?://github.com/([A-Za-z\\d-_]+)/([^/]*?)/?");

    public static @NotNull GHUpdateChecker of(@NotNull Logger logger, @NotNull String owner, @NotNull String repo) {
        return new GHUpdateChecker(logger, owner, repo);
    }

    public static @NotNull GHUpdateChecker of(@NotNull Plugin plugin) {
        return new GHUpdateChecker(plugin.getLogger(), getGithubOwner(plugin), plugin.getName());
    }

    public static @NotNull Runnable runner(@NotNull Logger logger,
                                           @NotNull String owner, @NotNull String repo,
                                           @NotNull String currentVersion) {
        return of(logger, owner, repo).createRunner(currentVersion);
    }

    public static @NotNull Runnable runner(@NotNull Plugin plugin) {
        return of(plugin).createRunner(plugin.getDescription().getVersion());
    }

    public static @NotNull BukkitTask run(@NotNull Plugin plugin) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runner(plugin));
    }

    protected final @NotNull Logger logger;
    protected final @NotNull String owner;
    protected final @NotNull String repo;

    public GHUpdateChecker(@NotNull Logger logger, @NotNull String owner, @NotNull String repo) {
        this.logger = logger;
        this.owner = owner;
        this.repo = repo;
    }

    public void checkUpdate(@NotNull String currentVersion) {
        Integer behindVersions = GithubReleases4J.getVersionBehind(owner, repo, currentVersion);
        String downloadURL = GithubReleases4J.getReleasesURL(owner, repo);

        if (behindVersions == null) {
            this.logger.severe("Failed to check for updates, please check regularly to see if the plugin is updated to avoid security issues.");
            this.logger.severe("Download at -> " + downloadURL);
        } else if (behindVersions == 0) {
            this.logger.info("Now is up-to-date");
        } else if (behindVersions > 0) {
            this.logger.info("New version found! Now is behind " + behindVersions + " version(s).");
            this.logger.info("Download at -> " + downloadURL);
        } else {
            this.logger.severe("Checking for updates failed! The current version is unknown, please use the native version to avoid security issues.");
            this.logger.severe("Download at -> " + downloadURL);
        }
    }

    public Runnable createRunner(@NotNull String currentVersion) {
        return () -> checkUpdate(currentVersion);
    }

    protected static String getGithubOwner(Plugin plugin) {
        String websiteOwner = getGithubOwner(plugin.getDescription().getWebsite());
        if (websiteOwner != null && !websiteOwner.isEmpty()) return websiteOwner;
        List<String> authors = plugin.getDescription().getAuthors();
        if (!authors.isEmpty()) return authors.get(0);
        return plugin.getName();
    }

    protected static String getGithubOwner(String url) {
        Matcher matcher = GH_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

}