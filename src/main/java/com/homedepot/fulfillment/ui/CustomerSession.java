package com.homedepot.fulfillment.ui;

/**
 * In-memory session holder for the currently identified customer.
 * Persists for the lifetime of the JVM (app session) so navigating to/from
 * the associate screen doesn't force re-login. Cleared on sign-out.
 */
public class CustomerSession {

    private static Long   customerId;
    private static String firstName;
    private static String lastName;

    public static boolean hasSession()         { return customerId != null; }
    public static Long    getCustomerId()      { return customerId; }
    public static String  getFirstName()       { return firstName; }
    public static String  getLastName()        { return lastName; }

    public static void set(Long id, String first, String last) {
        customerId = id;
        firstName  = first;
        lastName   = last;
    }

    public static void clear() {
        customerId = null;
        firstName  = null;
        lastName   = null;
    }
}
