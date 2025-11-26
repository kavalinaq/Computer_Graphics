import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageProcessorApp extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private JLabel imageLabel;
    private JComboBox<String> methodBox;
    private JButton loadButton, processButton, saveButton;

    public ImageProcessorApp() {
        super("Image Processing App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        loadButton = new JButton("Загрузить изображение");
        processButton = new JButton("Обработать");
        saveButton = new JButton("Сохранить результат");

        methodBox = new JComboBox<>(new String[]{
                "Медианный фильтр",
                "Минимальный фильтр",
                "Максимальный фильтр",
                "Пороговая обработка (Отсу)",
                "Пороговая обработка (градиент яркости)"
        });

        controlPanel.add(loadButton);
        controlPanel.add(methodBox);
        controlPanel.add(processButton);
        controlPanel.add(saveButton);

        add(controlPanel, BorderLayout.NORTH);

        imageLabel = new JLabel("Выберите изображение", SwingConstants.CENTER);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        initActions();
    }

    private void initActions() {
        loadButton.addActionListener(e -> loadImage());
        processButton.addActionListener(e -> processImage());
        saveButton.addActionListener(e -> saveImage());
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(chooser.getSelectedFile());
                processedImage = null;
                if (originalImage != null) {
                    imageLabel.setIcon(new ImageIcon(originalImage));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки изображения");
            }
        }
    }

    private void processImage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение");
            return;
        }

        String method = (String) methodBox.getSelectedItem();

        switch (method) {
            case "Медианный фильтр":
                processedImage = medianFilter(originalImage);
                break;
            case "Минимальный фильтр":
                processedImage = minFilter(originalImage);
                break;
            case "Максимальный фильтр":
                processedImage = maxFilter(originalImage);
                break;
            case "Пороговая обработка (Отсу)":
                processedImage = methodOtsu(originalImage);
                break;
            case "Пороговая обработка (градиент яркости)":
                processedImage = methodGradientThreshold(originalImage);
                break;
        }

        if (processedImage != null) {
            imageLabel.setIcon(new ImageIcon(processedImage));
        }
    }

    private void saveImage() {
        if (processedImage == null) return;

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(processedImage, "png", chooser.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения изображения");
            }
        }
    }

    private BufferedImage medianFilter(BufferedImage img) {
        return rankFilter(img, 4);
    }

    private BufferedImage minFilter(BufferedImage img) {
        return rankFilter(img, 0);
    }

    private BufferedImage maxFilter(BufferedImage img) {
        return rankFilter(img, 8);
    }

    private BufferedImage rankFilter(BufferedImage img, int rank) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int[] window = new int[9];
                int k = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color c = new Color(img.getRGB(x + dx, y + dy));
                        int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        window[k++] = gray;
                    }
                }
                java.util.Arrays.sort(window);
                int v = window[rank];
                Color res = new Color(v, v, v);
                out.setRGB(x, y, res.getRGB());
            }
        }


        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (y == 0 || y == h - 1 || x == 0 || x == w - 1) {
                    out.setRGB(x, y, img.getRGB(x, y));
                }
            }
        }

        return out;
    }

    private BufferedImage methodGradientThreshold(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[][] gray = new int[h][w];

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x,y));
                gray[y][x] = (c.getRed()+c.getGreen()+c.getBlue())/3;
            }

        int[][] grad = new int[h][w];
        int maxGrad = 0;

        for (int y = 1; y < h-1; y++)
            for (int x = 1; x < w-1; x++) {
                int gx = gray[y][x+1] - gray[y][x-1];
                int gy = gray[y+1][x] - gray[y-1][x];
                int g = (int)Math.sqrt(gx*gx + gy*gy);
                grad[y][x] = g;
                if(g > maxGrad) maxGrad = g;
            }

        if (maxGrad == 0) {
            JOptionPane.showMessageDialog(this,
                    "На изображении нет границ — градиент = 0.\n" +
                            "Картинка слишком однородная или слишком сжатая.");
            return img;
        }

        int threshold = (int)(maxGrad * 0.4);

        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

        for (int y=0;y<h;y++)
            for(int x=0;x<w;x++){
                int v;
                if (y == 0 || y == h-1 || x == 0 || x == w-1) {
                    Color c = new Color(img.getRGB(x,y));
                    v = (c.getRed()+c.getGreen()+c.getBlue())/3;
                } else {
                    v = grad[y][x] >= threshold ? 255 : 0;
                }
                out.setRGB(x,y,new Color(v,v,v).getRGB());
            }

        return out;
    }

    private BufferedImage methodOtsu(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] hist = new int[256];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int g = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                hist[g]++;
            }
        }

        int total = w * h;
        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * hist[i];

        float sumB = 0;
        int wB = 0;
        float maxVar = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += hist[i];
            if (wB == 0) continue;

            int wF = total - wB;
            if (wF == 0) break;

            sumB += i * hist[i];

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = wB * wF * (mB - mF) * (mB - mF);

            if (varBetween > maxVar) {
                maxVar = varBetween;
                threshold = i;
            }
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int g = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                int v = (g >= threshold) ? 255 : 0;
                out.setRGB(x, y, new Color(v, v, v).getRGB());
            }
        }

        return out;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageProcessorApp().setVisible(true));
    }
}