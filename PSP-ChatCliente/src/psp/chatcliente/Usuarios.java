package psp.chatcliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alberto
 */
public class Usuarios {
    
    // Cuando se crea el socket para el usuario este ya empieza el hilo propio
    public Usuarios(Socket newSocket) {
        try {
            new hilo(newSocket).start();
        } catch (IOException ex) {
            Logger.getLogger(Usuarios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class hilo extends Thread {

        Socket oldSocket;
        InputStream oldIs;
        OutputStream oldOs;
        byte[] mensaje;

        public hilo(Socket newSocket) throws IOException {

            oldSocket = newSocket;

        }

        @Override
        public void run() {

            try {
                oldIs = oldSocket.getInputStream();
                oldOs = oldSocket.getOutputStream();
                // Se hace un do while para quedar a la espera de nuevos Usuarios
                do {

                    mensaje = new byte[2000];

                    oldIs.read(mensaje);

                    String txtServer = new String(mensaje);

                    main.txArea.append(txtServer + "\n");
                } while (true);

                //newSocket.close();
                // System.out.println("Cerrando el socket servidor");
                // serverSocket.close();
                //System.out.println("Terminado");
            } catch (IOException e) {
                System.out.println(e);
            }

        }
    }
}
