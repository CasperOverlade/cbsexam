package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better - FIX
      //char[] key = {'C', 'B', 'S'};

      //Henter krypterings nøglen i config
      char[] key2 = Config.getEncryptKeyword();

      // Stringbuilder der gør det muligt at lege med strenge og lave brugbare ting
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?
      // Krypteringen foregår ved hjælp af XOR, som helt lavpraktisk, tager den første karakter i den
      // streng vi ønsker at kryptere, omdanner denne til binær, kombinere den med den første karakter
      // i vores XOR-nøgle omdannet til binær, for til sidst at omdanne kombinationen til en karakter igen.
      // Processen forsætter for hele strengen vi ønsker at kryptere. Hvis strengen er længere end XOR-nøglen,
      // vil den starte forfra på XOR-nøglens karakterer. Eksempel: Lad os antage at der første bogstav der
      // skal krypteres, er ’a’ og det første bogstav i krypteringsnøglen er ’b’. Den binære værdi af disse
      // er henholdsvis 0110 0001 og 0110 0010. Ved XOR krypteringen bliver 0000 0011, hvilket svarer
      // til tallet ’3’.
      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key2[i % key2.length]));
      }

      // Vi returnere den krypterede streng
      return thisIsEncrypted.toString();

    } else {
      // Vi returnere uden at have gjort noget
      return rawString;
    }
  }
}
