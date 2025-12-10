package ui;

import javax.swing.JPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JDateChooser extends JPanel {
    private JTextField textField;
    private JButton button;
    private JPopupMenu popupMenu;
    private Calendar calendar;
    private DateFormat dateFormat;
    private JSpinner yearSpinner;
    private JSpinner monthSpinner;
    private JButton[][] dayButtons;

    public JDateChooser() {
        this("yyyy-MM-dd");
    }

    public JDateChooser(String format) {
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(format);

        setLayout(new BorderLayout());

        textField = new JTextField(10);
        textField.setEditable(false);
        add(textField, BorderLayout.CENTER);

        button = new JButton("...");
        button.setPreferredSize(new Dimension(25, textField.getPreferredSize().height));
        add(button, BorderLayout.EAST);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup();
            }
        });

        createPopupMenu();
    }

    private void createPopupMenu() {
        popupMenu = new JPopupMenu();

        JPanel calendarPanel = new JPanel(new BorderLayout());

        //     ѡ
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        yearSpinner = new JSpinner(new SpinnerNumberModel(calendar.get(Calendar.YEAR), 1900, 2100, 1));
        monthSpinner = new JSpinner(new SpinnerNumberModel(calendar.get(Calendar.MONTH) + 1, 1, 12, 1));

        yearSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateCalendar();
            }
        });

        monthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateCalendar();
            }
        });

        JButton prevMonthButton = new JButton("<");
        prevMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int month = (Integer) monthSpinner.getValue() - 1;
                int year = (Integer) yearSpinner.getValue();

                if (month < 1) {
                    month = 12;
                    year--;
                }

                monthSpinner.setValue(month);
                yearSpinner.setValue(year);
            }
        });

        JButton nextMonthButton = new JButton(">");
        nextMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int month = (Integer) monthSpinner.getValue() + 1;
                int year = (Integer) yearSpinner.getValue();

                if (month > 12) {
                    month = 1;
                    year++;
                }

                monthSpinner.setValue(month);
                yearSpinner.setValue(year);
            }
        });

        headerPanel.add(prevMonthButton);
        headerPanel.add(new JLabel("  "));
        headerPanel.add(yearSpinner);
        headerPanel.add(new JLabel("  "));
        headerPanel.add(monthSpinner);
        headerPanel.add(nextMonthButton);

        calendarPanel.add(headerPanel, BorderLayout.NORTH);

        //    ڱ
        JPanel weekPanel = new JPanel(new GridLayout(1, 7));
        String[] weekDays = {"  ", "һ", "  ", "  ", "  ", "  ", "  "};
        for (String day : weekDays) {
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setForeground(Color.RED);
            weekPanel.add(label);
        }

        calendarPanel.add(weekPanel, BorderLayout.CENTER);

        //    ڰ ť
        JPanel daysPanel = new JPanel(new GridLayout(6, 7));
        dayButtons = new JButton[6][7];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                final int row = i;
                final int col = j;

                dayButtons[i][j] = new JButton();
                dayButtons[i][j].setFocusPainted(false);
                dayButtons[i][j].setBorderPainted(false);
                dayButtons[i][j].setContentAreaFilled(false);

                dayButtons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = dayButtons[row][col].getText();
                        if (!text.isEmpty()) {
                            calendar.set(Calendar.YEAR, (Integer) yearSpinner.getValue());
                            calendar.set(Calendar.MONTH, (Integer) monthSpinner.getValue() - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));

                            textField.setText(dateFormat.format(calendar.getTime()));
                            popupMenu.setVisible(false);
                        }
                    }
                });

                daysPanel.add(dayButtons[i][j]);
            }
        }

        calendarPanel.add(daysPanel, BorderLayout.SOUTH);

        popupMenu.add(calendarPanel);

        updateCalendar();
    }

    private void updateCalendar() {
        calendar.set(Calendar.YEAR, (Integer) yearSpinner.getValue());
        calendar.set(Calendar.MONTH, (Integer) monthSpinner.getValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        //       а ť
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                dayButtons[i][j].setText("");
                dayButtons[i][j].setForeground(Color.BLACK);
            }
        }

        //
        int day = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < firstDayOfWeek - 1) {
                    continue;
                }

                if (day > daysInMonth) {
                    break;
                }

                dayButtons[i][j].setText(String.valueOf(day));

                //       ʾ  ǰ
                if (isCurrentDay(day, (Integer) monthSpinner.getValue() - 1, (Integer) yearSpinner.getValue())) {
                    dayButtons[i][j].setForeground(Color.RED);
                }

                day++;
            }

            if (day > daysInMonth) {
                break;
            }
        }
    }

    private boolean isCurrentDay(int day, int month, int year) {
        Calendar current = Calendar.getInstance();
        return day == current.get(Calendar.DAY_OF_MONTH) &&
                month == current.get(Calendar.MONTH) &&
                year == current.get(Calendar.YEAR);
    }

    private void showPopup() {
        Point location = button.getLocationOnScreen();
        popupMenu.show(button, 0, button.getHeight());
    }

    public Date getDate() {
        String text = textField.getText();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return dateFormat.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDate(Date date) {
        if (date != null) {
            textField.setText(dateFormat.format(date));

            calendar.setTime(date);
            yearSpinner.setValue(calendar.get(Calendar.YEAR));
            monthSpinner.setValue(calendar.get(Calendar.MONTH) + 1);

            updateCalendar();
        }
    }

    public void setDateFormat(String format) {
        this.dateFormat = new SimpleDateFormat(format);
        Date date = getDate();
        if (date != null) {
            textField.setText(dateFormat.format(date));
        }
    }

    public JTextField getTextField() {
        return textField;
    }
}

