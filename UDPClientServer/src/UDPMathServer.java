import java.io.*;
import java.net.*;
import javax.script.ScriptException;

class UDPMathServer implements Runnable 
{
    private static final int Port = 1604;
    private boolean running = false;
    private static DatagramSocket dgs;

    // main entry point for the application
    public static void main(String args[]) 
    {
        try 
        {
            // startup
            UDPMathServer server = new UDPMathServer();
            Thread serverThread = new Thread(server);
            serverThread.start();


            // wait until finished
            System.out.println("Math server running, press enter to quit...");
            Console cons = System.console(); 
            String enterString = cons.readLine();

                // shutdown gracefully
                server.running = false;
                serverThread.interrupt();


        } 
        catch (Exception e) 
        {
            System.err.println("Math server error: " + e.toString());
        }
    }

    // constructor
    public UDPMathServer()
    {
        this.running = true;
    }

    // this is the server thread
    public void run() 
    {

        try
        {
            // TODO: create datagram socket on using the Port constant above

            dgs=new DatagramSocket(Port);


        }
        catch (Exception e)
        {
            System.err.println("Couldn't create datagram socket: " + e.toString());
        }

        while(this.running == true)
        {
            try
            {
                // TODO: create buffer and packet

                byte[] buf= new byte[256];
                DatagramPacket packet= new DatagramPacket(buf,buf.length);

                // TODO: receive packet via the datagram socket
                dgs.receive(packet);

                // TODO: convert the buffer in the packet into string variable message
                String message = new String(packet.getData());
                //System.out.println(message);
                // answer the math problem and print answer
                int mathAnswer = this.handleMathMessage(message);
                System.out.println("Received via UDP: " + message);
                System.out.println("The math answer to the message was : " + Integer.toString(mathAnswer));
                Thread.yield();
            }
            catch (Exception e)
            {
                System.err.println("Failed to receive UDP packet, general exception: " + e.toString());
                this.running = false;
                break;
            }
        }
    }

    private int handleMathMessage(String message) throws ScriptException{
        /*
         * TODO: handle your math application layer protocol message
         * this protocol must handle: add, subtract, multiply, and divide
         * each of these functions should take 2 integers
         * 
         * You must parse the string, perform the math, and then return
         * the integer value of the answer
         * 
         * You must handle negative numbers gracefully
         * 
         */

            String[] array = message.split(" ");
            int number1 = Integer.parseInt(array[0]);
            String operator = array[1];
            int number2 = Integer.parseInt(array[2]);
            int output = 0;

            switch (operator) {
                case "+":
                    output = number1 + number2;
                    break;
                case "-":
                    output = number1 - number2;
                    break;
                case "*":
                    output = number1 * number2;
                    break;
                case "/":
                    output = number1 / number2;
                    break;
                default:
                    System.out.println("Please select valid operation");


            }

        return output;
    }

}