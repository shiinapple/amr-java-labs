package lab;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SpinnerNumberModel;

/** 可选的简易可视化入口；算法仍只在 MyPathChecker 中练习。 */
public final class MapLabApp {
    private final JFrame frame = new JFrame("地图路径检查 · Java 练习");
    private final Canvas canvas = new Canvas();
    private final JLabel status = new JLabel("先生成地图，再运行检查。红色表示参与冲突的路径。");
    private final JComboBox<String> scenario = new JComboBox<>(new String[]{"分散路径", "随机交叉", "文档中的五条路径"});
    private final JComboBox<String> implementation = new JComboBox<>(new String[]{"我的实现", "旧版参照"});
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(300, 0, 3000, 50));
    private final JButton generate = new JButton("生成地图");
    private final JButton run = new JButton("运行检查");
    private List<MapPath> paths = ExampleMaps.teachingExample();

    private void show() {
        JPanel controls = new JPanel();
        controls.add(new JLabel("地图：")); controls.add(scenario);
        controls.add(new JLabel("数量：")); controls.add(count);
        controls.add(generate); controls.add(implementation); controls.add(run);
        canvas.setPaths(paths);
        status.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame.add(controls, BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 700);
        frame.setLocationRelativeTo(null);
        generate.addActionListener(event -> generate());
        run.addActionListener(event -> check());
        frame.setVisible(true);
    }

    private void generate() {
        int n = (int) count.getValue();
        paths = switch (scenario.getSelectedIndex()) {
            case 1 -> ExampleMaps.random(n, 42);
            case 2 -> ExampleMaps.teachingExample();
            default -> ExampleMaps.sparse(n);
        };
        canvas.setPaths(paths);
        status.setText("已生成 " + paths.size() + " 条路径；切换实现后可对同一张地图重复检查。");
    }

    private void check() {
        List<MapPath> snapshot = List.copyOf(paths);
        PathChecker checker = implementation.getSelectedIndex() == 0 ? new MyPathChecker() : new ReferencePathChecker();
        String label = (String) implementation.getSelectedItem();
        run.setEnabled(false); generate.setEnabled(false);
        status.setText("正在检查 " + snapshot.size() + " 条路径……");
        new SwingWorker<CheckResult, Void>() {
            long elapsed;
            @Override protected CheckResult doInBackground() {
                long start = System.nanoTime();
                CheckResult result = checker.check(snapshot);
                elapsed = System.nanoTime() - start;
                return result;
            }
            @Override protected void done() {
                try {
                    CheckResult result = get();
                    canvas.setConflicts(result.conflicts());
                    status.setText(String.format("%s：%,d 条路径；%,d 对冲突；%,d 次精确判断；耗时 %.1f 毫秒。",
                            label, snapshot.size(), result.conflicts().size(), result.exactChecks(), elapsed / 1_000_000.0));
                } catch (Exception ex) {
                    status.setText("检查失败：" + (ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()));
                } finally {
                    run.setEnabled(true); generate.setEnabled(true);
                }
            }
        }.execute();
    }

    private static final class Canvas extends JPanel {
        private List<MapPath> paths = List.of();
        private Set<String> highlighted = Set.of();
        Canvas() { setBackground(Color.WHITE); setPreferredSize(new Dimension(900, 550)); }
        void setPaths(List<MapPath> paths) { this.paths = paths; highlighted = Set.of(); repaint(); }
        void setConflicts(List<Conflict> conflicts) {
            Set<String> ids = new HashSet<>();
            for (Conflict c : conflicts) { ids.add(c.firstPathId()); ids.add(c.secondPathId()); }
            highlighted = ids; repaint();
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (paths.isEmpty()) return;
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
                double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
                for (MapPath path : paths) for (Point p : List.of(path.from(), path.to())) {
                    minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
                    minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
                }
                double scale = Math.min((getWidth() - 80.0) / Math.max(1.0, maxX - minX),
                        (getHeight() - 80.0) / Math.max(1.0, maxY - minY));
                for (MapPath path : paths) {
                    boolean hit = highlighted.contains(path.id());
                    g.setColor(hit ? new Color(205, 52, 52) : new Color(52, 102, 160));
                    g.setStroke(new BasicStroke(hit ? 2.5f : 1.5f));
                    int x1 = 40 + (int) ((path.from().x() - minX) * scale);
                    int y1 = getHeight() - 40 - (int) ((path.from().y() - minY) * scale);
                    int x2 = 40 + (int) ((path.to().x() - minX) * scale);
                    int y2 = getHeight() - 40 - (int) ((path.to().y() - minY) * scale);
                    g.drawLine(x1, y1, x2, y2);
                    g.fillOval(x1 - 2, y1 - 2, 5, 5); g.fillOval(x2 - 2, y2 - 2, 5, 5);
                    if (paths.size() <= 20) g.drawString(path.id(), x1 + 3, y1 - 4);
                }
            } finally { g.dispose(); }
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new MapLabApp().show()); }
}
