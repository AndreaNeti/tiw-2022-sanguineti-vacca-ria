package it.polimi.tiw.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;

public class LoginFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		HttpSession session = req.getSession();
		// redirect to home if already logged
		if (!(session.isNew() || session.getAttribute("user") == null)) {
			res.setStatus(HttpServletResponse.SC_OK);
			res.getWriter().println(((User) session.getAttribute("user")).getUsername());
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

}
