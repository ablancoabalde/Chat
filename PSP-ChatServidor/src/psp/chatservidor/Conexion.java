package psp.chatservidor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Alberto
 */
public class Conexion {

    // Variables Constantes que no van a cambiar
    final String ipDefecto = "localhost";
    final int puertoDefecto = 5555;
    final String mensajeBienvenida = "Bienvenido ";
    final String mensajeIp = " Ip: ";
    final String mensajePuerto = " Puerto: ";
    final int maxClientes = 10;
    // Para identificar los mensajes que se usan para contar a los clientes dentro del chat.
    final String mensajeUsuarios = "/*usuarios";
    // Para identificar los mensajes que se usa cerrar a los clientes.
    final String mensajeCierre = "*/close";

    // Variable que inicializa los clientes a 
    int clientes = 0;
    // Condición para que el servidor quede a la escucha de nuevos clientes
    Boolean nuevosCli = true;

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

            // Switch que dependiendo de la recibimos, hace sus respectivas funciones
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
            while (nuevosCli) {
                newSocket = null;
                if (clientes > maxClientes) {
                    // Cuando llegues al máximo número de clientes el servidor queda enviando el mensaje por consola todo el tiempo
                    System.out.println("Demasiados clientes");
                    //newSocket = serverSocket.accept();
                    //algo(newSocket);
                } else {
                    newSocket = serverSocket.accept();
                    nHilo(newSocket);
                }

            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Inicia un nuevo hilo con el socket recibido y suma 1 a los clientes
     *
     * @param Socket
     */
    private void nHilo(Socket Socket) {

        try {

            new hilo(Socket).start();
            clientes += 1;

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
                Boolean condicionCierre = true;
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

                        // Creamos el mensaje de Cliente conectado
                        String recibimos = mensajeBienvenida + new String(mensaje) + mensajeIp + oldSocket.getLocalAddress() + mensajePuerto + oldSocket.getPort();

                        // Para ver por consola que recibimos
                        System.out.println("1º mensaje de conexión que recibimos: " + recibimos);
                       
                        
                        // Recorremos el Arraylist creado en la clase arrayClientes y enviamos 2 mensajes el de número de clientes conectados y el de bienvenida a todos los 
                        // sockets, para eso utilizamos el .getOutputStream() junto a su metodo write para enviar a todos los sockets almacenados.
                        for (Clientes cli : Clientes.arrayClientes) {

                            cli.getSocket().getOutputStream().write((mensajeUsuarios + Clientes.arrayClientes.size()).getBytes());
                            // La función trim la utilizo para que si el string tuviera algún espacio al final que lo elimine
                            cli.getSocket().getOutputStream().write(recibimos.trim().getBytes());

                        }
                        // Ponemos la variable local a true para que no vuelva a entrar
                        primerMensaje = true;
                    } else {

                        String recibimos = null;

                        mensaje = new byte[2000];

                        int read = oldIs.read(mensaje);

                        recibimos = new String(mensaje);

                        // Condición de que si el mensaje que recibimos contiene una seríe de caracteres, elimina al Cliente del arrayList
                        // descuenta un cliente y abre otra condición, para que, si el número de clientes llegue a 0 cierra al cliente y el servido
                        // y si no que solo cierre al cliente.
                        if (recibimos.contains(mensajeCierre)) {

                            clientes -= 1;
                            for (Iterator<Clientes> iterator = Clientes.arrayClientes.iterator(); iterator.hasNext();) {
                                Clientes obj = iterator.next();
                                System.out.println(obj.Socket);
                                if (obj.Socket.equals(oldSocket)) {
                                     enviarMensaje("Cliente desconectado");
                                    // Elimina el elemento encontrado
                                    iterator.remove();
                                }
                            }

                            if (recibimos.contains(mensajeCierre) && clientes == 0) {
                                oldSocket.close();
                                serverSocket.close();
                                condicionCierre = false;
                            } else {
                               
                                oldSocket.close();
                               
                            }

                            // Si no contiene mensaje de Cierre envía el mensaje recibido a todos los clientes 
                        } else {

                            enviarMensaje(new String(mensaje));

                        }

                    }
                } while (condicionCierre);

            } catch (IOException e) {
                System.out.println(e);
            }

        }

        private void enviarMensaje(String mensaje) {

            String recibimos = null;
            // Aquí se hacen 2 bucles, el 1º lo que hace es identificar que cliente envía el mensaje a traves de su socket
            // cuando lo encuentra agrega su nombre al mensaje escrito.
            for (Clientes cli : Clientes.arrayClientes) {

                if (cli.getSocket() == oldSocket) {
                    recibimos = cli.getNick() + " " + mensaje;
                    System.out.println("Mensaje que recibimos " + recibimos);

                }

            }

            // Aquí envio el mensaje a todos los arrayClientes del Arraylis. No hago esto en el bucle anterior,
            //pues sí el mensaje que envio no es del primer cliente en la lista, me enviaría un mensaje Null y con ello una excepción                        
            for (Clientes cli : Clientes.arrayClientes) {
                try {
                    cli.getSocket().getOutputStream().write((mensajeUsuarios + clientes).getBytes());
                    cli.getSocket().getOutputStream().write(recibimos.getBytes());
                } catch (IOException ex) {
                    Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
