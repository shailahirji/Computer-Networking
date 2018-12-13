import java.net.*;
import java.util.Scanner;


class UDPMathClient 
{
    private static final int Port = 1604;

    // main entry point for the application
    public static void main(String args[]) 
    {
        try 
        {
            boolean shouldQuit = false;
            while(!shouldQuit)
            {
                // TODO: print a menu with 4 math operations (add, subtract, multiple, divide) and "quit"
                String operand="";
                Scanner sc = new Scanner(System.in);
                String operation="";
                while(operand.equals("")) {
                    System.out.print("Please select an operation ");
                    System.out.print("Add(A) || Subtract(S) || Multiply(M) || Divide(D) ||  Quit(Q) : ");

                    // TODO: ask user for choice

                    sc = new Scanner(System.in);
                    operation = sc.next().toUpperCase();

                    switch (operation) {

                        case "A":
                            operand = "+";
                            break;
                        case "S":
                            operand = "-";
                            break;
                        case "M":
                            operand = "*";
                            break;
                        case "D":
                            operand = "/";
                            break;
                        case "Q":
                            operand = "Q";
                            break;
                        default:
                            System.out.println("Please select valid operation");
                    }
                }
                    // TODO: if "quit" then exit this loop
                    if (operand.equals("Q")) {
                        System.out.println("You have successfully exited the application");
                        shouldQuit = true;
                        break;
                    }

                // TODO: ask user for the two numbers to perform the math operation on
                System.out.println("Input 2 numbers for your operation");
                System.out.print("Number a: ");
                String number1=sc.next();
                System.out.print("Number b: ");
                String number2=sc.next();

                /*
                 * TODO: convert the math operation and two values into a string of your
                 * application layer math protocol that is handled by handleMathMessage
                 * in your UDP math server, store that string in mathProtocol
                 */ 
                String mathProtocol = number1+" "+operand+" "+number2+" ";


                try
                {

                    // TODO: create datagram socket
                    DatagramSocket ds= new DatagramSocket();

                    InetAddress localIpAddress = InetAddress.getLocalHost();
                    // TODO: create a datagram packet going to localIpAddress and Port containing the mathProtocol string
                    byte[] buf= mathProtocol.getBytes();
                    DatagramPacket packet= new DatagramPacket(buf,buf.length,localIpAddress,Port);
                    // TODO: send the packet
                    ds.send(packet);
                }
                catch (Exception e)
                {
                    System.err.println("Failed to create socket and send packet: " + e.toString());
                    shouldQuit = true;
                    break;
                }
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Math client error: " + e.toString());
        }
    }


}