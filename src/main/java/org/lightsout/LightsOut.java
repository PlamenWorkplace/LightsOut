package org.lightsout;

import org.lightsout.game.Game;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LightsOut {

    private static final Logger LOGGER = Logger.getLogger(LightsOut.class.getName());
    // set programmatically so reviewer doesn't have to set VM arguments manually.
    private static final Level LOG_LEVEL = Level.WARNING;

    public static void main(String[] args) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(LOG_LEVEL);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(LOG_LEVEL);
        }

        ClassLoader classLoader = LightsOut.class.getClassLoader();
        String line1;
        String line2;
        String line3;

        // The 9th puzzle has a 10-second delay before getting solved, although the delay
        // largely depends on which puzzle piece you start with (it can fluctuate between 2-20 seconds).
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(classLoader.getResourceAsStream("games/01.txt")), StandardCharsets.UTF_8))) {
            line1 = reader.readLine();
            line2 = reader.readLine();
            line3 = reader.readLine();
        } catch (IOException e) {
            LOGGER.severe("Could not read file: " + e);
            return;
        }

        Game game = new Game(line1, line2, line3);
        game.solve();
    }

}