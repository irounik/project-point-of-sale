package com.increff.ironic.pos.util;

import com.increff.ironic.pos.model.auth.UserPrincipal;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.pojo.UserPojo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityUtil {

    public static void createContext(HttpSession session) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute(Constants.SECURITY_CONTEXT, securityContext);
    }

    public static void setAuthentication(Authentication token) {
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UserPrincipal getPrincipal() {
        Authentication token = getAuthentication();
        return token == null ? null : (UserPrincipal) getAuthentication().getPrincipal();
    }

    public static UserRole getCurrentUserRole() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return UserRole.NONE;
        }

        boolean isSupervisor = auth.getAuthorities()
                .stream()
                .anyMatch(it -> it.getAuthority().equalsIgnoreCase(UserRole.SUPERVISOR.toString()));
        return isSupervisor ? UserRole.SUPERVISOR : UserRole.OPERATOR;
    }

    public static boolean isAuthenticated() {
        UserPrincipal principal = SecurityUtil.getPrincipal();
        if (principal == null) return false;
        return !ValidationUtil.isBlank(principal.getEmail());
    }

    public static void createAuthSession(UserPojo userPojo, HttpServletRequest req) {
        // Create authentication object
        Authentication authentication = ConversionUtil.convertToAuth(userPojo);

        // Create new session
        HttpSession session = req.getSession(true);

        // Attach Spring SecurityContext to this new session
        createContext(session);

        // Attach Authentication object to the Security Context
        setAuthentication(authentication);
    }

}
