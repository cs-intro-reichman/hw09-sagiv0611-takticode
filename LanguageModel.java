import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);

        // Reads just enough characters to form the first window
        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
        }

        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();

            // Checks if the window is already in the map
            List probs = CharDataMap.get(window);

            // If the window was not found in the map
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }

            // Calculates the counts of the current character
            probs.update(c);

            // Advances the window
            window = window.substring(1) + c;
        }

        // Computes and sets the probabilities (p and cp fields) of all the
        // characters in the given list.
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		int totalChars = 0;
        Node current = probs.first;
        
        // Step 1: Calculate the total number of characters in the list
        while (current != null) {
            totalChars += current.cp.count;
            current = current.next;
        }

        // Step 2: Calculate p and cp for each character
        current = probs.first;
        double cumulativeProbability = 0.0;
        
        while (current != null) {
            current.cp.p = (double) current.cp.count / totalChars;
            cumulativeProbability += current.cp.p;
            current.cp.cp = cumulativeProbability;
            
            current = current.next;
        }
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        Node current = probs.first;
        while (current != null) {
            if (r < current.cp.cp) {
                return current.cp.chr;
            }
            current = current.next;
        }
		return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }
        
        String generatedText = initialText;
        String window = "";
        
        for (int i = 0; i < textLength; i++) {
            window = generatedText.substring(generatedText.length() - windowLength);
            List probs = CharDataMap.get(window);
            
            if (probs == null) {
                break;
            }
            
            Node current = probs.first;
            double r = randomGenerator.nextDouble();
            
            while (current != null) {
                if (r < current.cp.cp) {
                    generatedText += current.cp.chr;
                    break;
                }
                current = current.next;
            }
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
	int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        String fileName = args[3];
        LanguageModel lm;
        
        if (args.length == 5) {
            lm = new LanguageModel(windowLength, Integer.parseInt(args[4]));
        } else {
            lm = new LanguageModel(windowLength);
        }

        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
