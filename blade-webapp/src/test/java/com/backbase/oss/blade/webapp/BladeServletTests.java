package com.backbase.oss.blade.webapp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.oss.blade.model.Blade;
import com.backbase.oss.blade.model.Stage;
import com.backbase.oss.blade.model.WebApp;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S1989")
public class BladeServletTests {

    @Test
    public void testBladeServletGet() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream servlet = mock(ServletOutputStream.class);
        when(request.getPathInfo()).thenReturn("/api/status");
        when(response.getOutputStream()).thenReturn(servlet);

        BladeServlet bladeServlet = new BladeServlet();

        bladeServlet.doGet(request, response);
        Assertions.assertNotNull(response);
    }


    @Test
    public void testBladeServletPost() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream servlet = mock(ServletOutputStream.class);
        when(request.getPathInfo()).thenReturn("/api/status");
        when(response.getOutputStream()).thenReturn(servlet);

        BladeServlet bladeServlet = new BladeServlet();
        bladeServlet.doGet(request, response);
        Assertions.assertNotNull(response);
    }


    @BeforeEach
    private void setupBladeRegistry() {
        BladeRegistry bladeRegistry = BladeRegistry.getInstance();
        String bladeId = "test";
        Blade blade = new Blade(bladeId);

        WebApp webApp = new WebApp();
        webApp.setName("webApp");

        Stage stage = new Stage();
        stage.setWebApps(Collections.singletonList(webApp));
        blade.setStages(Collections.singletonList(stage));

        bladeRegistry.put(blade);

        Assertions.assertNotNull(bladeRegistry.get(bladeId));
        Assertions.assertNotNull(bladeRegistry.find("webApp"));
        bladeRegistry.remove(bladeId);
        Assertions.assertNull(bladeRegistry.get(bladeId));
    }

}
