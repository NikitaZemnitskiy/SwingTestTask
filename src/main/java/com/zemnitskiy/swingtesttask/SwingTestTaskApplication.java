package com.zemnitskiy.swingtesttask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Main application class for the Swing Test Task.
 * <p>
 * This application allows users to generate random numbers, display them as buttons,
 * and sort them with an animated QuickSort algorithm.
 * </p>
 */
public class SwingTestTaskApplication extends JFrame {

    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 30;
    private static final Dimension BUTTON_SIZE = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
    private static final String FONT_NAME = "Arial";
    private static final int FONT_STYLE = Font.BOLD;
    private static final int FONT_SIZE = 14;
    private static final Font DEFAULT_FONT = new Font(FONT_NAME, FONT_STYLE, FONT_SIZE);
    private static final Font LABEL_FONT = new Font(FONT_NAME, FONT_STYLE, 16);

    private static final int SORT_DELAY_MS = 50;
    private static final int MAX_VALUE_FOR_SPECIAL_BUTTON = 30;
    private static final int MAX_BUTTONS_PER_COLUMN = 10;

    private static final String CARD_INTRO = "Intro";
    private static final String CARD_SORT = "Sort";

    private static final Color BUTTON_COLOR = new Color(59, 89, 182);
    private static final Color HIGHLIGHT_COLOR = Color.YELLOW;

    private final Random randomGenerator = new Random();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private JPanel numbersPanel;
    private JButton sortButton;
    private List<Integer> numbers;
    private int numberCount;

    // Sorting flag: false – descending, true – ascending; toggles after each "Sort" click
    private boolean sortAscending = false;

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingTestTaskApplication().setVisible(true));
    }

    /**
     * Constructs the SwingTestTaskApplication and initializes the UI.
     */
    public SwingTestTaskApplication() {
        initFrame();
        initUI();
    }

    /**
     * Initializes the main frame settings.
     */
    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Initializes the user interface by setting up panels and the card layout.
     */
    private void initUI() {
        JPanel introPanel = createIntroScreen();
        mainPanel.add(introPanel, CARD_INTRO);

        JPanel sortPanel = createSortScreen();
        mainPanel.add(sortPanel, CARD_SORT);

        add(mainPanel);
        cardLayout.show(mainPanel, CARD_INTRO);
    }

    /**
     * Creates the introductory screen with an input field and an "Enter" button.
     *
     * @return the JPanel representing the introductory screen.
     */
    private JPanel createIntroScreen() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);

        final JLabel promptLabel = new JLabel("How many numbers to display?");
        promptLabel.setFont(LABEL_FONT);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel.add(promptLabel, gridBagConstraints);

        final JTextField inputField = new JTextField(10);
        gridBagConstraints.gridy = 1;
        panel.add(inputField, gridBagConstraints);

        final JButton enterButton = createConfiguredButton("Enter");
        gridBagConstraints.gridy = 2;
        panel.add(enterButton, gridBagConstraints);

        enterButton.addActionListener(e -> {
            try {
                numberCount = Integer.parseInt(inputField.getText());
                if (numberCount <= 0) {
                    showError("Please enter a positive number.");
                    return;
                }
                generateNumbers();
                cardLayout.show(mainPanel, CARD_SORT);
            } catch (NumberFormatException ex) {
                showError("Please enter a valid number.");
            }
        });

        return panel;
    }

    /**
     * Creates the sorting screen with number buttons and control buttons ("Sort" and "Reset").
     *
     * @return the JPanel representing the sorting screen.
     */
    private JPanel createSortScreen() {
        JPanel panel = new JPanel(new BorderLayout());

        numbersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JScrollPane scrollPane = new JScrollPane(numbersPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        sortButton = createConfiguredButton("Sort");
        JButton resetButton = createConfiguredButton("Reset");

        controlPanel.add(sortButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(resetButton);

        panel.add(controlPanel, BorderLayout.EAST);

        sortButton.addActionListener(e -> startSortAnimation());
        resetButton.addActionListener(e -> cardLayout.show(mainPanel, CARD_INTRO));

        return panel;
    }

    /**
     * Generates a list of random numbers based on the provided count.
     */
    private void generateNumbers() {
        numbers = new ArrayList<>();
        // Generate (numberCount - 1) numbers, each greater or equal to MAX_VALUE_FOR_SPECIAL_BUTTON.
        for (int i = 0; i < numberCount - 1; i++) {
            int randomNumber = randomGenerator.nextInt(1000 - MAX_VALUE_FOR_SPECIAL_BUTTON + 1)
                    + MAX_VALUE_FOR_SPECIAL_BUTTON;
            numbers.add(randomNumber);
        }
        // Ensure at least one number is less than or equal to MAX_VALUE_FOR_SPECIAL_BUTTON.
        numbers.add(randomGenerator.nextInt(MAX_VALUE_FOR_SPECIAL_BUTTON) + 1);
        Collections.shuffle(numbers);
        refreshNumbersPanel();
    }

    /**
     * Refreshes the numbers panel by arranging buttons in columns.
     */
    private void refreshNumbersPanel() {
        numbersPanel.removeAll();
        int totalNumbers = numbers.size();
        int columns = (int) Math.ceil(totalNumbers / (double) MAX_BUTTONS_PER_COLUMN);
        int index = 0;

        for (int col = 0; col < columns; col++) {
            JPanel columnPanel = new JPanel();
            columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));

            for (int row = 0; row < MAX_BUTTONS_PER_COLUMN && index < totalNumbers; row++) {
                JButton numberButton = createNumberButton(index);
                columnPanel.add(numberButton);
                columnPanel.add(Box.createVerticalStrut(5));
                index++;
            }
            numbersPanel.add(columnPanel);
        }
        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    /**
     * Creates a button representing a number based on its index.
     *
     * @param index the index of the number in the list.
     * @return the configured JButton displaying the number.
     */
    private JButton createNumberButton(int index) {
        int value = numbers.get(index);
        JButton button = createConfiguredButton(String.valueOf(value));

        button.addActionListener(e -> {
            if (value <= MAX_VALUE_FOR_SPECIAL_BUTTON) {
                // Set the new count based on the button's value and regenerate numbers.
                numberCount = value;
                generateNumbers();
            } else {
                showError("Please select a value smaller or equal to " + MAX_VALUE_FOR_SPECIAL_BUTTON);
            }
        });
        return button;
    }

    /**
     * Starts the sorting animation using the QuickSort algorithm.
     */
    private void startSortAnimation() {
        sortButton.setEnabled(false);

        SwingWorker<Void, Void> sorter = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SortingException {
                try {
                    quickSort(0, numbers.size() - 1, sortAscending);
                } catch (Exception ex) {
                    throw new SortingException("An error occurred during sorting.", ex);
                }
                return null;
            }

            @Override
            protected void done() {
                // Toggle sorting order for the next sort.
                sortAscending = !sortAscending;
                sortButton.setEnabled(true);
            }
        };
        sorter.execute();
    }

    /**
     * Implements the recursive QuickSort algorithm.
     *
     * @param low       the starting index of the partition.
     * @param high      the ending index of the partition.
     * @param ascending whether to sort in ascending order.
     * @throws Exception if an error occurs during sorting.
     */
    private void quickSort(int low, int high, boolean ascending) throws Exception {
        if (low < high) {
            int partitionIndex = partition(low, high, ascending);
            quickSort(low, partitionIndex - 1, ascending);
            quickSort(partitionIndex + 1, high, ascending);
        }
    }

    /**
     * Partitions the list for QuickSort using the median-of-three pivot selection.
     *
     * @param low       the starting index.
     * @param high      the ending index.
     * @param ascending whether to sort in ascending order.
     * @return the partition index.
     * @throws Exception if an error occurs during partitioning.
     */
    private int partition(int low, int high, boolean ascending) throws Exception {
        int pivotIndex = selectPivotIndex(low, high);
        Collections.swap(numbers, pivotIndex, high);
        updateUIWithDelay();

        int pivotValue = numbers.get(high);
        int smallerElementIndex  = low - 1;

        for (int j = low; j < high; j++) {
            if ((ascending && numbers.get(j) < pivotValue) ||
                    (!ascending && numbers.get(j) > pivotValue)) {
                smallerElementIndex ++;
                highlightButtons(smallerElementIndex , j);
                Collections.swap(numbers, smallerElementIndex , j);
                updateUIWithDelay();
            }
        }
        Collections.swap(numbers, smallerElementIndex  + 1, high);
        updateUIWithDelay();

        return smallerElementIndex  + 1;
    }

    /**
     * Selects the pivot index using the median-of-three method.
     *
     * @param low  the starting index.
     * @param high the ending index.
     * @return the selected pivot index.
     */
    private int selectPivotIndex(int low, int high) {
        int mid = low + (high - low) / 2;
        int first = numbers.get(low);
        int middle = numbers.get(mid);
        int last = numbers.get(high);

        // Determine the median value among the three
        if ((first <= middle && middle <= last) || (last <= middle && middle <= first)) {
            return mid;
        } else if ((middle <= first && first <= last) || (last <= first && first <= middle)) {
            return low;
        } else {
            return high;
        }
    }

    /**
     * Highlights the buttons involved in the sorting process.
     *
     * @param index1 the first button index.
     * @param index2 the second button index.
     */
    private void highlightButtons(int index1, int index2) {
        resetButtonColors();
        final JButton button1 = getButtonByIndex(index1);
        final JButton button2 = getButtonByIndex(index2);

        if (button1 != null) {
            button1.setBackground(HIGHLIGHT_COLOR);
        }
        if (button2 != null) {
            button2.setBackground(HIGHLIGHT_COLOR);
        }

        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    /**
     * Resets all button colors to the default BUTTON_COLOR.
     */
    private void resetButtonColors() {
        for (JButton button : getAllButtons()) {
            button.setBackground(BUTTON_COLOR);
        }
    }

    /**
     * Retrieves all buttons from the numbers panel.
     *
     * @return a List of all JButtons in the numbers panel.
     */
    private List<JButton> getAllButtons() {
        final List<JButton> buttons = new ArrayList<>();
        final Component[] components = numbersPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel jPanel) {
                for (Component child : jPanel.getComponents()) {
                    if (child instanceof JButton jButton) {
                        buttons.add(jButton);
                    }
                }
            }
        }
        return buttons;
    }

    /**
     * Retrieves a button by its index from the numbers panel.
     *
     * @param targetIndex the index of the button.
     * @return the JButton if found, otherwise {@code null}.
     */
    private JButton getButtonByIndex(int targetIndex) {
        final List<JButton> buttons = getAllButtons();
        return (targetIndex >= 0 && targetIndex < buttons.size()) ? buttons.get(targetIndex) : null;
    }

    /**
     * Updates the UI and introduces a delay for the sorting animation.
     *
     * @throws Exception if an error occurs during the UI update.
     */
    private void updateUIWithDelay() throws Exception {
        SwingUtilities.invokeAndWait(this::refreshNumbersPanel);
        Thread.sleep(SORT_DELAY_MS);
    }

    /**
     * Creates and configures a JButton with the specified text.
     *
     * @param text the text to display on the button.
     * @return the configured JButton.
     */
    private JButton createConfiguredButton(String text) {
        final JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMinimumSize(BUTTON_SIZE);
        button.setPreferredSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(DEFAULT_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR));
        return button;
    }

    /**
     * Displays an error message in a dialog.
     *
     * @param message the error message to display.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Custom exception class for errors that occur during sorting.
     */
    private static class SortingException extends Exception {
        /**
         * Constructs a new SortingException with the specified detail message and cause.
         *
         * @param message the detail message.
         * @param cause   the underlying cause of the exception.
         */
        public SortingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}