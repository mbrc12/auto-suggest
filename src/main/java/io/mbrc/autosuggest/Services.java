package io.mbrc.autosuggest;

import io.mbrc.autosuggest.encoder.Soundex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static io.mbrc.autosuggest.Util.*;

public class Services {

    static Logger log = LoggerFactory.getLogger(Services.class);

    public static Function<String, String> hashFunction () {
        Soundex soundex = new Soundex();
        return string -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char current = string.charAt(i);
                if (isAlphabet(current))
                    builder.append(current);
            }

            return soundex.encode(builder.toString());
        };
    }

    public static
    Predicate<String> ignorableChecker () {
        try {
            final Set<String> ignoredWords = new HashSet<>();
            Logger log = LoggerFactory.getLogger("ignorableChecker");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new ClassPathResource("low_info_words.txt").getInputStream()));

            while (true) {
                try {
                    String token = reader.readLine().strip().toLowerCase();
                    ignoredWords.add(token);
                    log.debug("Ignored word: {}", token);
                } catch (Exception e) {
                    break;
                }
            }
//        String contents = Files.readString(
//                Path.of(new ClassPathResource("low_info_words.txt")
//                        .getFile()
//                        .getAbsolutePath()));
//
//        StringTokenizer tokens = new StringTokenizer(contents);
//        while (tokens.hasMoreTokens()) {
//            ignoredWords.add(tokens.nextToken().toLowerCase());
//        }

            final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

            return word -> {
                if (ignoredWords.contains(word)) return true;
                return pattern.matcher(word).matches();
            };
        } catch (IOException e) {
            log.error("Could not find low_info_words.txt.");
            e.printStackTrace();
            throw new RuntimeException("Could not find low_info_words.txt");
        }
    }


    public static
    String splitDelimiters = ",.;'\"|:-!@_=\\[]{}()<>?~`&*=+/ ";

//        StringBuilder stringBuilder = new StringBuilder();
//        for (char c = 0; c < 255; c++) {
//            if (isAlphabet(c) || Character.isDigit(c))
//                continue;
//            stringBuilder.append(c);
//        }
//        return stringBuilder.toString();
}
