/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.net.InetAddress;

import mpi.MPI;
import mpi.Status;


public class HelloWorldMPI {

  public static final int BUFSIZE = 128;

  public static final int TAG = 0;

  public static void main(String[] args) {
    try {
      char[] idstr = new char[32];
      char[] buff = new char[BUFSIZE];
      Status stat;

      String hostAddress = InetAddress.getLocalHost().getHostAddress();
      String hostName = InetAddress.getLocalHost().getHostName();
      //System.out.println("HOST: " + hostName + "(" + hostAddress + ")" + " LD_LIB_PATH="
      //     + System.getenv("LD_LIBRARY_PATH"));

      MPI.Init(args);
      int numprocs = MPI.COMM_WORLD.Size(); /* find out how big the SPMD world is */
      int myid = MPI.COMM_WORLD.Rank(); /* and this processes' rank is */

      /* At this point, all the programs are running equivalently, the rank is used to
         distinguish the roles of the programs in the SPMD model, with rank 0 often used
         specially... */
      if (myid == 0) {
        System.out.println("Node " + myid + ": We have " + numprocs + " processors\n");
        for (int i = 1; i < numprocs; i++) {
          String s = "Hello " + i;
          System.arraycopy(s.toCharArray(), 0, buff, 0, s.length());
          MPI.COMM_WORLD.Send(buff, 0, BUFSIZE, MPI.CHAR, i, TAG);
        }
        for (int i = 1; i < numprocs; i++) {
          stat = MPI.COMM_WORLD.Recv(buff, 0, BUFSIZE, MPI.CHAR, i, TAG);
          System.out.printf("%d: %s\n", myid, new String(buff));
        }
      } else {
        /* receive from rank 0: */
        stat = MPI.COMM_WORLD.Recv(buff, 0, BUFSIZE, MPI.CHAR, 0, TAG);
        String s = "Processor " + myid + " on " + hostName;
        System.arraycopy(s.toCharArray(), 0, idstr, 0, s.length());
        s = new String(idstr);
        s += " reporting for duty\n";
        System.arraycopy(s.toCharArray(), 0, buff, 0, s.length());
        /* send to rank 0: */
        MPI.COMM_WORLD.Send(buff, 0, BUFSIZE, MPI.CHAR, 0, TAG);
      }

      MPI.Finalize(); /* MPI Programs end with MPI Finalize; this is a weak synchronization point */
    } catch (Exception e) {
      System.out.println("************************************ " + e.toString());
    }
  }
}
