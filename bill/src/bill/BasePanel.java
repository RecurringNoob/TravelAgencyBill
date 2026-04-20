package bill;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Abstract base for all screen panels.
 * Provides the shared design system: colours, fonts, and component helpers.
 *
 * FIX: gbc is NO LONGER a shared field. createCardPanel() now returns a CardResult
 * that bundles the panel with its own GridBagConstraints, eliminating the
 * original mutable-field bug where a second call to createCardPanel() would
 * overwrite constraints used by the first card.
 */
public abstract class BasePanel extends JPanel {

    protected MainApp        controller;
    protected BookingSession session;

    // -------------------------------------------------------------------------
    // Design Tokens — Colours
    // -------------------------------------------------------------------------
    protected static final Color PRIMARY_COLOR    = new Color(41,  128, 185);
    protected static final Color SECONDARY_COLOR  = new Color(52,  152, 219);
    protected static final Color ACCENT_COLOR     = new Color(44,  62,  80);
    protected static final Color SUCCESS_COLOR    = new Color(39,  174, 96);
    protected static final Color DANGER_COLOR     = new Color(231, 76,  60);
    protected static final Color WARNING_COLOR    = new Color(243, 156, 18);
    protected static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    protected static final Color TEXT_COLOR       = new Color(44,  62,  80);
    protected static final Color TEXT_MUTED       = new Color(127, 140, 141);
    protected static final Color BORDER_COLOR     = new Color(189, 195, 199);

    // -------------------------------------------------------------------------
    // Design Tokens — Typography
    // -------------------------------------------------------------------------
    protected static final Font HEADER_FONT    = new Font("Segoe UI", Font.BOLD,  24);
    protected static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    protected static final Font LABEL_FONT     = new Font("Segoe UI", Font.BOLD,  12);
    protected static final Font INPUT_FONT     = new Font("Segoe UI", Font.PLAIN, 14);

    // -------------------------------------------------------------------------
    // Currency — single source of truth (fixes ₹ vs Rs. mismatch)
    // -------------------------------------------------------------------------
    protected static final String CURRENCY = "Rs.";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public BasePanel(MainApp controller, BookingSession session) {
        this.controller = controller;
        this.session    = session;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
    }

    // -------------------------------------------------------------------------
    // Layout Helpers
    // -------------------------------------------------------------------------

    protected JPanel createTitlePanel(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 20, 40));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        if (subtitle != null) {
            panel.add(Box.createVerticalStrut(5));
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(SUBHEADER_FONT);
            subtitleLabel.setForeground(TEXT_MUTED);
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(subtitleLabel);
        }
        return panel;
    }

    /**
     * Bundles a card panel with its own independent GridBagConstraints.
     * Use this instead of the old createCardPanel() to avoid the shared-gbc bug.
     */
    public static class CardResult {
        public final JPanel                panel;
        public final GridBagConstraints    gbc;
        CardResult(JPanel panel, GridBagConstraints gbc) {
            this.panel = panel;
            this.gbc   = gbc;
        }
    }

    /** Creates a white rounded-corner card panel with its own GBC. */
    protected CardResult createCard() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        return new CardResult(panel, gbc);
    }

    // -------------------------------------------------------------------------
    // Component Helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a label + field pair into a GridBagLayout panel.
     * @param gbc the card's own GBC (not a shared field)
     */
    protected void addLabelAndField(JPanel panel, GridBagConstraints gbc,
                                    String labelText, JComponent field, int row) {
        gbc.gridx   = 0;
        gbc.gridy   = row;
        gbc.weightx = 0.3;
        gbc.anchor  = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText.toUpperCase());
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_MUTED);
        panel.add(label, gbc);

        gbc.gridx   = 1;
        gbc.weightx = 0.7;

        styleField(field);
        panel.add(field, gbc);
    }

    private void styleField(JComponent field) {
        if (field instanceof JTextField) {
            JTextField tf = (JTextField) field;
            tf.setFont(INPUT_FONT);
            tf.setForeground(TEXT_COLOR);
            tf.setBackground(Color.WHITE);
            tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
            tf.setPreferredSize(new Dimension(250, 40));
        } else if (field instanceof JSpinner) {
            field.setPreferredSize(new Dimension(250, 40));
            field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(250, 40));
            field.setFont(INPUT_FONT);
            field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        }
    }

    protected JButton createStyledButton(String text, Color baseColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if      (getModel().isPressed())  g2.setColor(baseColor.darker());
                else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
                else                              g2.setColor(baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(isPrimary ? 180 : 140, 45));
        return btn;
    }

    /** Creates a small icon-style button (e.g. for table row actions). */
    protected JButton createIconButton(String text, Color baseColor) {
        JButton btn = createStyledButton(text, baseColor, false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(70, 30));
        return btn;
    }

    protected void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 246, 254));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(BORDER_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
    }

    // -------------------------------------------------------------------------
    // Dialog Shortcuts
    // -------------------------------------------------------------------------
    protected boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm",
               JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    protected void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE);       }
    protected void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    protected void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);     }
}
