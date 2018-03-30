package ua.rsa.gd.external.javadbf;

import java.io.PrintStream;
import java.io.PrintWriter;


public class JDBFException extends Exception
{
  //~ Instance fields ----------------------------------------------------------

 private static final long serialVersionUID = 1L;
	
private Throwable detail;

  //~ Constructors -------------------------------------------------------------

  public JDBFException(String s)
  {
    this(s, null);
  }

  public JDBFException(Throwable throwable)
  {
    this(throwable.getMessage(), throwable);
  }

  public JDBFException(String s, Throwable throwable)
  {
    super(s);
    detail = throwable;
  }

  //~ Methods ------------------------------------------------------------------

  public String getMessage()
  {
    if (detail == null)
      return super.getMessage();
    else

      return super.getMessage();
  }

  public void printStackTrace(PrintStream printstream)
  {
    if (detail == null)
    {
      super.printStackTrace(printstream);

      return;
    }

    PrintStream printstream1 = printstream;
    printstream1.println(this);
    detail.printStackTrace(printstream);

    return;
  }

  public void printStackTrace()
  {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintWriter printwriter)
  {
    if (detail == null)
    {
      super.printStackTrace(printwriter);

      return;
    }

    PrintWriter printwriter1 = printwriter;

    printwriter1.println(this);
    detail.printStackTrace(printwriter);

    return;
  }
}
