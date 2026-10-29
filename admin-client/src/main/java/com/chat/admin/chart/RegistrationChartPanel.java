package com.chat.admin.chart;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.view.AdminUIStyle;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Annual New User Registrations Bar Chart Panel (Task 10 / T016).
 *
 * <p>Covers Requirement 1.2.6: Allow selecting a year and render a 12-month JFreeChart bar chart of
 * newly registered users for each month of the selected year.
 */
public final class RegistrationChartPanel extends JPanel {
  private static final String[] MONTHS = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
  private final JComboBox<Integer> yearCombo =
      new JComboBox<>(new Integer[] {2024, 2025, 2026});
  private final JLabel annualTotalLabel = new JLabel("0 signups");
  private final JLabel peakMonthLabel = new JLabel("Aug (0)");
  private final JLabel monthlyAvgLabel = new JLabel("0.0 / month");
  private JFreeChart barChart;

  public RegistrationChartPanel() {
    setLayout(new BorderLayout(0, 10));
    setBackground(AdminUIStyle.SURFACE_WHITE);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AdminUIStyle.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

    yearCombo.setFont(AdminUIStyle.FONT_BODY_BOLD);
    yearCombo.setSelectedItem(2025);

    add(buildHeaderRow(), BorderLayout.NORTH);
    add(buildChartCanvas(), BorderLayout.CENTER);
    add(buildMiniMetricsFooter(), BorderLayout.SOUTH);

    yearCombo.addActionListener(
        e -> {
          Integer selected = (Integer) yearCombo.getSelectedItem();
          if (selected != null) {
            updateChartForYear(selected);
          }
        });
    dataStore.addChangeListener(
        () -> {
          Integer selected = (Integer) yearCombo.getSelectedItem();
          if (selected != null) {
            updateChartForYear(selected);
          }
        });

    updateChartForYear(2025);
  }

  /** Updates the selected year and refreshes the 12-month registration dataset. */
  public void setSelectedYear(int year) {
    yearCombo.setSelectedItem(year);
    updateChartForYear(year);
  }

  private JPanel buildHeaderRow() {
    JPanel header = new JPanel(new BorderLayout(10, 0));
    header.setOpaque(false);

    JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
    titleBox.setOpaque(false);
    JLabel title = new JLabel("Monthly New User Registrations");
    title.setFont(AdminUIStyle.FONT_SUBTITLE);
    title.setForeground(AdminUIStyle.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("12-month signup distribution by calendar year");
    subtitle.setFont(AdminUIStyle.FONT_SMALL);
    subtitle.setForeground(AdminUIStyle.TEXT_MUTED);
    titleBox.add(title);
    titleBox.add(subtitle);

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    controls.setOpaque(false);
    JLabel yearLbl = new JLabel("Year:");
    yearLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    yearLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    controls.add(yearLbl);
    controls.add(yearCombo);

    header.add(titleBox, BorderLayout.CENTER);
    header.add(controls, BorderLayout.EAST);
    return header;
  }

  private ChartPanel buildChartCanvas() {
    barChart =
        ChartFactory.createBarChart(
            null,
            "Month of Year",
            "New Registrations",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false);
    barChart.setBackgroundPaint(AdminUIStyle.SURFACE_WHITE);

    CategoryPlot plot = barChart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xF8FAFC));
    plot.setOutlineVisible(false);
    plot.setRangeGridlinePaint(new Color(0xCBD5E1));
    plot.setRangeGridlineStroke(new BasicStroke(1.0f));
    plot.setDomainGridlinesVisible(false);

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setLabelFont(AdminUIStyle.FONT_BODY_BOLD);
    domainAxis.setLabelPaint(AdminUIStyle.TEXT_SECONDARY);
    domainAxis.setTickLabelFont(AdminUIStyle.FONT_SMALL);
    domainAxis.setTickLabelPaint(AdminUIStyle.TEXT_PRIMARY);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setLabelFont(AdminUIStyle.FONT_BODY_BOLD);
    rangeAxis.setLabelPaint(AdminUIStyle.TEXT_SECONDARY);
    rangeAxis.setTickLabelFont(AdminUIStyle.FONT_SMALL);
    rangeAxis.setTickLabelPaint(AdminUIStyle.TEXT_SECONDARY);
    rangeAxis.setUpperMargin(0.18);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setSeriesPaint(0, new Color(0x3B82F6));
    renderer.setShadowVisible(false);
    renderer.setMaximumBarWidth(0.055);
    renderer.setDefaultItemLabelGenerator(
        new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getIntegerInstance()));
    renderer.setDefaultItemLabelFont(AdminUIStyle.FONT_BADGE);
    renderer.setDefaultItemLabelPaint(AdminUIStyle.TEXT_PRIMARY);
    renderer.setDefaultItemLabelsVisible(true);

    ChartPanel chartPanel = new ChartPanel(barChart);
    chartPanel.setPreferredSize(new Dimension(480, 320));
    chartPanel.setMinimumDrawWidth(200);
    chartPanel.setMinimumDrawHeight(150);
    chartPanel.setMaximumDrawWidth(2000);
    chartPanel.setMaximumDrawHeight(1500);
    chartPanel.setBackground(AdminUIStyle.SURFACE_WHITE);
    chartPanel.setPopupMenu(null);
    chartPanel.setDomainZoomable(false);
    chartPanel.setRangeZoomable(false);
    return chartPanel;
  }

  private JPanel buildMiniMetricsFooter() {
    JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
    row.setOpaque(false);
    row.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AdminUIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 4, 0, 4)));

    row.add(createStatPill("Annual Total:", annualTotalLabel, AdminUIStyle.PRIMARY));
    row.add(createStatPill("Peak Signup Month:", peakMonthLabel, new Color(0x4F46E5)));
    row.add(createStatPill("Monthly Average:", monthlyAvgLabel, AdminUIStyle.TEXT_PRIMARY));
    return row;
  }

  private JPanel createStatPill(String labelText, JLabel valueLabel, Color valueColor) {
    JPanel pill = new JPanel(new GridLayout(2, 1, 0, 2));
    pill.setOpaque(false);
    JLabel title = new JLabel(labelText);
    title.setFont(AdminUIStyle.FONT_SMALL);
    title.setForeground(AdminUIStyle.TEXT_MUTED);
    valueLabel.setFont(AdminUIStyle.FONT_BODY_BOLD);
    valueLabel.setForeground(valueColor);
    pill.add(title);
    pill.add(valueLabel);
    return pill;
  }

  private void updateChartForYear(int year) {
    int[] values = dataStore.getMonthlyRegistrations(year);
    dataset.clear();

    int total = 0;
    int maxVal = -1;
    int maxMonthIdx = 0;

    for (int i = 0; i < MONTHS.length && i < values.length; i++) {
      dataset.addValue(values[i], "New Registrations (" + year + ")", MONTHS[i]);
      total += values[i];
      if (values[i] > maxVal) {
        maxVal = values[i];
        maxMonthIdx = i;
      }
    }

    double avg = values.length == 0 ? 0.0 : (double) total / values.length;
    annualTotalLabel.setText(total + " signups (" + year + ")");
    peakMonthLabel.setText(MONTHS[maxMonthIdx] + " (" + maxVal + ")");
    monthlyAvgLabel.setText(String.format("%.1f / month", avg));
  }
}
