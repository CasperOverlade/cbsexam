package utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import model.User;
import java.util.Date;

public final class Token {

    //her bliver token lavet
    public static String generateToken(User user) throws JWTVerificationException{
        try {
            // We load the hashing algorithm we wish to use. And create the token with claims.
            Algorithm algorithm = Algorithm.HMAC256("secret");
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withIssuedAt(new Date(System.currentTimeMillis()))
                    .withExpiresAt(new Date(System.currentTimeMillis() + 900000)) // equals 15 minutes
                    .withSubject(Integer.toString(user.getId()))
                    .sign(algorithm);

            return token;
        } catch (JWTCreationException exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        return null;
    }

    // Her bliver token verificeret
    public static boolean verifyToken(String token, User user) {


        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .withSubject(Integer.toString(user.getId()))
                    .build(); //Reusable verifier instance

            verifier.verify(token);

            return true;

        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
        }
        return false;
    }


}
