package sqlite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ParallelAccessTests extends DBConnectionFixture {
  private TestThread t1;
  private TestThread t2;

  protected void setUp() throws Exception {
    super.setUp();
    t1 = new TestThread();
    t2 = new TestThread();
    t1.exec("create table x (x)");
    t1.exec("insert into x values (1);");
    t1.exec("insert into x values (2);");
    t1.exec("insert into x values (3);");
  }

  protected void tearDown() throws Exception {
    Exception e1 = t1.finish();
    if (e1 != null) {
      e1.printStackTrace();
    }
    Exception e2 = t2.finish();
    if (e2 != null) {
      e2.printStackTrace();
    }
    if (e1 != null)
      throw e1;
    if (e2 != null)
      throw e2;
    super.tearDown();
  }

  public void testParallelReads() throws Exception {
    DBStatement st1 = t1.prepare("select x from x order by x");
    DBStatement st2 = t2.prepare("select x from x order by x");
    boolean b1, b2;
    b1 = t1.step(st1);
    assertTrue(b1);
    b1 = t1.step(st1);
    assertTrue(b1);
    b2 = t2.step(st2);
    assertTrue(b2);
    b1 = t1.step(st1);
    assertTrue(b1);
    b2 = t2.step(st2);
    assertTrue(b2);
    b1 = t1.step(st1);
    assertFalse(b1);
  }


  private class TestThread extends Thread {
    private Exception myException;
    private DBConnection myConnection;
    private List<DBRunnable> myQueue = new ArrayList<DBRunnable>();

    private TestThread() {
      start();
    }

    public void run() {
      try {
        myConnection = new DBConnection(new File(tempName("db")));
        myConnection.open();
        while (true) {
          DBRunnable r;
          synchronized (this) {
            if (myQueue.isEmpty()) {
              wait(500);
              continue;
            }
            r = myQueue.remove(0);
          }
          if (r == null)
            break;
          r.dbrun();
        }
      } catch (Exception e) {
        myException = e;
      } finally {
        try {
          myConnection.close();
        } catch (Exception e) {
          //
        }
      }
    }

    public void exec(final String sql) throws DBException, InterruptedException {
      perform(true, new DBRunnable() {
        public void dbrun() throws DBException {
          myConnection.exec(sql);
        }
      });
    }

    private void perform(boolean wait, final DBRunnable runnable) throws InterruptedException, DBException {
      if (!isAlive())
        return;
      if (this == Thread.currentThread()) {
        runnable.dbrun();
        return;
      }
      DBRunnable r = runnable;
      Semaphore p = null;
      if (wait) {
        final Semaphore sp = new Semaphore(1);
        sp.acquire();
        r = new DBRunnable() {
          public void dbrun() throws DBException {
            try {
              runnable.dbrun();
            } finally {
              sp.release();
            }
          }
        };
        p = sp;
      }
      synchronized (this) {
        myQueue.add(r);
        notify();
      }
      if (p != null) {
        while (!p.tryAcquire(500, TimeUnit.MILLISECONDS)) {
          if (!isAlive())
            return;
        }
      }
    }

    public Exception finish() throws InterruptedException {
      synchronized (this) {
        myQueue.add(null);
        notify();
      }
      join();
      return myException;
    }

    public DBStatement prepare(final String sql) throws DBException, InterruptedException {
      final DBStatement[] result = {null};
      perform(true, new DBRunnable() {
        public void dbrun() throws DBException {
          result[0] = myConnection.prepare(sql);
        }
      });
      return result[0];
    }

    public boolean step(final DBStatement statement) throws DBException, InterruptedException {
      final boolean[] result = {false};
      perform(true, new DBRunnable() {
        public void dbrun() throws DBException {
          result[0] = statement.step();
        }
      });
      return result[0];
    }
  }
}
