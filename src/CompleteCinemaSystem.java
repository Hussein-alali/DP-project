import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*; // Required for Search Listener
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class CompleteCinemaSystem extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Uses your existing CinemaData class
    private CinemaData db = CinemaData.getInstance();
    private User currentUser;

    // --- COLORS & FONTS ---
    private static final Color COL_BACKGROUND = new Color(18, 18, 18);
    private static final Color COL_HEADER     = new Color(30, 30, 30);
    private static final Color COL_SURFACE    = new Color(30, 30, 30);
    private static final Color COL_PRIMARY    = new Color(229, 9, 20); // Red
    private static final Color COL_ACCENT     = new Color(64, 124, 255); // Blue
    private static final Color COL_TEXT_MAIN  = new Color(240, 240, 240);
    private static final Color COL_TEXT_SEC   = new Color(170, 170, 170);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_PLAIN  = new Font("Segoe UI", Font.PLAIN, 14);

    private Point mouseDownCompCoords = null;

    public CompleteCinemaSystem() {
        setTitle("Cinema Ticket Booking System");
        setSize(1100, 750);
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI Defaults
        try {
            UIManager.put("Panel.background", COL_BACKGROUND);
            UIManager.put("OptionPane.background", COL_SURFACE);
            UIManager.put("OptionPane.messageForeground", COL_TEXT_MAIN);
        } catch(Exception e){}

        setLayout(new BorderLayout());
        add(createWindowHeader(), BorderLayout.NORTH);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        add(mainPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // CUSTOM WINDOW HEADER
    // ==========================================
    private JPanel createWindowHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_HEADER);
        header.setPreferredSize(new Dimension(getWidth(), 30));

        JLabel title = new JLabel("  Cinema Booking System");
        title.setForeground(new Color(200, 200, 200));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(1, 3, 0, 0));
        controls.setBackground(COL_HEADER);
        controls.setPreferredSize(new Dimension(135, 30));

        JButton btnMin = createHeaderButton("\u2015", e -> setState(Frame.ICONIFIED));
        JButton btnMax = createHeaderButton("\u25A1", e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) setExtendedState(JFrame.NORMAL);
            else setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
        JButton btnClose = createHeaderButton("\u2715", e -> System.exit(0));

        addHoverEffect(btnClose, true);
        addHoverEffect(btnMin, false);
        addHoverEffect(btnMax, false);

        controls.add(btnMin); controls.add(btnMax); controls.add(btnClose);
        header.add(controls, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseDownCompCoords = e.getPoint(); }
        });
        header.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });

        return header;
    }

    private JButton createHeaderButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBorder(null); btn.setFocusPainted(false); btn.setContentAreaFilled(false);
        btn.setOpaque(true); btn.setBackground(COL_HEADER); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        btn.addActionListener(action);
        return btn;
    }

    private void addHoverEffect(JButton btn, boolean isCloseBtn) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(isCloseBtn ? new Color(232, 17, 35) : new Color(60, 60, 60));
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(COL_HEADER); }
        });
    }

    // ==========================================
    // LOGIN
    // ==========================================
    private JPanel createLoginPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(COL_BACKGROUND);
        RoundedPanel card = new RoundedPanel(30, COL_SURFACE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField uField = createStyledField();
        JPasswordField pField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("LOGIN", COL_PRIMARY);
        JButton regBtn = createStyledButton("Create Account", new Color(60, 60, 60));

        gbc.gridy=0; card.add(new JLabel("CINEMA LOGIN"){{setFont(FONT_HEADER);setForeground(COL_TEXT_MAIN);setHorizontalAlignment(0);}}, gbc);
        gbc.gridy=1; card.add(new JLabel("Username"){{setForeground(COL_TEXT_SEC);}}, gbc);
        gbc.gridy=2; card.add(uField, gbc);
        gbc.gridy=3; card.add(new JLabel("Password"){{setForeground(COL_TEXT_SEC);}}, gbc);
        gbc.gridy=4; card.add(pField, gbc);
        gbc.gridy=5; card.add(Box.createVerticalStrut(10), gbc);
        gbc.gridy=6; card.add(loginBtn, gbc);
        gbc.gridy=7; card.add(regBtn, gbc);
        container.add(card);

        loginBtn.addActionListener(e -> {
            currentUser = db.login(uField.getText(), new String(pField.getPassword()));
            if(currentUser != null) {
                if(currentUser instanceof Admin) {
                    mainPanel.add(createAdminPanel(), "ADMIN");
                    cardLayout.show(mainPanel, "ADMIN");
                } else {
                    mainPanel.add(createCustomerPanel(), "CUSTOMER");
                    cardLayout.show(mainPanel, "CUSTOMER");
                }
            } else JOptionPane.showMessageDialog(this, "Invalid Credentials");
        });
        regBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        return container;
    }

    // ==========================================
    // REGISTER
    // ==========================================
    private JPanel createRegisterPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(COL_BACKGROUND);
        RoundedPanel card = new RoundedPanel(30, COL_SURFACE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField uField = createStyledField();
        JPasswordField pField = createStyledPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Customer", "Admin"});
        roleBox.setBackground(new Color(50,50,50)); roleBox.setForeground(Color.WHITE);
        JButton subBtn = createStyledButton("REGISTER", COL_PRIMARY);
        JButton backBtn = createStyledButton("Back", new Color(60,60,60));

        gbc.gridy=0; card.add(new JLabel("NEW ACCOUNT"){{setFont(FONT_HEADER);setForeground(COL_TEXT_MAIN);setHorizontalAlignment(0);}}, gbc);
        gbc.gridy=1; card.add(new JLabel("Username:"){{setForeground(COL_TEXT_SEC);}}, gbc);
        gbc.gridy=2; card.add(uField, gbc);
        gbc.gridy=3; card.add(new JLabel("Password:"){{setForeground(COL_TEXT_SEC);}}, gbc);
        gbc.gridy=4; card.add(pField, gbc);
        gbc.gridy=5; card.add(new JLabel("Role:"){{setForeground(COL_TEXT_SEC);}}, gbc);
        gbc.gridy=6; card.add(roleBox, gbc);
        gbc.gridy=7; card.add(subBtn, gbc);
        gbc.gridy=8; card.add(backBtn, gbc);
        container.add(card);

        subBtn.addActionListener(e -> {
            User u = UserFactory.create((String)roleBox.getSelectedItem(), uField.getText(), new String(pField.getPassword()));
            db.register(u);
            JOptionPane.showMessageDialog(this, "Registered!");
            cardLayout.show(mainPanel, "LOGIN");
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        return container;
    }

    // ==========================================
    // ADMIN PANEL
    // ==========================================
    private JPanel createAdminPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BOLD); tabs.setBackground(COL_BACKGROUND); tabs.setForeground(Color.WHITE);

        // Movie Management
        JPanel moviePanel = new JPanel(new BorderLayout(10, 10));
        moviePanel.setBackground(COL_BACKGROUND); moviePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        DefaultTableModel model = new DefaultTableModel(new String[]{"Title", "Genre", "Lang", "Hall", "Active"}, 0);
        JTable table = new JTable(model);
        styleTable(table);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for(Movie m : db.getMovies()) model.addRow(new Object[]{m.getTitle(), m.getGenre(), m.getLanguage(), m.getHall().getName(), m.isActive()});
        };
        refresh.run();

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBackground(COL_BACKGROUND);
        JButton addBtn = createStyledButton("Add Movie", COL_ACCENT);
        JButton delBtn = createStyledButton("Delete", COL_PRIMARY);
        JButton toggleBtn = createStyledButton("Toggle Status", new Color(70,70,70));
        controls.add(addBtn); controls.add(delBtn); controls.add(toggleBtn);

        addBtn.addActionListener(e -> showAddMovieDialog(refresh));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) { db.removeMovie(db.getMovies().get(row)); refresh.run(); }
        });
        toggleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) { Movie m = db.getMovies().get(row); m.setActive(!m.isActive()); refresh.run(); }
        });

        moviePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        moviePanel.add(controls, BorderLayout.SOUTH);

        // Hall Management
        JPanel hallPanel = new JPanel(new BorderLayout(10, 10));
        hallPanel.setBackground(COL_BACKGROUND); hallPanel.setBorder(new EmptyBorder(20,20,20,20));
        DefaultTableModel hallModel = new DefaultTableModel(new String[]{"Hall Name", "Capacity"}, 0);
        JTable hallTable = new JTable(hallModel);
        styleTable(hallTable);
        Runnable refreshHall = () -> {
            hallModel.setRowCount(0);
            for(Hall h : db.getHalls()) hallModel.addRow(new Object[]{h.getName(), h.getCapacity()});
        };
        refreshHall.run();
        JButton addHallBtn = createStyledButton("Add New Hall", COL_ACCENT);
        addHallBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Hall Name:");
            String cap = JOptionPane.showInputDialog("Capacity:");
            if(name != null && cap != null) { db.addHall(new Hall(name, Integer.parseInt(cap))); refreshHall.run(); }
        });
        JPanel hallControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hallControls.setBackground(COL_BACKGROUND); hallControls.add(addHallBtn);
        hallPanel.add(new JScrollPane(hallTable), BorderLayout.CENTER);
        hallPanel.add(hallControls, BorderLayout.SOUTH);

        tabs.addTab("Movies", moviePanel);
        tabs.addTab("Halls", hallPanel);
        for(int i=0; i<tabs.getTabCount(); i++) { tabs.setBackgroundAt(i, COL_SURFACE); tabs.setForegroundAt(i, Color.WHITE); }
        return wrapDashboard(tabs, "ADMIN DASHBOARD");
    }

    private void showAddMovieDialog(Runnable refreshCallback) {
        JDialog d = new JDialog(this, "Add Movie", true);
        d.setSize(450, 500); d.setLocationRelativeTo(this); d.setUndecorated(true);
        ((JPanel)d.getContentPane()).setBorder(new LineBorder(COL_PRIMARY, 2));

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBackground(COL_SURFACE); p.setBorder(new EmptyBorder(20,20,20,20));
        JTextField t = createStyledField(), g = createStyledField(), l = createStyledField(), price = createStyledField(), s = createStyledField();
        JComboBox<Hall> hBox = new JComboBox<>(db.getHalls().toArray(new Hall[0]));

        Color lblColor = COL_TEXT_SEC;
        p.add(new JLabel("Title:"){{setForeground(lblColor);}}); p.add(t);
        p.add(new JLabel("Genre:"){{setForeground(lblColor);}}); p.add(g);
        p.add(new JLabel("Language:"){{setForeground(lblColor);}}); p.add(l);
        p.add(new JLabel("Price:"){{setForeground(lblColor);}}); p.add(price);
        p.add(new JLabel("Showtime:"){{setForeground(lblColor);}}); p.add(s);
        p.add(new JLabel("Hall:"){{setForeground(lblColor);}}); p.add(hBox);

        JPanel btnPanel = new JPanel(new FlowLayout()); btnPanel.setBackground(COL_SURFACE);
        JButton save = createStyledButton("SAVE", COL_PRIMARY);
        JButton cancel = createStyledButton("CANCEL", Color.GRAY);
        btnPanel.add(save); btnPanel.add(cancel);

        save.addActionListener(e -> {
            try {
                Movie m = new Movie.MovieBuilder(t.getText()).setGenre(g.getText()).setLanguage(l.getText())
                        .setPrice(Double.parseDouble(price.getText())).setShowtime(s.getText())
                        .setHall((Hall)hBox.getSelectedItem()).build();
                db.addMovie(m); refreshCallback.run(); d.dispose();
            } catch(Exception ex) { JOptionPane.showMessageDialog(d, "Invalid Input"); }
        });
        cancel.addActionListener(e -> d.dispose());
        d.add(new JLabel(" NEW MOVIE"){{setFont(FONT_HEADER);setForeground(Color.WHITE);setOpaque(true);setBackground(COL_BACKGROUND);setHorizontalAlignment(0);}}, BorderLayout.NORTH);
        d.add(p, BorderLayout.CENTER); d.add(btnPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ==========================================
    // CUSTOMER PANEL
    // ==========================================
    private JPanel createCustomerPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BOLD); tabs.setBackground(COL_BACKGROUND); tabs.setForeground(Color.WHITE);

        // Browse Movies
        JPanel browsePanel = new JPanel(new BorderLayout(10, 10));
        browsePanel.setBackground(COL_BACKGROUND); browsePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        DefaultTableModel model = new DefaultTableModel(new String[]{"Title", "Genre", "Lang", "Time", "Price", "Rating"}, 0);
        JTable table = new JTable(model);
        styleTable(table);
        Runnable loadData = () -> {
            model.setRowCount(0);
            for(Movie m : db.getMovies()) if(m.isActive()) model.addRow(new Object[]{m.getTitle(), m.getGenre(), m.getLanguage(), m.getShowtime(), m.getPrice(), String.format("%.1f", m.getAverageRating())});
        };
        loadData.run();

        // --- FIXED SEARCH BAR ---
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.setBackground(COL_BACKGROUND);
        JTextField searchField = createStyledField(); searchField.setColumns(20);

        // Add DocumentListener for Real-time Search and Reset
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String q = searchField.getText().toLowerCase();
                model.setRowCount(0);
                for(Movie m : db.getMovies()) {
                    if(m.isActive()) {
                        // If query is empty, add everything. Else, check contains.
                        if (q.isEmpty() || m.getTitle().toLowerCase().contains(q) || m.getGenre().toLowerCase().contains(q)) {
                            model.addRow(new Object[]{m.getTitle(), m.getGenre(), m.getLanguage(), m.getShowtime(), m.getPrice(), String.format("%.1f", m.getAverageRating())});
                        }
                    }
                }
            }
        });

        searchBar.add(new JLabel("Filter: "){{setForeground(COL_TEXT_MAIN);setFont(FONT_BOLD);}});
        searchBar.add(searchField);
        // Removed "Search" button since it is now real-time, but you can keep a "Clear" button if you want

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(COL_BACKGROUND);
        JButton bookBtn = createStyledButton("Book Tickets", COL_PRIMARY);
        JButton reviewBtn = createStyledButton("Reviews", new Color(60,60,60));
        actions.add(reviewBtn); actions.add(bookBtn);

        bookBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if(r >= 0) showSeatSelection(findMovie((String)model.getValueAt(r, 0)));
        });
        reviewBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if(r >= 0) showReviewDialog(findMovie((String)model.getValueAt(r, 0)), loadData);
        });

        browsePanel.add(searchBar, BorderLayout.NORTH);
        browsePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        browsePanel.add(actions, BorderLayout.SOUTH);
        tabs.addTab("Browse Movies", browsePanel);

        // My Bookings
        DefaultListModel<String> bookingModel = new DefaultListModel<>();
        JList<String> bookingList = new JList<>(bookingModel);
        bookingList.setBackground(COL_BACKGROUND);
        bookingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel p = new JPanel(new BorderLayout()); p.setBackground(COL_SURFACE);
                p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, new Color(50,50,50)), new EmptyBorder(15,15,15,15)));
                JLabel l = new JLabel("🎟️  " + value.toString());
                l.setFont(FONT_PLAIN); l.setForeground(Color.WHITE); p.add(l, BorderLayout.CENTER);
                return p;
            }
        });
        tabs.addChangeListener(e -> {
            if(tabs.getSelectedIndex() == 1) {
                bookingModel.clear();
                if (currentUser instanceof Customer) {
                    for(String s : ((Customer)currentUser).bookings) bookingModel.addElement(s);
                }
            }
        });
        tabs.addTab("My Bookings", new JScrollPane(bookingList));
        for(int i=0; i<tabs.getTabCount(); i++) { tabs.setBackgroundAt(i, COL_SURFACE); tabs.setForegroundAt(i, Color.WHITE); }
        return wrapDashboard(tabs, "Hello, " + currentUser.username);
    }

    private Movie findMovie(String title) { return db.getMovies().stream().filter(m -> m.getTitle().equals(title)).findFirst().orElse(null); }

    // ==========================================
    // BOOKING LOGIC
    // ==========================================
    private void showSeatSelection(Movie m) {
        JDialog d = new JDialog(this, "Select Seats", true);
        d.setSize(700, 600); d.setLocationRelativeTo(this); d.setUndecorated(true);
        ((JPanel)d.getContentPane()).setBorder(new LineBorder(COL_PRIMARY, 2));

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(COL_BACKGROUND); main.setBorder(new EmptyBorder(20,20,20,20));
        JLabel screen = new JLabel("SCREEN"); screen.setOpaque(true); screen.setBackground(Color.GRAY); screen.setHorizontalAlignment(0); screen.setPreferredSize(new Dimension(100, 30));
        main.add(screen, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10)); grid.setBackground(COL_BACKGROUND);
        List<String> selectedSeats = new ArrayList<>();
        for(int i=1; i<=m.getHall().getCapacity(); i++) {
            String sName = "S" + i;
            JToggleButton btn = new JToggleButton(sName);
            if(m.getBookedSeats().contains(sName)) {
                btn.setEnabled(false); btn.setBackground(new Color(60, 0, 0));
            } else {
                btn.setBackground(new Color(40, 40, 40)); btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    if(btn.isSelected()) { btn.setBackground(Color.GREEN); btn.setForeground(Color.BLACK); selectedSeats.add(sName); }
                    else { btn.setBackground(new Color(40, 40, 40)); btn.setForeground(Color.WHITE); selectedSeats.remove(sName); }
                });
            }
            grid.add(btn);
        }

        JPanel bottom = new JPanel(new FlowLayout()); bottom.setBackground(COL_BACKGROUND);
        JButton payBtn = createStyledButton("Checkout", COL_PRIMARY);
        JButton closeBtn = createStyledButton("Cancel", Color.GRAY);
        payBtn.addActionListener(e -> {
            if(selectedSeats.isEmpty()) JOptionPane.showMessageDialog(d, "Select seats!");
            else { d.dispose(); showPayment(m, selectedSeats); }
        });
        closeBtn.addActionListener(e -> d.dispose());
        bottom.add(payBtn); bottom.add(closeBtn);
        main.add(new JScrollPane(grid), BorderLayout.CENTER); main.add(bottom, BorderLayout.SOUTH);
        d.add(main); d.setVisible(true);
    }

    private void showPayment(Movie m, List<String> seats) {
        Ticket ticket = new MovieTicket(m);

        JCheckBox pop = new JCheckBox("Popcorn ($8)"); pop.setBackground(COL_SURFACE); pop.setForeground(Color.WHITE);
        JCheckBox soda = new JCheckBox("Soda ($4)"); soda.setBackground(COL_SURFACE); soda.setForeground(Color.WHITE);

        Object[] msg = {"Unit Price: $" + ticket.getCost(), pop, soda};
        int res = JOptionPane.showConfirmDialog(this, msg, "Add Snacks?", JOptionPane.OK_CANCEL_OPTION);

        if(res == JOptionPane.OK_OPTION) {
            if(pop.isSelected()) ticket = new Popcorn(ticket);
            if(soda.isSelected()) ticket = new Soda(ticket);

            double totalCost = ticket.getCost() * seats.size();

            String[] opts = {"Credit Card", "Cash"};
            int type = JOptionPane.showOptionDialog(this,
                    "Tickets: " + seats.size() + "\n" +
                            "Item: " + ticket.getDescription() + "\n" +
                            "TOTAL: $" + totalCost,
                    "Payment", 0, 1, null, opts, opts[0]);

            PaymentStrategy ps = (type == 0) ? new CreditCardStrategy("1234") : new CashStrategy();
            if(ps.pay(totalCost)) {
                m.getBookedSeats().addAll(seats);

                // --- FIXED REDUNDANT TITLE ---
                String details = seats.size() + "x [" + ticket.getDescription() + "]";

                if(currentUser instanceof Customer) {
                    ((Customer)currentUser).bookings.add(details);
                }

                // ============================================================
                //  TRIGGER OBSERVER PATTERN (Email + Logs)
                // ============================================================
                // This notifies the system that a booking happened so it can
                // send emails and log revenue automatically.
                db.notifyObservers(currentUser.username, m.getTitle());
                // ============================================================

                JOptionPane.showMessageDialog(this, "Booked Successfully!");
            }
        }
    }

    private void showReviewDialog(Movie m, Runnable callback) {
        JDialog d = new JDialog(this, "Reviews", true);
        d.setSize(500, 500); d.setLocationRelativeTo(this); d.setUndecorated(true);
        ((JPanel)d.getContentPane()).setBorder(new LineBorder(COL_ACCENT, 2));
        JPanel p = new JPanel(new BorderLayout(10,10)); p.setBackground(COL_BACKGROUND); p.setBorder(new EmptyBorder(15,15,15,15));
        JTextArea area = new JTextArea(m.getReviewsSummary()); area.setEditable(false); area.setBackground(COL_SURFACE); area.setForeground(COL_TEXT_MAIN);
        JPanel input = new JPanel(new BorderLayout(5,5)); input.setBackground(COL_BACKGROUND);
        JTextField comment = createStyledField();
        JComboBox<String> rateBox = new JComboBox<>(new String[]{"5", "4", "3", "2", "1"});
        JButton post = createStyledButton("Post", COL_PRIMARY);
        JButton close = createStyledButton("Close", Color.GRAY);

        JPanel btns = new JPanel(); btns.setBackground(COL_BACKGROUND); btns.add(post); btns.add(close);
        input.add(comment, BorderLayout.CENTER); input.add(rateBox, BorderLayout.EAST); input.add(btns, BorderLayout.SOUTH);

        post.addActionListener(e -> {
            m.addReview(new Review(currentUser.username, comment.getText(), Double.parseDouble((String)rateBox.getSelectedItem())));
            area.setText(m.getReviewsSummary()); callback.run();
        });
        close.addActionListener(e -> d.dispose());
        p.add(new JScrollPane(area), BorderLayout.CENTER); p.add(input, BorderLayout.SOUTH);
        d.add(p); d.setVisible(true);
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private JPanel wrapDashboard(JComponent content, String titleText) {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(10, 10, 10)); top.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel l = new JLabel(titleText.toUpperCase()); l.setForeground(COL_PRIMARY); l.setFont(FONT_HEADER);
        JButton out = createStyledButton("Logout", new Color(60,60,60));
        out.setFont(new Font("Segoe UI", Font.BOLD, 12)); out.setBorder(new EmptyBorder(5, 15, 5, 15));
        out.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        top.add(l, BorderLayout.WEST); top.add(out, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH); p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField(15);
        field.setFont(FONT_PLAIN); field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE); field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(70,70,70), 1), new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(15);
        field.setFont(FONT_PLAIN); field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE); field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(70,70,70), 1), new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35); table.setFont(FONT_PLAIN); table.setShowVerticalLines(false);
        table.setBackground(COL_SURFACE); table.setForeground(COL_TEXT_MAIN);
        table.setSelectionBackground(COL_PRIMARY); table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(60,60,60));
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD); header.setBackground(new Color(45, 45, 45)); header.setForeground(COL_TEXT_SEC);
        header.setBorder(BorderFactory.createMatteBorder(0,0,2,0, COL_PRIMARY));
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setHorizontalAlignment(JLabel.CENTER);
    }

    class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(bgColor); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius)); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompleteCinemaSystem().setVisible(true));
    }
}