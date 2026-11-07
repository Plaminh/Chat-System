package com.chat.admin.chart;

import com.chat.common.ui.ChatTheme;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.view.AdminUIComponents;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
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
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Annual Active Users (App Opens) Line Chart Panel (Task 10 / T017).
 *
 * <p>Covers Requirement 1.2.9: Allow selecting a year and render a 12-month JFreeChart line chart
 * showing the number of users who opened the application in each month of the selected year.
 */
public final class ActivityChartPanel extends JPanel {
  private static final String[] MONTHS = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
  private final JComboBox<Integer> yearCombo =
      new JComboBox<>(new Integer[] {2024, 2025, 2026});
  private final JLabel annualActiveTotalLabel = new JLabel("0 active sessions");
  private final JLabel peakActivityMonthLabel = new JLabel("Aug (0)");
  private final JLabel monthlyAvgActiveLabel = new JLabel("0.0 / month");
  private JFreeChart lineChart;

  public ActivityChartPanel() {
    setLayout(new BorderLayout(0, 10));
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ChatTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

    yearCombo.setFont(ChatTheme.FONT_BODY_BOLD);
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

  /** Updates the selected year and refreshes the 12-month active users dataset. */
  public void setSelectedYear(int year) {
    yearCombo.setSelectedItem(year);
    updateChartForYear(year);
  }

  private JPanel buildHeaderRow() {
    JPanel header = new JPanel(new BorderLayout(10, 0));
    header.setOpaque(false);

    JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
    titleBox.setOpaque(false);
    JLabel title = new JLabel("Monthly Active Users (App Opens)");
    title.setFont(ChatTheme.FONT_SUBTITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("12-month trend of active users by calendar year");
    subtitle.setFont(ChatTheme.FONT_SMALL);
    subtitle.setForeground(ChatTheme.TEXT_MUTED);
    titleBox.add(title);
    titleBox.add(subtitle);

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    controls.setOpaque(false);
    JLabel yearLbl = new JLabel("Year:");
    yearLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    yearLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    controls.add(yearLbl);
    controls.add(yearCombo);

    header.add(titleBox, BorderLayout.CENTER);
    header.add(controls, BorderLayout.EAST);
    return header;
  }

  private ChartPanel buildChartCanvas() {
    lineChart =
        ChartFactory.createLineChart(
            null,
            "Month of Year",
            "Active Users (App Opens)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false);
    lineChart.setBackgroundPaint(ChatTheme.SURFACE_WHITE);

    CategoryPlot plot = lineChart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xF8FAFC));
    plot.setOutlineVisible(false);
    plot.setRangeGridlinePaint(new Color(0xCBD5E1));
    plot.setRangeGridlineStroke(new BasicStroke(1.0f));
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(new Color(0xE2E8F0));

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setLabelFont(ChatTheme.FONT_BODY_BOLD);
    domainAxis.setLabelPaint(ChatTheme.TEXT_SECONDARY);
    domainAxis.setTickLabelFont(ChatTheme.FONT_SMALL);
    domainAxis.setTickLabelPaint(ChatTheme.TEXT_PRIMARY);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setLabelFont(ChatTheme.FONT_BODY_BOLD);
    rangeAxis.setLabelPaint(ChatTheme.TEXT_SECONDARY);
    rangeAxis.setTickLabelFont(ChatTheme.FONT_SMALL);
    rangeAxis.setTickLabelPaint(ChatTheme.TEXT_SECONDARY);
    rangeAxis.setUpperMargin(0.18);

    LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, true);
    renderer.setSeriesPaint(0, new Color(0x10B981));
    renderer.setSeriesStroke(
        0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    renderer.setSeriesShape(0, new Ellipse2D.Double(-4.5, -4.5, 9.0, 9.0));
    renderer.setUseFillPaint(true);
    renderer.setSeriesFillPaint(0, Color.WHITE);
    renderer.setDefaultItemLabelGenerator(
        new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getIntegerInstance()));
    renderer.setDefaultItemLabelFont(ChatTheme.FONT_BADGE);
    renderer.setDefaultItemLabelPaint(ChatTheme.TEXT_PRIMARY);
    renderer.setDefaultItemLabelsVisible(true);
    plot.setRenderer(renderer);

    ChartPanel chartPanel = new ChartPanel(lineChart);
    chartPanel.setPreferredSize(new Dimension(480, 320));
    chartPanel.setMinimumDrawWidth(200);
    chartPanel.setMinimumDrawHeight(150);
    chartPanel.setMaximumDrawWidth(2000);
    chartPanel.setMaximumDrawHeight(1500);
    chartPanel.setBackground(ChatTheme.SURFACE_WHITE);
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
            BorderFactory.createMatteBorder(1, 0, 0, 0, ChatTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 4, 0, 4)));

    row.add(createStatPill("Annual Active Sum:", annualActiveTotalLabel, ChatTheme.SUCCESS_DOT));
    row.add(createStatPill("Peak Activity Month:", peakActivityMonthLabel, new Color(0x0D9488)));
    row.add(createStatPill("Monthly Average:", monthlyAvgActiveLabel, ChatTheme.TEXT_PRIMARY));
    return row;
  }

  private JPanel createStatPill(String labelText, JLabel valueLabel, Color valueColor) {
    JPanel pill = new JPanel(new GridLayout(2, 1, 0, 2));
    pill.setOpaque(false);
    JLabel title = new JLabel(labelText);
    title.setFont(ChatTheme.FONT_SMALL);
    title.setForeground(ChatTheme.TEXT_MUTED);
    valueLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    valueLabel.setForeground(valueColor);
    pill.add(title);
    pill.add(valueLabel);
    return pill;
  }

  private void updateChartForYear(int year) {
    int[] values = dataStore.getMonthlyActiveUsers(year);
    dataset.clear();

    int total = 0;
    int maxVal = -1;
    int maxMonthIdx = 0;

    for (int i = 0; i < MONTHS.length && i < values.length; i++) {
      dataset.addValue(values[i], "Active Users (" + year + ")", MONTHS[i]);
      total += values[i];
      if (values[i] > maxVal) {
        maxVal = values[i];
        maxMonthIdx = i;
      }
    }

    double avg = values.length == 0 ? 0.0 : (double) total / values.length;
    annualActiveTotalLabel.setText(total + " active (" + year + ")");
    peakActivityMonthLabel.setText(MONTHS[maxMonthIdx] + " (" + maxVal + ")");
    monthlyAvgActiveLabel.setText(String.format("%.1f / month", avg));
  }
}
