import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.security.MessageDigest;

public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface {
  public static final int M = 2;
  Registry registry;    // rmi registry for lookup the remote objects.
  ChordMessageInterface successor;
  ChordMessageInterface predecessor;
  ChordMessageInterface[] finger;
  int nextFinger;
  int i;   		// GUID
  public ChordMessageInterface rmiChord(String ip, int port) {
    ChordMessageInterface chord = null;
    try{
      Registry registry = LocateRegistry.getRegistry(ip, port);
      chord = (ChordMessageInterface)(registry.lookup("Chord"));
      return chord;
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch(NotBoundException e){
      e.printStackTrace();
    }
    return null;
  }
  public Boolean isKeyInSemiCloseInterval(int key, int key1, int key2){
    if (key1 < key2) {
      return (key > key1 && key <= key2);
    } else {
      return (key > key1 || key <= key2);
    }
  }
  public Boolean isKeyInOpenInterval(int key, int key1, int key2) {
    if (key1 < key2){
      return (key > key1 && key < key2);
    } else {
      return (key > key1 || key < key2);
    }
  }
  public void put(int guid, InputStream stream) throws RemoteException{
    //TODO Store the file at "./" port/repository/guid
    // "./port/repository/guid-hash"
    // convert guid into a md5 hash
    // create a new path to save off of that.,
    // save thhe has
    //
    try {
      String thingOfaKey = Integer.toString(guid);
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(thingOfaKey.getBytes());
      byte[] byteData = md.digest();
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < byteData.length; i++){
        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
      }
       System.out.println("Digest(in hex format):: "+sb.toString());
       System.out.println("MD5:: "+ new BigInteger(1, m.digest()).toString(1));
       String fileName = "./"+i+"/repository/" + guid;
       // read-up on fileoutputstream
      FileOutputStream output = new FileOutputStream(fileName);
      while (stream.available() > 0){
        output.write(stream.read());
        output.flush();
        output.close();
      }
     } catch (IOException e) {
       System.out.println(e);
     }
  }
  // this returns a file given a guid
  public InputStream get(int guid) throws RemoteException {
    // any arbitray number, with the md5,
    // I'm given a number
    // if the md5, and number passed in don't exist, neither does the fiel.
    // else return what it found.
    FileStream file = null;
    //TODO get  the file ./port/repository/guid
    return file;
  }
  public void delete(int guid) throws RemoteException {
        //TODO delete the file ./port/repository/guid
  }
  public int getId() throws RemoteException {
      return i;
  }
  public boolean isAlive() throws RemoteException {
    return true;
  }
  public ChordMessageInterface getPredecessor() throws RemoteException{
    return predecessor;
  }
  public ChordMessageInterface locateSuccessor(int key) throws RemoteException{
    if (key == i){
      throw new IllegalArgumentException("Key must be distinct that  " + i);
    }
    if (successor.getId() != i){
      if (isKeyInSemiCloseInterval(key, i, successor.getId())){
        return successor;
      }
      ChordMessageInterface j = closestPrecedingNode(key);
      if (j == null){
        return null;
      }
      return j.locateSuccessor(key);
    }
    return successor;
  }
  public ChordMessageInterface closestPrecedingNode(int key) throws RemoteException {
    // look for the nodes that have me this port number as a preceeding node, or who is related to me as a note. Other nodes will have records of other nodes.
    // TODO:
    return successor;
   }
  public void joinRing(String ip, int port)  throws RemoteException {
    try{
      System.out.println("Get Registry to joining ring");
      Registry registry = LocateRegistry.getRegistry(ip, port);
      ChordMessageInterface chord = (ChordMessageInterface)(registry.lookup("Chord"));
      predecessor = null;
      successor = chord.locateSuccessor(this.getId());
      System.out.println("Joining ring");
     } catch(RemoteException | NotBoundException e){
       successor = this;
     }
   }
  public void findingNextSuccessor() {
     int i;
     successor = this;
     for (i = 0;  i< M; i++) {
       try {
         if (finger[i].isAlive()) {
           successor = finger[i];
         }
       } catch(RemoteException | NullPointerException e) {
         finger[i] = null;
       }
     }
   }
  public void stabilize() {
     boolean error = false;
     try {
       if (successor != null) {
         ChordMessageInterface x = successor.getPredecessor();
         if (x != null && x.getId() != this.getId() && isKeyInOpenInterval(x.getId(), this.getId(), successor.getId())) {
           successor = x;
         }
         if (successor.getId() != getId()) {
           successor.notify(this);
         }
       }
     } catch(RemoteException | NullPointerException e1) {
       error = true;
     }
     if (error){
       findingNextSuccessor();
     }
   }
  public void notify(ChordMessageInterface j) throws RemoteException {
    if (predecessor == null || (predecessor != null && isKeyInOpenInterval(j.getId(), predecessor.getId(), i))){
      // TODO:
      // //transfer keys in the range [j,i) to j;
      predecessor = j;
    }
  }
  public void fixFingers() {
    int id = i;
    try {
      int nextId;
      if (nextFinger == 0){ // && successor != null)
        nextId = (this.getId() + (1 << nextFinger));
      } else{
        nextId = finger[nextFinger -1].getId();
      }
      finger[nextFinger] = locateSuccessor(nextId);

      if (finger[nextFinger].getId() == i){
        finger[nextFinger] = null;
      } else{
        nextFinger = (nextFinger + 1) % M;
      }
     } catch(RemoteException | NullPointerException e){
         finger[nextFinger] = null;
         e.printStackTrace();
     }
    }
  public void checkPredecessor() {
    try {
      if (predecessor != null && !predecessor.isAlive()){
        predecessor = null;
      }
    } catch(RemoteException e) {
      predecessor = null;
      e.printStackTrace();
    }
  }
  public Chord(int port) throws RemoteException {
    int j;
    finger = new ChordMessageInterface[M];
    for (j=0;j<M; j++){
      finger[j] = null;
    }
    // do we turn it into a guid here? no
    i = port;
    predecessor = null;
    successor = this;
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            stabilize();
            fixFingers();
            checkPredecessor();
        }
    }, 500, 500);
    try{
      // create the registry and bind the name and object.
      System.out.println("Starting RMI at port="+port);
      registry = LocateRegistry.createRegistry( port );
      registry.rebind("Chord", this);
    } catch(RemoteException e){
      throw e;
    }
  }
  void Print(){
      int i;
      try {
        if (successor != null){
          System.out.println("successor "+ successor.getId());
        }
        if (predecessor != null){
          System.out.println("predecessor "+ predecessor.getId());
        }
        for (i=0; i<M; i++) {
          try {
            if (finger != null)
            System.out.println("Finger "+ i + " " + finger[i].getId());
          } catch(NullPointerException e) {
            finger[i] = null;
          }
        }
     } catch(RemoteException e) {
         System.out.println("Cannot retrive id");
      }
  }
}
