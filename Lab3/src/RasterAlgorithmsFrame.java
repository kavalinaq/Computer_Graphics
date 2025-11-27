import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RasterAlgorithmsFrame extends JFrame {

    private RasterPanel rasterPanel;
    private JComboBox<String> algorithmComboBox;
    private JSpinner x1Spinner, y1Spinner, x2Spinner, y2Spinner, radiusSpinner;
    private JButton drawButton, clearButton;
    private JButton zoomInButton, zoomOutButton, resetZoomButton;
    private JLabel scaleLabel;

    public RasterAlgorithmsFrame() {
        setTitle("Алгоритмы растеризации с масштабированием");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        scaleLabel = new JLabel("Масштаб: 1.0x");
        rasterPanel = new RasterPanel(scaleLabel);

        String[] algorithms = {
                "Пошаговый алгоритм (отрезок)",
                "Алгоритм ЦДА (отрезок)",
                "Алгоритм Брезенхема (отрезок)",
                "Алгоритм Брезенхема (окружность)"
        };

        algorithmComboBox = new JComboBox<>(algorithms);

        x1Spinner = new JSpinner(new SpinnerNumberModel(0, -40, 40, 1));
        y1Spinner = new JSpinner(new SpinnerNumberModel(0, -40, 40, 1));
        x2Spinner = new JSpinner(new SpinnerNumberModel(10, -40, 40, 1));
        y2Spinner = new JSpinner(new SpinnerNumberModel(5, -40, 40, 1));
        radiusSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 40, 1));

        drawButton = new JButton("Построить");
        clearButton = new JButton("Очистить");

        zoomInButton = new JButton("Увеличить (+)");
        zoomOutButton = new JButton("Уменьшить (-)");
        resetZoomButton = new JButton("Сбросить масштаб");

        drawButton.addActionListener(new DrawButtonListener());
        clearButton.addActionListener(e -> rasterPanel.clearPoints());

        zoomInButton.addActionListener(e -> rasterPanel.zoomIn());
        zoomOutButton.addActionListener(e -> rasterPanel.zoomOut());
        resetZoomButton.addActionListener(e -> rasterPanel.resetZoom());

        algorithmComboBox.addActionListener(e -> updateInterface());

        updateInterface();
    }

    private void updateInterface() {
        String selected = (String) algorithmComboBox.getSelectedItem();
        boolean isCircle = selected.contains("окружность");

        x2Spinner.setEnabled(!isCircle);
        y2Spinner.setEnabled(!isCircle);
        radiusSpinner.setEnabled(isCircle);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("Алгоритм:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(algorithmComboBox, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("X1:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(x1Spinner, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("Y1:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(y1Spinner, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("X2:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(x2Spinner, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("Y2:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(y2Spinner, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(new JLabel("Радиус:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(radiusSpinner, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(drawButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(clearButton, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        controlPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Масштабирование:"), gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        controlPanel.add(scaleLabel, gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        controlPanel.add(zoomInButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(zoomOutButton, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        controlPanel.add(resetZoomButton, gbc);
        gbc.gridwidth = 1;

        add(controlPanel, BorderLayout.WEST);
        add(rasterPanel, BorderLayout.CENTER);
    }

    private class DrawButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int x1 = (Integer) x1Spinner.getValue();
            int y1 = (Integer) y1Spinner.getValue();
            int x2 = (Integer) x2Spinner.getValue();
            int y2 = (Integer) y2Spinner.getValue();
            int radius = (Integer) radiusSpinner.getValue();

            String algorithm = (String) algorithmComboBox.getSelectedItem();

            switch (algorithm) {
                case "Пошаговый алгоритм (отрезок)":
                    rasterPanel.drawStepByStep(x1, y1, x2, y2);
                    break;
                case "Алгоритм ЦДА (отрезок)":
                    rasterPanel.drawDDA(x1, y1, x2, y2);
                    break;
                case "Алгоритм Брезенхема (отрезок)":
                    rasterPanel.drawBresenhamLine(x1, y1, x2, y2);
                    break;
                case "Алгоритм Брезенхема (окружность)":
                    rasterPanel.drawBresenhamCircle(x1, y1, radius);
                    break;
            }
        }
    }


}