package com.backbase.oss.blade.utils;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import static org.junit.Assert.*;

public class BladeUtilsTest {

    @Test
    public void isPortAvailable() throws IOException {
        int port = 16132;
        assertTrue(BladeUtils.isPortAvailable(port));
        ServerSocket socket = new ServerSocket(port, 50, InetAddress.getByName("localhost"));
        assertFalse(BladeUtils.isPortAvailable(port));
        socket.close();
    }


}