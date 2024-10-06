package org.lightsout;

import org.lightsout.game.Game;

import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LightsOut {

    private static final Logger LOGGER = Logger.getLogger(LightsOut.class.getName());

    public static void main(String[] args) {
        ClassLoader classLoader = LightsOut.class.getClassLoader();
        String line1 = "";
        String line2 = "";
        String line3 = "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(classLoader.getResourceAsStream("02.txt")), StandardCharsets.UTF_8))) {
            line1 = reader.readLine();
            line2 = reader.readLine();
            line3 = reader.readLine();
        } catch (IOException e) {
            LOGGER.severe("Could not read file: " + e);
            System.exit(1);
        }

        Game game = new Game(line1, line2, line3);
        game.solve();
    }

}