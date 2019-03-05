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
    public Usuarios(Socket oldSocket) {
       
        try {
            new hilo(oldSocket).start();
        } catch (IOException ex) {
            Logger.getLogger(Usuarios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class hilo extends Thread {

        Socket newSocket;
        InputStream oldIs;
        OutputStream oldOs;
        byte[] respuesta;

        public hilo(Socket oldSocket) throws IOException {

            newSocket = oldSocket;

        }

        @Override
        public void run() {

            try {
                oldIs = newSocket.getInputStream();
                oldOs = newSocket.getOutputStream();
                // Se hace un do while para quedar a la espera de nuevas respuestas al usuario
                int contador = 0;

                do {

                    System.out.println("Respuesta " + (contador += 1));
                    respuesta = new byte[65535];

                    oldIs.read(respuesta);

                    txtArea(respuesta);

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

    public void txtArea(byte[] otro) {
        String txtServer = new String(otro);

        main.txArea.append(txtServer + "\n");
    }

}
