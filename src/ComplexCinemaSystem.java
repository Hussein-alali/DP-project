import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

// ==========================================
// 1. PATTERN: STRATEGY (Payment)
// ==========================================
interface PaymentStrategy {
    boolean pay(double amount);
}

class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;
    public CreditCardStrategy(String cardNumber) { this.cardNumber = cardNumber; }
    @Override public boolean pay(double amount) { return cardNumber.length() > 3; } // Simple validation
    @Override public String toString() { return "Credit Card"; }
}

class CashStrategy implements PaymentStrategy {
    @Override public boolean pay(double amount) { return true; }
    @Override public String toString() { return "Cash"; }
}

// ==========================================
// 2. DOMAIN OBJECTS (Movies, Halls, Reviews)
// ==========================================
class Review {
    String user;
    String comment;
    double rating;

    public Review(String user, String comment, double rating) {
        this.user = user;
        this.comment = comment;
        this.rating = rating;
    }
}

class Hall {
    private String name;
    private int capacity;
    public Hall(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int c) { this.capacity = c; }
    @Override public String toString() { return name + " (" + capacity + " seats)"; }
}

class Movie {
    private String id;
    private String title;
    private String genre;
    private String language;
    private double price;
    private String showtime;
    private Hall assignedHall;
    private boolean isActive;
    private List<String> bookedSeats;
    private List<Review> reviews;

    // Builder Pattern
    private Movie(MovieBuilder builder) {
        this.id = UUID.randomUUID().toString();
        this.title = builder.title;
        this.genre = builder.genre;
        this.language = builder.language;
        this.price = builder.price;
        this.showtime = builder.showtime;
        this.assignedHall = builder.hall;
        this.isActive = true;
        this.bookedSeats = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    // Getters & Setters for Edit Functionality
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getGenre() { return genre; }
    public String getLanguage() { return language; }
    public double getPrice() { return price; }
    public String getShowtime() { return showtime; }
    public Hall getHall() { return assignedHall; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public List<String> getBookedSeats() { return bookedSeats; }

    // Review Logic
    public void addReview(Review r) { reviews.add(r); }
    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToDouble(r -> r.rating).average().orElse(0.0);
    }
    public String getReviewsSummary() {
        if(reviews.isEmpty()) return "No reviews yet.";
        StringBuilder sb = new StringBuilder();
        for(Review r : reviews) sb.append(r.user).append(": ").append(r.comment).append("\n");
        return sb.toString();
    }

    public static class MovieBuilder {
        private String title;
        private String genre;
        private String language;
        private double price;
        private String showtime;
        private Hall hall;

        public MovieBuilder(String title) { this.title = title; }
        public MovieBuilder setGenre(String g) { this.genre = g; return this; }
        public MovieBuilder setLanguage(String l) { this.language = l; return this; }
        public MovieBuilder setPrice(double p) { this.price = p; return this; }
        public MovieBuilder setShowtime(String s) { this.showtime = s; return this; }
        public MovieBuilder setHall(Hall h) { this.hall = h; return this; }
        public Movie build() { return new Movie(this); }
    }
}

// ==========================================
// 3. SINGLETON DATABASE
// ==========================================
class CinemaData {
    private static CinemaData instance;
    private List<User> users = new ArrayList<>();
    private List<Movie> movies = new ArrayList<>();
    private List<Hall> halls = new ArrayList<>();

    private CinemaData() {
        // Seed Data
        users.add(new Admin("admin", "123"));
        users.add(new Customer("user", "123"));

        Hall h1 = new Hall("Hall A", 20); // Small hall
        Hall h2 = new Hall("IMAX Hall", 50); // Large hall
        halls.add(h1); halls.add(h2);

        movies.add(new Movie.MovieBuilder("Inception").setGenre("Sci-Fi").setLanguage("English").setPrice(12).setShowtime("18:00").setHall(h1).build());
        movies.add(new Movie.MovieBuilder("Parasite").setGenre("Thriller").setLanguage("Korean").setPrice(10).setShowtime("20:00").setHall(h2).build());
    }

    public static CinemaData getInstance() {
        if (instance == null) instance = new CinemaData();
        return instance;
    }

    public User login(String u, String p) {
        return users.stream().filter(user -> user.username.equals(u) && user.verify(p)).findFirst().orElse(null);
    }

    public void addMovie(Movie m) { movies.add(m); }
    public void removeMovie(Movie m) { movies.remove(m); }
    public List<Movie> getMovies() { return movies; }
    public List<Hall> getHalls() { return halls; }
    public void addHall(Hall h) { halls.add(h); }
    public void register(User u) { users.add(u); }
}

// ==========================================
// 4. USERS (Factory Pattern logic)
// ==========================================
abstract class User {
    protected String username;
    protected String password;
    public User(String u, String p) { this.username = u; this.password = p; }
    public boolean verify(String p) { return password.equals(p); }
    public abstract String getRole();
}
class Admin extends User { public Admin(String u, String p) { super(u, p); } @Override public String getRole() { return "ADMIN"; } }
class Customer extends User {
    List<String> bookings = new ArrayList<>();
    public Customer(String u, String p) { super(u, p); }
    @Override public String getRole() { return "CUSTOMER"; }
}

