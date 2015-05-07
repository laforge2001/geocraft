/**
 * 
 */
package org.geocraft.io.jms;


public interface IJmsMessageContext {

  public static final String REPEATER_SELECTOR = "VolumeRepeater.";

  public static final String REPEATER_COMPLETER_SELECTOR = "VolumeRepeaterCompleter.";

  public static final String INIT_DESC_SELECTOR = "INIT_DESCIPTION";

  public static final String STATUS_MSG = "Status";

  public static final String COMMAND_MSG = "Command";

  public static final String READY = "Ready";

  public static final String RUNNING = "Running";

  public static final String ERROR = "Error";

  public static final String START = "Start";

  public static final String STOP = "Stop";

  public static final String UPDATE = "Update";

  public static final String CAN_QUIT = "Can Quit";

  public static final String RUN_NEXT = "Run Next";

  public static final String PARM_ID = "ParamId";

  public static final String DIRECTION = "Direction";

  public static final String FROM_SS = "FROM_SS";

  public static final String FROM_GC = "FROM_GC";

  public static final String FROM_MASTER_JOB = "FROM_MASTER_JOB";

  public static final String FROM_CHILD_JOB = "FROM_CHILD_JOB";

  public static final String EXEC_NUM = "EXEC_NUM";

  public static final String GATHER_STATUS = "GatherStatus";

  public static final int MAP_TAG = 1337;

  public static final int CMD_TAG = -1337;

  public static final int MASTER_JOB_TAG = 13371337;

  public static final int NUM_JOBS_TAG = 133337;

  public static final int OFFSET_RANGE_TAG = 1333377777;

  public static final String MASTER_JOB_ID = "MASTER_JOB_ID";

  public static final String NUM_JOBS_ID = "NUM_JOBS_ID";

  public static final String INVALID_EXEC_NUM_SET = "INVALID_EXEC_SET";

  public static final String EXEC_NUM_SET = "EXEC_NUM_SET";

  public static final String START_OFFSET_ID = "START_OFFSET_ID";

  public static final String END_OFFSET_ID = "END_OFFSET_ID";

  public static final String DELTA_OFFSET_ID = "DELTA_OFFSET_ID";

  public static final int INVALID_ID = -99;

  public static final int END_DATA_ID = -99;

  public static final int CREATE_DATA_ID = -98;

}
