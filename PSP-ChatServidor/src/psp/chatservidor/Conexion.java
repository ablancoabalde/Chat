package psp.chatservidor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    final String mensajeBienvenida = "Bienvenido ";

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

            // Se hace el bucle do while para que el servidor quede a la escucha nuevas conexiónes con otro cliente
            do {

               if(usuarios >= 3){
                    System.out.println("Demasiados clientes");
               }else{
                            
                    newSocket = serverSocket.accept();

                    new hilo(newSocket).start();
                    usuarios += 1;                
               }             

            } while (usuarios <= 3);

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
        // Para enviar el nombre de usuario
        Boolean primerMensaje = false;

        public hilo(Socket newSocket) throws IOException {

            oldSocket = newSocket;

        }

        @Override
        public void run() {

            try {
                oldIs = oldSocket.getInputStream();
                oldOs = oldSocket.getOutputStream();
                // Se hace un do while para quedar a la espera de nuevos Usuarios
                int contador = 0;
                do {
                    // Esto del primer mensaje es para que cuando un nuevo cliente se conecte le mande un mensaje de bienvenida con su nombre.
                    if (primerMensaje == false) {
                        mensaje = new byte[2000];
                        int read = oldIs.read(mensaje);
                        // Como es la primera vez que entra pues creamos el objeto Clientes con el socket asigando y su nombre.
                        new Clientes(oldSocket, new String(mensaje));

                        String respuesta = mensajeBienvenida + new String(mensaje);

                        // Para ver por consola que recibimos
                        System.out.println("Mensaje que recibimos " + respuesta);

                        // Recorremos el Arraylist creado en la clase clientes y enviamos el mensaje de bienvenida a todos los 
                        // sockets, para eso utilizamos el .getOutputStream() junto a su metodo write para enviar
                        for (Clientes cli : Clientes.clientes) {
                            // La función trim la utilizo para que si el string tuviera algún espacio al final que lo elimine
                            cli.getSocket().getOutputStream().write(respuesta.trim().getBytes());

                        }
                        // Ponemos la variable local a true para que no vuelva a entrar
                        primerMensaje = true;
                    } else {
                        String respuesta = null;

                        // Saber si en consola se está conectado bien y no genera más clientes de los que hay
                        System.out.println("Servidor " + (contador += 1));
                        mensaje = new byte[2000];

                        int read = oldIs.read(mensaje);

                        System.out.println("int read " + read);

                        // Aquí se hacen 2 bucles, el 1º lo que hace es identificar que cliente envía el mensaje a traves de su socket
                        // cuando lo encuentra agrega su nombre al mensaje escrito.
                        for (Clientes cli : Clientes.clientes) {

                            if (cli.getSocket() == oldSocket) {
                                respuesta = cli.getNick() + " " + new String(mensaje);
                                System.out.println("Mensaje que recibimos " + respuesta);

                            }

                        }

                        // Aquí envio el mensaje a todos los clientes del Arraylis. No hago esto en el bucle anterior,
                        //pues sí el mensaje que envio no es del primer cliente en la lista, me enviaría un mensaje Null y con ello una excepción                        
                        for (Clientes cli : Clientes.clientes) {
                            cli.getSocket().getOutputStream().write(respuesta.getBytes());
                        }

                    }
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
