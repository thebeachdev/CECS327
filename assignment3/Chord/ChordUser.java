import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;

>>>>>>> JamesBranchMaster
public class ChordUser{
  int port;
  public ChordUser(int p){
    port = p;
    Timer timer1 = new Timer();
    timer1.scheduleAtFixedRate(new TimerTask(){
      @Override
      public void run(){
        System.out.println("Usage: \n\tjoin <port>\n\twrite <file> (the file must be an integer stored in the working directory, i.e, ./port/file");
        System.out.println("\tread <file>\n\tdelete <file>\n\tprint");
        System.out.println("Enter: ");
        try{

          Chord chord = new Chord(port);//The errors are coming from here.
          Scanner scan= new Scanner(System.in);
          String delims = "[ ]+";
          String command = "";

          while (true) {
            String text = scan.nextLine();
            String[] tokens = text.split(delims);
            if (tokens[0].equals("join") && tokens.length == 2) {

>>>>>>> JamesBranchMaster
              try {
                chord.joinRing("localhost", Integer.parseInt(tokens[1]));
              }catch(IOException e) {
                System.out.println("Error joining the ring!");
              }
            }
            if (tokens[0].equals("print")){

                 chord.Print();
              }
            if (tokens[0].equals("write") && tokens.length == 2) {
>>>>>>> JamesBranchMaster
              try{
                String path;
                int guid = Integer.parseInt(tokens[1]);
                // If you are using windows you have to use

                path = ".\\"+  port +"\\"+Integer.parseInt(tokens[1]); // path to file
  			        //path = "./"+  port +"/"+guid; // path to file
>>>>>>> JamesBranchMaster
  			        FileStream file = new FileStream(path);
  		          ChordMessageInterface peer = chord.locateSuccessor(guid);
                peer.put(guid, file); // put file into ring
                //file is just an object,
                //NOTE: I'm not sure where to get the file or find it.
              }catch(FileNotFoundException e1){
                e1.printStackTrace();
                System.out.println("File was not found!");
              }catch (RemoteException e1) {
                e1.printStackTrace();
                System.out.println("File was not found!");
              }catch(IOException e){
                e.printStackTrace();
                System.out.println("Could not put file!");
              }
            }
            if (tokens[0].equals("read") && tokens.length == 2) {

>>>>>>> JamesBranchMaster
              try {//
                chord.get(Integer.parseInt(tokens[1]));
              }catch (IOException e) {
                System.out.println("Could not get file!");
              }
            }
            if (tokens[0].equals("delete") && tokens.length == 2) {

>>>>>>> JamesBranchMaster
              try {
                chord.delete(Integer.parseInt(tokens[1]));
              } catch (IOException e) {
                System.out.println("Could not delete file!");
              }
            }
          }
        }catch(RemoteException e){
        }
      }
    }, 1000, 1000);
  }
  static public void main(String args[]){
    if (args.length < 1 ) {
      throw new IllegalArgumentException("Parameter: <port>");
    }
    try {
      ChordUser chordUser=new ChordUser( Integer.parseInt(args[0]));
    }catch(Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}