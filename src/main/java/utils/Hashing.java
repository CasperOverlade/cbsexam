package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Hex;

public final class Hashing {

  // Sætter salt krypteringen
  private static String salt = "meta43fds";

  // TODO: You should add a salt and make this secure : FIX
  /*
  public static String md5(String rawString) {
    try {

      // We load the hashing algoritm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

      md.update(salt.getBytes());

      // We convert to byte array
      byte[] byteArray = md.digest(rawString.getBytes());

      // Initialize a string buffer
      StringBuffer sb = new StringBuffer();

      // Run through byteArray one element at a time and append the value to our stringBuffer
      for (int i = 0; i < byteArray.length; ++i) {
        sb.append(Integer.toHexString((byteArray[i] & 0xFF) | 0x100).substring(1, 3));
      }

      //Convert back to a single string and return
      return sb.toString();

    } catch (java.security.NoSuchAlgorithmException e) {

      //If somethings breaks
      System.out.println("Could not hash string");
    }

    return null;
  }
   */

  // TODO: You should add a salt and make this secure : FIX


  public static String sha(String rawString) {
    try {
      // Vi indlæser den hashing algoritme vi vil bruge
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // vi tilføjere salt
      digest.update(salt.getBytes());

      // Vi konvertere til byte array
      byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

      // Laver laver hashing strengen
      String sha256hex = new String(Hex.encode(hash));

      // og returnere strengen
      return sha256hex;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return rawString;
  }
}