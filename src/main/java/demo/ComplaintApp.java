package demo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ComplaintApp {

    // Domain Enums
    public enum Role { USER, ADMIN }
    public enum Priority { Low, Medium, High, Critical }
    public enum Category { Facility, IT, Academic, Finance, Other }
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    // Domain Models
    public record User(long id, String name, String email, Role role) {}

    public static class Complaint {
        public final long id;
        public final long userId;
        public String title;
        public String description;
        public Category category;
        public Priority priority;
        public Status status;
        public Instant createdAt;
        public Long assignee;

        public Complaint(long id, long userId, String title, String description, Category category, Priority priority) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = priority;
            this.status = Status.OPEN;
            this.createdAt = Instant.now();
        }
    }

    // Concurrent In-Memory Stores
    private static final Map<Long, User> users = new ConcurrentHashMap<>();
    private static final Map<Long, Complaint> complaints = new ConcurrentHashMap<>();
    private static final AtomicLong compSeq = new AtomicLong(1000);

    public static void main(String[] args) throws IOException {
        // Seed initial data
        users.put(101L, new User(101, "Admin User", "admin@example.com", Role.ADMIN));
        users.put(102L, new User(102, "Student User", "user@example.com", Role.USER));

        Complaint seed1 = new Complaint(compSeq.incrementAndGet(), 102, "Lab WiFi Disconnected", "WiFi connection drops every 5 mins in IT Lab 2", Category.IT, Priority.High);
        seed1.status = Status.IN_PROGRESS;
        seed1.assignee = 101L;
        complaints.put(seed1.id, seed1);

        Complaint seed2 = new Complaint(compSeq.incrementAndGet(), 102, "Water Leakage", "Pipe leaking in Hostel B washroom", Category.Facility, Priority.Critical);
        complaints.put(seed2.id, seed2);

        // Start HttpServer on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new DashboardHandler());
        server.createContext("/submit", new SubmitComplaintHandler());
        server.createContext("/action", new ActionHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("====================================================");
        System.out.println("Online Complaint Management Dashboard Live!");
        System.out.println("Access UI at: http://localhost:8080");
        System.out.println("====================================================");
    }

    // Dashboard UI Handler
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Complaint Portal</title>");
            html.append("<style>");
            html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f4f6f9; margin: 0; padding: 20px; }");
            html.append(".container { max-width: 1000px; margin: auto; background: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
            html.append("h1, h2 { color: #2c3e50; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
            html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e1e8ed; }");
            html.append("th { background-color: #3498db; color: white; }");
            html.append(".form-group { margin-bottom: 12px; }");
            html.append(".form-group label { display: block; margin-bottom: 5px; font-weight: bold; }");
            html.append(".form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }");
            html.append("button { background: #2ecc71; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold; }");
            html.append("button:hover { background: #27ae60; }");
            html.append(".btn-action { background: #e67e22; padding: 5px 10px; font-size: 12px; margin-right: 5px; }");
            html.append(".btn-close { background: #e74c3c; }");
            html.append(".badge { padding: 4px 8px; border-radius: 4px; color: white; font-size: 12px; font-weight: bold; }");
            html.append(".OPEN { background: #f39c12; } .IN_PROGRESS { background: #3498db; } .RESOLVED { background: #2ecc71; } .CLOSED { background: #7f8c8d; }");
            html.append("</style></head><body>");

            html.append("<div class='container'>");
            html.append("<h1>📋 Online Complaint Management System</h1>");
            html.append("<p>Role-based Web Portal (Native Java HTTP Engine)</p>");

            // Complaint Submission Form
            html.append("<h2>Submit New Complaint</h2>");
            html.append("<form action='/submit' method='POST'>");
            html.append("<div class='form-group'><label>Title</label><input type='text' name='title' required minlength='3'/></div>");
            html.append("<div class='form-group'><label>Description</label><textarea name='description' required minlength='10'></textarea></div>");
            html.append("<div class='form-group'><label>Category</label><select name='category'>");
            for (Category c : Category.values()) html.append("<option value='").append(c).append("'>").append(c).append("</option>");
            html.append("</select></div>");
            html.append("<div class='form-group'><label>Priority</label><select name='priority'>");
            for (Priority p : Priority.values()) html.append("<option value='").append(p).append("'>").append(p).append("</option>");
            html.append("</select></div>");
            html.append("<button type='submit'>Submit Ticket</button>");
            html.append("</form>");

            // Complaint Management Table
            html.append("<h2>Active Complaint Tickets</h2>");
            html.append("<table><tr><th>ID</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th>Assignee</th><th>Admin Actions</th></tr>");

            for (Complaint c : complaints.values().stream().sorted(Comparator.comparing(x -> x.id)).toList()) {
                String assigneeName = c.assignee != null ? users.get(c.assignee).name() : "Unassigned";
                html.append("<tr>")
                    .append("<td>#").append(c.id).append("</td>")
                    .append("<td><strong>").append(escapeHtml(c.title)).append("</strong><br/><small>").append(escapeHtml(c.description)).append("</small></td>")
                    .append("<td>").append(c.category).append("</td>")
                    .append("<td>").append(c.priority).append("</td>")
                    .append("<td><span class='badge ").append(c.status).append("'>").append(c.status).append("</span></td>")
                    .append("<td>").append(assigneeName).append("</td>")
                    .append("<td>")
                    .append("<a href='/action?id=").append(c.id).append("&type=assign'><button class='btn-action'>Assign Admin</button></a>")
                    .append("<a href='/action?id=").append(c.id).append("&type=resolve'><button class='btn-action'>Resolve</button></a>")
                    .append("<a href='/action?id=").append(c.id).append("&type=close'><button class='btn-action btn-close'>Close</button></a>")
                    .append("</td>")
                    .append("</tr>");
            }
            html.append("</table>");
            html.append("</div></body></html>");

            sendResponse(exchange, 200, html.toString());
        }
    }

    // Handler for creating complaints
    static class SubmitComplaintHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String formData = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);

                long id = compSeq.incrementAndGet();
                Complaint c = new Complaint(
                        id, 102,
                        params.getOrDefault("title", "Untitled"),
                        params.getOrDefault("description", "No Details"),
                        Category.valueOf(params.getOrDefault("category", "Other")),
                        Priority.valueOf(params.getOrDefault("priority", "Low"))
                );
                complaints.put(id, c);
            }
            redirect(exchange, "/");
        }
    }

    // Handler for ticket actions (Assign, Resolve, Close)
    static class ActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                Map<String, String> params = parseFormData(query);
                long id = Long.parseLong(params.get("id"));
                String type = params.get("type");

                Complaint c = complaints.get(id);
                if (c != null) {
                    switch (type) {
                        case "assign" -> {
                            c.assignee = 101L;
                            c.status = Status.IN_PROGRESS;
                        }
                        case "resolve" -> c.status = Status.RESOLVED;
                        case "close" -> c.status = Status.CLOSED;
                    }
                }
            }
            redirect(exchange, "/");
        }
    }

    // Utility Functions
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                map.put(java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escapeHtml(String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}