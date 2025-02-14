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
 * This application generates random numbers, displays them as buttons,
 * and animates an in-place QuickSort algorithm.
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
    private boolean sortAscending = false;

    // Current sorting SwingWorker (if any)
    private SwingWorker<Void, Void> sortingWorker;

    /**
     * Launches the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingTestTaskApplication().setVisible(true));
    }

    /**
     * Constructs the application and initializes the UI.
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
     * Initializes the UI panels and card layout.
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
     * @return the introductory JPanel.
     */
    private JPanel createIntroScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel promptLabel = new JLabel("How many numbers to display?");
        promptLabel.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(promptLabel, gbc);

        JTextField inputField = new JTextField(10);
        gbc.gridy = 1;
        panel.add(inputField, gbc);

        JButton enterButton = createConfiguredButton("Enter");
        gbc.gridy = 2;
        panel.add(enterButton, gbc);

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
     * Creates the sorting screen with number buttons and "Sort" and "Reset" controls.
     *
     * @return the sorting JPanel.
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
        resetButton.addActionListener(e -> {
            if (sortingWorker != null && !sortingWorker.isDone()) {
                sortingWorker.cancel(true);
            }
            resetButtonColors();
            cardLayout.show(mainPanel, CARD_INTRO);
        });

        return panel;
    }

    /**
     * Generates a list of random numbers and updates the display.
     */
    private void generateNumbers() {
        numbers = new ArrayList<>();
        for (int i = 0; i < numberCount - 1; i++) {
            int randomNumber = randomGenerator.nextInt(1000 - MAX_VALUE_FOR_SPECIAL_BUTTON + 1)
                    + MAX_VALUE_FOR_SPECIAL_BUTTON;
            numbers.add(randomNumber);
        }
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
     * Creates a button representing a number.
     *
     * @param index the index in the numbers list.
     * @return the configured JButton.
     */
    private JButton createNumberButton(int index) {
        int value = numbers.get(index);
        JButton button = createConfiguredButton(String.valueOf(value));
        button.addActionListener(e -> {
            if (value <= MAX_VALUE_FOR_SPECIAL_BUTTON) {
                numberCount = value;
                generateNumbers();
            } else {
                showError("Please select a value smaller or equal to " + MAX_VALUE_FOR_SPECIAL_BUTTON);
            }
        });
        return button;
    }

    /**
     * Starts the animated QuickSort.
     */
    private void startSortAnimation() {
        sortButton.setEnabled(false);
        sortingWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    quickSort(0, numbers.size() - 1, sortAscending);
                } catch (InterruptedException ie) {
                    // Sorting was cancelled.
                }
                return null;
            }

            @Override
            protected void done() {
                sortButton.setEnabled(true);
                if (isCancelled()) {
                    SwingUtilities.invokeLater(() -> {
                        resetButtonColors();
                        refreshNumbersPanel();
                    });
                } else {
                    sortAscending = !sortAscending;
                }
            }
        };
        sortingWorker.execute();
    }

    /**
     * Recursively performs QuickSort.
     *
     * @param low       the starting index.
     * @param high      the ending index.
     * @param ascending sort order flag.
     * @throws Exception if an error occurs.
     */
    private void quickSort(int low, int high, boolean ascending) throws Exception {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Sorting cancelled.");
        }
        if (low < high) {
            int partitionIndex = partition(low, high, ascending);
            quickSort(low, partitionIndex - 1, ascending);
            quickSort(partitionIndex + 1, high, ascending);
        }
    }

    /**
     * Partitions the list using the median-of-three pivot selection.
     *
     * @param low       the starting index.
     * @param high      the ending index.
     * @param ascending sort order flag.
     * @return the partition index.
     * @throws Exception if an error occurs.
     */
    private int partition(int low, int high, boolean ascending) throws Exception {
        int pivotIndex = selectPivotIndex(low, high);
        highlightAndSwap(pivotIndex, high);

        int pivotValue = numbers.get(high);
        int smallerElementIndex = low - 1;
        for (int j = low; j < high; j++) {
            if ((ascending && numbers.get(j) < pivotValue) ||
                    (!ascending && numbers.get(j) > pivotValue)) {
                smallerElementIndex++;
                highlightAndSwap(smallerElementIndex, j);
            }
        }
        highlightAndSwap(smallerElementIndex + 1, high);
        return smallerElementIndex + 1;
    }

    /**
     * Selects a pivot index using the median-of-three method.
     *
     * @param low  the starting index.
     * @param high the ending index.
     * @return the pivot index.
     */
    private int selectPivotIndex(int low, int high) {
        int mid = low + (high - low) / 2;
        int first = numbers.get(low);
        int middle = numbers.get(mid);
        int last = numbers.get(high);
        if ((first <= middle && middle <= last) || (last <= middle && middle <= first)) {
            return mid;
        } else if ((middle <= first && first <= last) || (last <= first && first <= middle)) {
            return low;
        } else {
            return high;
        }
    }

    /**
     * Highlights two elements, swaps them, and updates the UI.
     *
     * @param index1 the first index.
     * @param index2 the second index.
     * @throws Exception if an error occurs.
     */
    private void highlightAndSwap(int index1, int index2) throws Exception {
        if (index1 == index2) {
            return;
        }
        SwingUtilities.invokeAndWait(() -> {
            resetButtonColors();
            JButton button1 = getButtonByIndex(index1);
            JButton button2 = getButtonByIndex(index2);
            if (button1 != null) button1.setBackground(HIGHLIGHT_COLOR);
            if (button2 != null) button2.setBackground(HIGHLIGHT_COLOR);
            numbersPanel.revalidate();
            numbersPanel.repaint();
        });
        Thread.sleep(SORT_DELAY_MS);
        Collections.swap(numbers, index1, index2);
        SwingUtilities.invokeAndWait(this::refreshNumbersPanel);
        Thread.sleep(SORT_DELAY_MS / 2);
    }

    /**
     * Resets all button colors to the default.
     */
    private void resetButtonColors() {
        for (JButton button : getAllButtons()) {
            button.setBackground(BUTTON_COLOR);
        }
    }

    /**
     * Retrieves all buttons from the numbers panel.
     *
     * @return a list of JButtons.
     */
    private List<JButton> getAllButtons() {
        List<JButton> buttons = new ArrayList<>();
        for (Component comp : numbersPanel.getComponents()) {
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
     * Retrieves a button by its index.
     *
     * @param targetIndex the target index.
     * @return the JButton if found; otherwise, null.
     */
    private JButton getButtonByIndex(int targetIndex) {
        List<JButton> buttons = getAllButtons();
        return (targetIndex >= 0 && targetIndex < buttons.size()) ? buttons.get(targetIndex) : null;
    }

    /**
     * Creates a configured JButton.
     *
     * @param text the button text.
     * @return the configured JButton.
     */
    private JButton createConfiguredButton(String text) {
        JButton button = new JButton(text);
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
     * Displays an error message dialog.
     *
     * @param message the error message.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

}