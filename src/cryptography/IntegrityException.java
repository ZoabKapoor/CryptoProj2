package cryptography;

public class IntegrityException extends Exception
{
	private static final long serialVersionUID = 1L;

	//Parameterless Constructor
      public IntegrityException() {}

      //Constructor that accepts a message
      public IntegrityException(String message)
      {
         super(message);
      }
 }
