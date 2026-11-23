package kz.edu.cookieprivacy;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@WebServlet("/consent")
public class ConsentServlet extends HttpServlet {
    private static final String CONSENT_COOKIE = "site_consent";
    private static final String ANALYTICS_COOKIE = "demo_analytics";
    private static final int MAX_AGE = 60 * 60 * 24 * 180;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        String value = findCookie(req, CONSENT_COOKIE);
        resp.getWriter().write(value == null ? "none" : value);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        boolean analytics = "true".equals(req.getParameter("analytics"));
        if (!"accept".equals(action) && !"reject".equals(action) && !"save".equals(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown consent action"); return;
        }
        // Essential cookies are always on. Optional analytics is enabled only by an explicit choice.
        if ("reject".equals(action)) analytics = false;
        String consentValue = "essential=true&analytics=" + analytics;
        addCookie(resp, req, CONSENT_COOKIE, consentValue, MAX_AGE, true);
        if (analytics) addCookie(resp, req, ANALYTICS_COOKIE, "enabled", MAX_AGE, false);
        else addCookie(resp, req, ANALYTICS_COOKIE, "", 0, false);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private static String findCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies).filter(c -> name.equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }

    private static void addCookie(HttpServletResponse resp, HttpServletRequest req, String name, String value, int age, boolean httpOnly) {
        String encoded = java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        String header = name + "=" + encoded + "; Path=" + contextPath(req) + "; Max-Age=" + age + "; SameSite=Lax";
        if (req.isSecure()) header += "; Secure";
        if (httpOnly) header += "; HttpOnly";
        header += "; Priority=Medium";
        resp.addHeader("Set-Cookie", header);
    }
    private static String contextPath(HttpServletRequest req) { return req.getContextPath().isEmpty() ? "/" : req.getContextPath() + "/"; }
}
