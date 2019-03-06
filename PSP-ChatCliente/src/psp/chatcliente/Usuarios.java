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

    // Para identificar los mensajes que se usan para contar a los usuarios dentro del chat.
    final String mensajeUsuarios = "/*usuarios";

    /**
     * Cuando se crea el socket para el usuario este ya empieza el hilo propio
     *
     * @param oldSocket
     */
    public Usuarios(Socket oldSocket) {

        try {
            new hilo(oldSocket).start();
        } catch (IOException ex) {
            Logger.getLogger(Usuarios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Clase que crea hilos de cada cliente
     */
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
            synchronized (this) {
                try {
                    oldIs = newSocket.getInputStream();
                    oldOs = newSocket.getOutputStream();

                    // Variable Local para controlar un error del systema
                    int contador = 0;

                    // Se hace un do while para quedar a la espera de nuevas respuestas al usuario
                    do {
                        // Mensaje por consola para controlar un error.
                        System.out.println("Respuesta " + (contador += 1));

                        respuesta = new byte[65535];

                        oldIs.read(respuesta);

                        // Condición para separar los mensajes que recibe del servidor,
                        //la condición es utilizada para sacar la cantidad de usuarios que hay conectados ahora mismo, si no, escribe el texto
                        if (new String(respuesta).contains(mensajeUsuarios)) {
                            main.LnumClientes.setText(new String(respuesta).split(mensajeUsuarios)[1]);
                        } else {
                            txtArea(respuesta);
                        }

                    } while (true);
                } catch (IOException e) {
                    System.out.println(e);
                }

            }
        }
    }

    /**
     * Metodo para insertar el texto en el TextArea de la clase Main
     *
     * @param mensaje
     */
    public void txtArea(byte[] mensaje) {

        String msnTxtServer = new String(mensaje);
        main.txArea.append(msnTxtServer + "\n");

    }

}
