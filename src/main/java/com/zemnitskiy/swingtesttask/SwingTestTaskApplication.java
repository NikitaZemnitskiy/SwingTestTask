package com.zemnitskiy.swingtesttask;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SwingTestTaskApplication extends JFrame {
    // Constants for button size
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 30;
    private static final Dimension BUTTON_SIZE = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);

    // Constants for font
    private static final String FONT_NAME = "Arial";
    private static final int FONT_STYLE = Font.BOLD;
    private static final int FONT_SIZE = 14;
    private static final Font DEFAULT_FONT = new Font(FONT_NAME, FONT_STYLE, FONT_SIZE);
    private static final Font LABEL_FONT = new Font(FONT_NAME, FONT_STYLE, 16);

    // Delay for sorting animation (in milliseconds)
    private static final int SORT_DELAY_MS = 50;

    private CardLayout cardLayout;
    private JPanel mainPanel, introPanel, sortPanel;
    private JTextField inputField;
    private JPanel numbersPanel;
    private JButton sortButton;
    private JButton resetButton;
    private List<Integer> numbers;
    private int numberCount;

    // Sorting flag: false – descending, true – ascending; toggles after each "Sort" click
    private boolean sortAscending = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingTestTaskApplication().setVisible(true));
    }

    public SwingTestTaskApplication() {
        setTitle("Swing Test Task");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        introPanel = createIntroScreen();
        mainPanel.add(introPanel, "Intro");

        sortPanel = createSortScreen();
        mainPanel.add(sortPanel, "Sort");

        add(mainPanel);
        cardLayout.show(mainPanel, "Intro");
    }

    // Create Intro screen: input field and Enter button
    private JPanel createIntroScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel label = new JLabel("How many numbers to display?");
        label.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label, gbc);

        inputField = new JTextField(10);
        gbc.gridy = 1;
        panel.add(inputField, gbc);

        JButton enterButton = new JButton("Enter");
        enterButton.setBackground(new Color(59, 89, 182));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFont(DEFAULT_FONT);
        enterButton.setMinimumSize(BUTTON_SIZE);
        enterButton.setPreferredSize(BUTTON_SIZE);
        enterButton.setMaximumSize(BUTTON_SIZE);
        gbc.gridy = 2;
        panel.add(enterButton, gbc);

        enterButton.addActionListener(e -> {
            try {
                numberCount = Integer.parseInt(inputField.getText());
                if (numberCount <= 0) {
                    JOptionPane.showMessageDialog(SwingTestTaskApplication.this,
                            "Enter a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                generateNumbers();
                cardLayout.show(mainPanel, "Sort");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SwingTestTaskApplication.this,
                        "Enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // Create Sort screen: number buttons and control buttons (Sort, Reset)
    private JPanel createSortScreen() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel for displaying number buttons (scrollable)
        numbersPanel = new JPanel();
        numbersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        JScrollPane scrollPane = new JScrollPane(numbersPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Control panel with Sort and Reset buttons
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        sortButton = new JButton("Sort");
        sortButton.setBackground(Color.GREEN);
        sortButton.setForeground(Color.WHITE);
        sortButton.setFont(DEFAULT_FONT);
        sortButton.setMinimumSize(BUTTON_SIZE);
        sortButton.setPreferredSize(BUTTON_SIZE);
        sortButton.setMaximumSize(BUTTON_SIZE);

        resetButton = new JButton("Reset");
        resetButton.setBackground(Color.GREEN);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(DEFAULT_FONT);
        resetButton.setMinimumSize(BUTTON_SIZE);
        resetButton.setPreferredSize(BUTTON_SIZE);
        resetButton.setMaximumSize(BUTTON_SIZE);

        controlPanel.add(sortButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(resetButton);
        panel.add(controlPanel, BorderLayout.EAST);

        sortButton.addActionListener(e -> startSortAnimation());
        resetButton.addActionListener(e -> cardLayout.show(mainPanel, "Intro"));

        return panel;
    }

    // Generate random numbers according to requirements
    private void generateNumbers() {
        numbers = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numberCount - 1; i++) {
            int num = rand.nextInt(1000 - 31 + 1) + 31;
            numbers.add(num);
        }
        numbers.add(rand.nextInt(30) + 1); // Ensure at least one number ≤ 30
        Collections.shuffle(numbers);
        updateNumbersPanel();
    }

    // Update the numbers panel: arrange buttons in columns (max 10 buttons per column)
    private void updateNumbersPanel() {
        numbersPanel.removeAll();
        int total = numbers.size();
        int columns = (int) Math.ceil(total / 10.0);
        int index = 0;

        for (int col = 0; col < columns; col++) {
            JPanel colPanel = new JPanel();
            colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));

            for (int row = 0; row < 10 && index < total; row++) {
                JButton btn = getjButton(index);
                colPanel.add(btn);
                colPanel.add(Box.createVerticalStrut(5));
                index++;
            }
            numbersPanel.add(colPanel);
        }

        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    private JButton getjButton(int index) {
        int value = numbers.get(index);
        JButton btn = new JButton(String.valueOf(value));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMinimumSize(BUTTON_SIZE);
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setMaximumSize(BUTTON_SIZE);

        btn.setBackground(new Color(59, 89, 182));
        btn.setForeground(Color.WHITE);
        btn.setFont(DEFAULT_FONT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(59, 89, 182)));

        btn.addActionListener(e -> {
            if (value <= 30) {
                generateNumbers();
            } else {
                JOptionPane.showMessageDialog(SwingTestTaskApplication.this,
                        "Please select a value smaller or equal to 30.");
            }
        });
        return btn;
    }

    // Start sorting animation using QuickSort
    private void startSortAnimation() {
        sortButton.setEnabled(false);

        SwingWorker<Void, Void> sorter = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                quickSort(0, numbers.size() - 1, sortAscending);
                return null;
            }

            @Override
            protected void done() {
                sortAscending = !sortAscending; // Toggle sorting order
                sortButton.setEnabled(true);
            }
        };

        sorter.execute();
    }

    // Recursive QuickSort implementation
    private void quickSort(int low, int high, boolean ascending) throws Exception {
        if (low < high) {
            int pi = partition(low, high, ascending);
            quickSort(low, pi - 1, ascending);
            quickSort(pi + 1, high, ascending);
        }
    }

    // Partition method for QuickSort using median-of-three pivot selection
    private int partition(int low, int high, boolean ascending) throws Exception {
        int mid = low + (high - low) / 2;

        // Retrieve values of first, middle, and last elements
        int a = numbers.get(low);
        int b = numbers.get(mid);
        int c = numbers.get(high);

        // Determine median-of-three pivot index
        int pivotIndex;
        if ((a <= b && b <= c) || (c <= b && b <= a)) {
            pivotIndex = mid;
        } else if ((b <= a && a <= c) || (c <= a && a <= b)) {
            pivotIndex = low;
        } else {
            pivotIndex = high;
        }

        // Swap the chosen pivot with the last element
        Collections.swap(numbers, pivotIndex, high);
        updateAndDelay();

        int pivot = numbers.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if ((ascending && numbers.get(j) < pivot) ||
                    (!ascending && numbers.get(j) > pivot)) {
                i++;
                Collections.swap(numbers, i, j);
                updateAndDelay();
            }
        }
        Collections.swap(numbers, i + 1, high);
        updateAndDelay();
        return i + 1;
    }

    // Update UI and add delay for animation
    private void updateAndDelay() throws Exception {
        SwingUtilities.invokeAndWait(this::updateNumbersPanel);
        Thread.sleep(SORT_DELAY_MS);
    }
}