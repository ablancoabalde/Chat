
package psp.chatservidor;

import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Alberto
 */
public class Clientes {
    
    public static  ArrayList<Clientes> clientes = new ArrayList<Clientes>();
      Socket Socket;
      String nick;
      
    public Clientes(Socket rSocket, String rNick) {
        Socket=rSocket;
        nick=rNick;
        clientes.add(this);
    }

    public Socket getSocket() {
        return Socket;
    }

    public void setSocket(Socket Socket) {
        this.Socket = Socket;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
      
      

}
