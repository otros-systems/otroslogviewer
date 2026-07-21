package pl.otros.logview.singleinstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;

/**
 * Provides a simple single-instance guard for the application.
 * <p>
 * The class uses an operating-system-level file lock to determine whether
 * another instance of the application is already running. The first process
 * that successfully obtains the lock is considered the primary application
 * instance. Any later process that cannot obtain the same lock is treated as
 * a secondary instance and should exit or delegate its request to the already
 * running process.
 * </p>
 * <p>
 * The lock file is created in the system temporary directory. The actual
 * protection is provided by {@link FileLock}, not by the mere existence of
 * the lock file. This means that a stale lock file alone does not necessarily
 * prevent the application from starting again.
 * </p>
 */
public class SimpleSingleInstance {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSingleInstance.class);

  private SimpleSingleInstance() {
  }

  /**
   * Checks whether the current process is the first running instance of the application.
   * <p>
   * The method attempts to create and lock a file named {@code OtrosSingleInstance.lock}
   * in the system temporary directory. If the lock can be acquired, the current process
   * is treated as the first instance. A shutdown hook is then registered to release the
   * lock, close the underlying file channel, and delete the lock file when the JVM exits.
   * </p>
   * <p>
   * If the lock cannot be acquired because another process already holds it, the method
   * returns {@code false}. If an {@link IOException} occurs while creating or locking the
   * file, the error is logged and the method currently returns {@code true}, allowing the
   * application to continue rather than preventing startup because of a lock-file problem.
   * </p>
   *
   * @return {@code true} if this process should continue as the first application instance;
   *         {@code false} if another instance already holds the lock
   */
  public static boolean checkIsFirstInstance() {
    try {
      // Build the lock file path inside the operating system's temporary directory.
      String lockFile = System.getProperty("java.io.tmpdir") + "/OtrosSingleInstance.lock";

      // Open the lock file for reading and writing and obtain its file channel.
      // The channel must remain open for as long as the lock is held.
      FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();

      // Try to acquire an exclusive lock without blocking.
      // If another application instance already owns the lock, tryLock() returns null.
      FileLock lock = channel.tryLock();

      if (lock == null) {
        // Another process already holds the lock, so this is not the first instance.
        return false;
      }

      LOGGER.info("The Application creates the lock file {}", lockFile);

      // Register cleanup logic that runs when the JVM shuts down.
      // This releases the OS-level lock, closes the file channel, and removes the lock file.
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          lock.release();
          channel.close();
          Files.delete(new File(lockFile).toPath());
        } catch (RuntimeException | IOException e) {
          LOGGER.error("Cannot close lock", e);
        }
      }));
    } catch (IOException e) {
      // If the lock cannot be created because of an I/O problem, log the error.
      // The application is allowed to continue in this case.
      LOGGER.error("Cannot create lock file to check if application is running", e);
    }

    // The lock was acquired successfully, or the check could not be performed because
    // of an I/O error. In both cases the caller is allowed to continue startup.
    return true;
  }
}