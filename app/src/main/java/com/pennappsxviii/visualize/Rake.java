package com.pennappsxviii.visualize;

import android.annotation.TargetApi;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Rapid Automatic Keyword Extraction (RAKE)
 * =========================================
 *
 * Rose, Stuart & Engel, Dave & Cramer, Nick & Cowley, Wendy. (2010).
 * Automatic Keyword Extraction from Individual Documents.
 * Text Mining: Applications and Theory. 1 - 20. 10.1002/9780470689646.ch1.
 *
 * Implementation based on https://github.com/aneesha/RAKE
 */
@TargetApi(26)
public class Rake {
    String language;
    String stopWordsPattern;

    Rake(String language) {
        this.language = language;
        String[] stopWordsArray = new String[]{
                "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at",
                "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot",
                "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each",
                "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he",
                "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's",
                "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's",
                "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
                "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd",
                "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their",
                "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're",
                "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd",
                "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which",
                "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll",
                "you're", "you've", "your", "yours", "yourself", "yourselves"
        };

        ArrayList<String> stopWords = new ArrayList<String>(Arrays.asList(stopWordsArray));
        ArrayList<String> regexList = new ArrayList<>();
        // Turn the stop words into an array of regex
        for (String word : stopWords) {
            String regex = "\\b" + word + "(?![\\w-])";
            regexList.add(regex);
        }
        // Join all regexes into global pattern
        this.stopWordsPattern = String.join("|", regexList);
    }

    /**
     * Returns a list of all sentences in a given string of text
     *
     * @param text
     * @return String[]
     */
    private String[] getSentences(String text) {
        return text.split("[.!?,;:\\t\\\\\\\\\"\\\\(\\\\)\\\\'\\u2019\\u2013]|\\\\s\\\\-\\\\s");
    }

    /**
     * Returns a list of all words that are have a length greater than a specified number of characters
     *
     * @param text given text
     * @param size minimum size
     */
    private String[] separateWords(String text, int size) {
        String[] split = text.split("[^a-zA-Z0-9_\\\\+/-\\\\]");
        ArrayList<String> words = new ArrayList<>();

        for (String word : split) {
            String current = word.trim().toLowerCase();
            int len = current.length();

            if (len > size && len > 0 && !StringUtils.isNumeric(current))
                words.add(current);
        }

        return words.toArray(new String[words.size()]);
    }

    /**
     * Generates a list of keywords by splitting sentences by their stop words
     *
     * @param sentences
     * @return
     */
    private String[] getKeywords(String[] sentences) {
        ArrayList<String> phraseList = new ArrayList<>();

        for (String sentence : sentences) {
            String temp = sentence.trim().replaceAll(this.stopWordsPattern, "|");
            String[] phrases = temp.split("\\|");

            for (String phrase : phrases) {
                phrase = phrase.trim().toLowerCase();

                if (phrase.length() > 0)
                    phraseList.add(phrase);
            }
        }

        return phraseList.toArray(new String[phraseList.size()]);
    }

    /**
     * Calculates word scores for each word in a collection of phrases
     * <p>
     * Scores is calculated by dividing the word degree (collective length of phrases the word appears in)
     * by the number of times the word appears
     *
     * @param phrases
     * @return
     */
    private LinkedHashMap<String, Double> calculateWordScores(String[] phrases) {
        LinkedHashMap<String, Integer> wordFrequencies = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> wordDegrees = new LinkedHashMap<>();
        LinkedHashMap<String, Double> wordScores = new LinkedHashMap<>();

        for (String phrase : phrases) {
            String[] words = this.separateWords(phrase, 0);
            int length = words.length;
            int degree = length - 1;

            for (String word : words) {
                wordFrequencies.put(word, wordDegrees.getOrDefault(word, 0) + 1);
                wordDegrees.put(word, wordFrequencies.getOrDefault(word, 0) + degree);
            }
        }

        for (String item : wordFrequencies.keySet()) {
            wordDegrees.put(item, wordDegrees.get(item) + wordFrequencies.get(item));
            wordScores.put(item, wordDegrees.get(item) / (wordFrequencies.get(item) * 1.0));
        }

        return wordScores;
    }

    /**
     * Returns a list of keyword candidates and their respective word scores
     *
     * @param phrases
     * @param wordScores
     * @return
     */
    private LinkedHashMap<String, Double> getCandidateKeywordScores(String[] phrases, LinkedHashMap<String, Double> wordScores) {
        LinkedHashMap<String, Double> keywordCandidates = new LinkedHashMap<>();

        for (String phrase : phrases) {
            double score = 0.0;

            String[] words = this.separateWords(phrase, 0);

            for (String word : words) {
                score += wordScores.get(word);
            }

            keywordCandidates.put(phrase, score);
        }

        return keywordCandidates;
    }

    /**
     * Sorts a LinkedHashMap by value from lowest to highest
     *
     * @param map
     * @return
     */
    private LinkedHashMap<String, Double> sortHashMap(LinkedHashMap<String, Double> map) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(list);

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Double> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Extracts keywords from the given text body using the RAKE algorithm
     *
     * @param text
     */
    public LinkedHashMap<String, Double> getKeywordsFromText(String text) {
        String[] sentences = this.getSentences(text);
        String[] keywords = this.getKeywords(sentences);

        LinkedHashMap<String, Double> wordScores = this.calculateWordScores(keywords);
        LinkedHashMap<String, Double> keywordCandidates = this.getCandidateKeywordScores(keywords, wordScores);

        return this.sortHashMap(keywordCandidates);
    }
}