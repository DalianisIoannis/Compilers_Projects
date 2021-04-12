//This program counts the occurrences of every word within a text file (program argument).
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;

class Count {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }
    
        Map<String, Integer> table = new HashMap<String, Integer>();
        Map<Character, Set<String>> charToWords = new TreeMap<Character, Set<String>>(); 

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(args[0])));

            String line;
            while ((line = in.readLine()) != null) {
                // Strip punctuation and convert to lowercase
                line = line.replaceAll("[^a-zA-Z 0-9]", "").toLowerCase();

                // Split text into words
                String[] words = line.split(" ");
                for (int i = 0 ; i != words.length ; ++i) {
                    Integer count;
		    
                    if(!table.containsKey(words[i])){
                        count = 1;
                    }
                    else{
                        count = table.get(words[i]) + 1;
                    }

                    table.put(words[i], count);

                    for (char c: words[i].toCharArray()) {
                        Set<String> wordsWithChar = charToWords.getOrDefault(c, new HashSet<String>());
                        wordsWithChar.add(words[i]);

                        if (wordsWithChar.size() == 1)
                            charToWords.put(c, wordsWithChar);
                    }
                }
            }
        } 
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        catch (IOException e){
            System.err.println(e.getMessage());
            System.exit(2);
        }
        finally{
            try{
                in.close();
            }
            catch(IOException e){
                System.err.println(e.getMessage());
                System.exit(2);
            }
        }

        System.out.println("---Word Frequencies---");
        for (String word : table.keySet())
            System.out.println(word + ": " + table.get(word));

        System.out.println("\n\n\n---Words containing char---");

        for(Map.Entry<Character, Set<String>> entry : charToWords.entrySet()) {
            Character key = entry.getKey();
            Set<String> wordSet = entry.getValue();

            System.out.printf("%c: %s\n", key, wordSet.toString());

        }
    }
}
