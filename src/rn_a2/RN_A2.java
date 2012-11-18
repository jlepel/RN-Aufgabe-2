/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rn_a2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abe235
 */
public class RN_A2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        final ChatGUI gui = new ChatGUI();
        final LoginForm login = new LoginForm(gui);
        

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                login.setVisible(true);
            }
        });
        
       // Thread.sleep(100000);
    }
}
