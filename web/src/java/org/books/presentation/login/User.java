package org.books.presentation.login;

/**
 * Represents the user logged in.
 * 
 * This is a value object.
 *
 * @author Ronny Stauffer
 */
public class User {
    private String firstName;
    private String lastName;
    private String eMailAddress;
    
    public static class Builder {
        private String firstName;
        
        private Builder(String firstName) {
            assert firstName != null && !firstName.isEmpty();
            
            this.firstName = firstName;
        }
        
        public static class Builder2 {
            private Builder builder;
            
            private String lastName;
            private String eMailAddress;
            
            private Builder2(Builder builder, String lastName) {
                assert builder != null;
                assert lastName != null && !lastName.isEmpty();
                
                this.builder = builder;
                this.lastName = lastName;
            }
            
            public Builder2 eMailAddress(String eMailAddress) {
//                if (eMailAddress == null) {
//                    throw new NullPointerException("eMailAddress must not be null!");
//                }
                if (eMailAddress.isEmpty()) {
                    throw new IllegalArgumentException("eMailAddress must not be empty!");
                }
                
                this.eMailAddress = eMailAddress;
                
                return this;
            }
            
            public User build() {
                User user = new User();
                user.firstName = builder.firstName;
                user.lastName = lastName;
                user.eMailAddress = eMailAddress;
                
                return user;
            }
        }
        
        public Builder2 lastName(String lastName) {
            if (lastName == null) {
                throw new NullPointerException("lastName must not be null!");
            }
            if (lastName.isEmpty()) {
                throw new IllegalArgumentException("lastName must not be empty!");
            }
            
            return new Builder2(this, lastName);
        }
    }
    
    private User() {
        
    }
    
    public static Builder firstName(String firstName) {
        if (firstName == null) {
            throw new NullPointerException("firstName must not be null!");
        }
        if (firstName.isEmpty()) {
            throw new IllegalArgumentException("firstName must not be empty!");
        }
        
        return new Builder(firstName);
    }
    
    // Required attribute
    public String getFirstName() {
        return firstName;
    }
    
    // Required attribute
    public String getLastName() {
        return lastName;
    }
    
    public String getEMailAddress() {
        return eMailAddress;
    }
}
