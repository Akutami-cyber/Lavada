package mainpackage;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Provides shared Swing layout and activation behavior for dashboard pages.
 * Subclasses supply page content and may override the activation callback.
 */
public abstract class AbstractDashboardPage extends JPanel implements DashboardPage {
    private static final long serialVersionUID = 1L;

    protected AbstractDashboardPage(Color background, int top, int left, int bottom, int right) {
        setBackground(background);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    /** Restores Swing layout and paint state when a page is selected. */
    @Override
    public void onPageShown() {
        revalidate();
        repaint();
    }
}




