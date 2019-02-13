package psp.chatservidor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Alberto
 */
public class Conexion {

    final String ipDefecto = "localhost";
    final int puertoDefecto = 5555;

    int usuarios = 0;

    ServerSocket serverSocket;
    InetSocketAddress addr;
    Socket newSocket;

    public Conexion() {

        try {

            serverSocket = new ServerSocket();

            String[] botones = {"Aceptar", "Por defecto", "Cancelar"};

            JPanel panel = new JPanel();
            panel.add(new JLabel("Escriba un puerto antes de pulsar aceptar: "));
            JTextField textField = new JTextField(10);
            panel.add(textField);

            // Función que habilita un JOptionPane con 3 botones y recoja el botón seleccionado
            int respuesta = JOptionPane.showOptionDialog(null, panel, "Introduzca un puerto",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, botones, null);

            // Switch que dependiendo de la respuesta, hace sus respectivas funciones
            switch (respuesta) {
                // En caso de darle al botón aceptar, recoge el puerto de la caja de texto
                case JOptionPane.YES_OPTION:
                    int intPuerto = Integer.parseInt(textField.getText());
                    addr = new InetSocketAddress(ipDefecto, intPuerto);
                    //      System.out.println(addr);
                    break;
                // En este se le mete un puerto por defecto
                case JOptionPane.NO_OPTION:
                    addr = new InetSocketAddress(ipDefecto, puertoDefecto);

                    break;
                // Cierra el servidor
                default:
                    serverSocket.close();
                    System.exit(0);
                //      System.out.println("Terminado");
            }

            serverSocket.bind(addr);

            // Se hace el bucle do while para que el servidor quede a la escucha nuevas conexiónes con otro servidor
            do {

                newSocket = serverSocket.accept();

                new hilo(newSocket).start();
                usuarios += 1;
            } while (usuarios <= 10);

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Se crea un nuevo hilo, cada nuevo cliente se conecte a la máquina
     */
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

                    String respuesta = new String(mensaje);

                    System.out.println("Mensaje que recibimos " + respuesta);
                    // Borra cache y luego envia el mensaje
                    oldOs.flush();
                    oldOs.write(respuesta.getBytes());

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
