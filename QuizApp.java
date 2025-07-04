import java.io.BufferedReader; 
import java.io.FileReader;    
import java.io.IOException;   
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class QuizApp {

    private static final String QUESTIONS_FILE = "questions.txt"; 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Question> questions = new ArrayList<>();
        int score = 0;

        System.out.println("--- Welcome to the Java Quiz! ---");

        boolean loaded = loadQuestionsFromFile(questions);

        if (!loaded || questions.isEmpty()) {
            System.out.println("Failed to load questions from " + QUESTIONS_FILE + " or file is empty.");
            System.out.println("Please ensure '" + QUESTIONS_FILE + "' exists in the same directory and is formatted correctly.");
            scanner.close();
            return; 
        }

        System.out.println("\nSuccessfully loaded " + questions.size() + " questions from " + QUESTIONS_FILE + ".");
        System.out.println("Answer the questions by typing the number corresponding to your choice.");

        for (int i = 0; i < questions.size(); i++) {
            Question currentQuestion = questions.get(i);
            currentQuestion.displayQuestion();

            int userAnswer = -1;
            boolean validInput = false;

            while (!validInput) {
                System.out.print("Your answer (1-" + currentQuestion.getOptions().size() + "): ");
                if (scanner.hasNextInt()) {
                    userAnswer = scanner.nextInt();
                    scanner.nextLine(); 
                    if (userAnswer >= 1 && userAnswer <= currentQuestion.getOptions().size()) {
                        validInput = true;
                    } else {
                        System.out.println("Invalid input. Please enter a number between 1 and " + currentQuestion.getOptions().size() + ".");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next(); 
                    scanner.nextLine(); 
                }
            }

            if (currentQuestion.checkAnswer(userAnswer)) {
                System.out.println("Correct!");
                score++;
            } else {
                System.out.println("Incorrect. The correct answer was: " + (currentQuestion.getCorrectOptionIndex() + 1) + ". " + currentQuestion.getOptions().get(currentQuestion.getCorrectOptionIndex()));
            }
        }

        System.out.println("\n--- Quiz Finished! ---");
        System.out.println("You answered " + score + " out of " + questions.size() + " questions correctly.");
        double percentage = (double) score / questions.size() * 100;
        System.out.printf("Your score: %.2f%%\n", percentage);

        scanner.close();
    }

    private static boolean loadQuestionsFromFile(List<Question> questions) {
        try (BufferedReader reader = new BufferedReader(new FileReader(QUESTIONS_FILE))) {
            String line;
            String questionText = null;
            List<String> options = new ArrayList<>();
            int correctOptionIndex = -1;
            int lineCount = 0; 
            while ((line = reader.readLine()) != null) {
                line = line.trim(); 
                lineCount++;

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equals("---")) {
                    if (questionText != null && !options.isEmpty() && correctOptionIndex != -1) {
                        questions.add(new Question(questionText, new ArrayList<>(options), correctOptionIndex - 1));
                    } else {
                        System.err.println("Warning: Incomplete question block detected before '---' at line " + lineCount);
                    }
                    questionText = null;
                    options.clear();
                    correctOptionIndex = -1;
                    continue;
                }

                if (questionText == null) {
                    questionText = line;
                } else if (line.matches("\\d+")) {
                    try {
                        correctOptionIndex = Integer.parseInt(line);
                        if (correctOptionIndex < 1 || correctOptionIndex > options.size()) {
                             System.err.println("Warning: Correct option index " + correctOptionIndex + " out of bounds for " + options.size() + " options at line " + lineCount + " (Question: " + questionText + "). This question might be malformed.");
                             correctOptionIndex = -1; 
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid number format for correct option index at line " + lineCount + ": " + line);
                        correctOptionIndex = -1; 
                    }
                } else {
                    options.add(line);
                }
            }
            if (questionText != null && !options.isEmpty() && correctOptionIndex != -1) {
                questions.add(new Question(questionText, new ArrayList<>(options), correctOptionIndex - 1));
            }

        } catch (IOException e) {
            System.err.println("Error reading questions file: " + e.getMessage());
            return false;
        } catch (Exception e) { 
            System.err.println("An unexpected error occurred while parsing questions file: " + e.getMessage());
            return false;
        }
        return true;
    }
}