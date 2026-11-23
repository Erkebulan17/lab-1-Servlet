package kz.edu.cookieprivacy;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        HttpSession session = req.getSession(false);
        Object username = session == null ? null : session.getAttribute("username");
        resp.getWriter().write(username == null ? "guest" : "authenticated:" + username);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        // Demo credentials can be overridden with DEMO_USERNAME and DEMO_PASSWORD environment variables.
        String expectedUser = System.getenv().getOrDefault("DEMO_USERNAME", "student");
        String expectedPassword = System.getenv().getOrDefault("DEMO_PASSWORD", "Cookie2026!");
        if (!constantTimeEquals(expectedUser, username) || !constantTimeEquals(expectedPassword, password)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Логин немесе пароль қате");
            return;
        }

        HttpSession session = req.getSession(true);
        req.changeSessionId();
        session.setAttribute("username", expectedUser);
        session.setMaxInactiveInterval(30 * 60);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) actual = "";
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
