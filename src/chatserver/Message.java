/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.util.Date;
import org.json.simple.*;
/**
 *
 * @author Faigjaz
 */
public class Message {
    public String userName;
    public String message;
    public Date timeStamp;
    
    public Message(String userName,String message,Date timeStamp){
        this.userName = userName;
        this.message = message;
        this.timeStamp = timeStamp;
    }
}
