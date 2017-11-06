package m.group.sem.projectm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by drbum on 03-Nov-17.
 */

public class AccountHelper {

    public AccountHelper() {
    }

    protected boolean isUsernameValid(String username) {
        //TODO: Introduce some kind of SQL injection protection
        return username.length() >= 3;
    }

    protected boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6;
    }

    /**
     * hashes the password using the SHA-256 algorithm
     *
     * @param password the password to be hashed
     * @return the hash of the password
     */
    protected String hashPassword(String password) {
        byte[] hashBytes = null;
        // shitty (or sneaky) attempt at salting the password :)
        password += password.substring(0, 4);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            hashBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return String.format("%064x", new java.math.BigInteger(1, hashBytes)).toLowerCase();
    }

}
